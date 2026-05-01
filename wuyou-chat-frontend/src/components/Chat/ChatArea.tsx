import { useState, useCallback, useEffect, useRef } from 'react';
import { MessageList } from './MessageList';
import { ChatInput } from './ChatInput';
import { RoleSelector } from './RoleSelector';
import { useSSE } from '../../hooks/useSSE';
import { api } from '../../services/api';
import type { Message, ChatRecord, Session } from '../../types';

interface Props {
  session: Session | null;
  onRoleChange: (roleType: string) => void;
  onFirstMessage: () => void;
}

export function ChatArea({ session, onRoleChange, onFirstMessage }: Props) {
  const [messages, setMessages] = useState<Message[]>([]);
  const [streamingContent, setStreamingContent] = useState('');
  const { isStreaming, startStream, stopStream } = useSSE();

  // 用 ref 存储流式内容，避免闭包陈旧问题
  const streamingRef = useRef('');
  const sessionRef = useRef(session);
  sessionRef.current = session;
  const onFirstMessageRef = useRef(onFirstMessage);
  onFirstMessageRef.current = onFirstMessage;

  useEffect(() => {
    if (!session) {
      setMessages([]);
      return;
    }
    setMessages([]);
    api.getHistoryBySession(session.id).then((records: ChatRecord[]) => {
      const history: Message[] = [];
      for (const r of records) {
        history.push({ id: `q-${r.id}`, role: 'user', content: r.question, createdAt: r.createdAt });
        history.push({ id: `a-${r.id}`, role: 'assistant', content: r.answer, createdAt: r.createdAt });
      }
      setMessages(history);
    }).catch(console.error);
  }, [session?.id]);

  const handleSend = useCallback((message: string) => {
    const currentSession = sessionRef.current;
    if (!currentSession) return;

    const userMsg: Message = {
      id: `q-${Date.now()}`,
      role: 'user',
      content: message,
      createdAt: new Date().toISOString(),
    };
    setMessages(prev => {
      // 首次消息触发标题更新
      if (prev.length === 0) {
        onFirstMessageRef.current();
      }
      return [...prev, userMsg];
    });

    streamingRef.current = '';
    setStreamingContent('');

    startStream(
      currentSession.id,
      message,
      // onChunk — 直接追加到 ref，并同步 streamingContent 用于渲染
      (chunk) => {
        streamingRef.current += chunk;
        setStreamingContent(streamingRef.current);
      },
      // onDone — 从 ref 读取最终内容添加到消息列表
      () => {
        const finalContent = streamingRef.current;
        streamingRef.current = '';
        setStreamingContent('');
        if (finalContent) {
          setMessages(existing => [...existing, {
            id: `a-${Date.now()}`,
            role: 'assistant',
            content: finalContent,
            createdAt: new Date().toISOString(),
          }]);
        }
      },
      // onError
      (err) => {
        console.error('Stream error:', err);
        streamingRef.current = '';
        setStreamingContent('');
      },
    );
  }, [startStream]);

  // 组件卸载时停止流式请求
  useEffect(() => {
    return () => {
      stopStream();
    };
  }, [stopStream]);

  if (!session) {
    return (
      <div className="chat-area">
        <div className="messages-empty">
          <h2>欢迎使用友聊</h2>
          <p>点击左侧 "+" 新建会话，开始与 AI 助手对话</p>
        </div>
      </div>
    );
  }

  return (
    <div className="chat-area">
      <div className="chat-header">
        <span className="chat-title">{session.title}</span>
        <RoleSelector currentRole={session.roleType} onChange={onRoleChange} />
      </div>
      <MessageList messages={messages} streamingContent={streamingContent} isStreaming={isStreaming} />
      <ChatInput onSend={handleSend} disabled={isStreaming} />
    </div>
  );
}
