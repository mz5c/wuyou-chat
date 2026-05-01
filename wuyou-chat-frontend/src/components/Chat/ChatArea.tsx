import { useState, useCallback, useEffect } from 'react';
import { MessageList } from './MessageList';
import { ChatInput } from './ChatInput';
import { RoleSelector } from './RoleSelector';
import { useSSE } from '../../hooks/useSSE';
import { api } from '../../services/api';
import type { Message, ChatRecord, Session } from '../../types';

interface Props {
  session: Session | null;
  onRoleChange: (roleType: string) => void;
  onFirstMessage: (sessionId: number) => void;
}

export function ChatArea({ session, onRoleChange, onFirstMessage }: Props) {
  const [messages, setMessages] = useState<Message[]>([]);
  const [streamingContent, setStreamingContent] = useState('');
  const { isStreaming, startStream } = useSSE();

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
    if (!session) return;

    const userMsg: Message = {
      id: `q-${Date.now()}`,
      role: 'user',
      content: message,
      createdAt: new Date().toISOString(),
    };
    setMessages(prev => [...prev, userMsg]);

    if (messages.length === 0) {
      onFirstMessage(session.id);
    }

    setStreamingContent('');
    let fullContent = '';

    startStream(
      session.id,
      message,
      (chunk) => {
        fullContent += chunk;
        setStreamingContent(fullContent);
      },
      () => {
        const aiMsg: Message = {
          id: `a-${Date.now()}`,
          role: 'assistant',
          content: fullContent,
          createdAt: new Date().toISOString(),
        };
        setMessages(prev => [...prev, aiMsg]);
        setStreamingContent('');
      },
      (err) => {
        console.error('Stream error:', err);
        setStreamingContent('');
      },
    );
  }, [session, messages, startStream, onFirstMessage]);

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
