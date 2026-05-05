import { useEffect, useRef } from 'react';
import { MessageBubble } from './MessageBubble';
import type { Message } from '../../types';

interface Props {
  messages: Message[];
  streamingContent?: string;
  isStreaming: boolean;
  loading?: boolean;
  loadingMore?: boolean;
  onScrollToTop?: () => void;
}

export function MessageList({ messages, streamingContent, isStreaming, loading, loadingMore, onScrollToTop }: Props) {
  const bottomRef = useRef<HTMLDivElement>(null);
  const listRef = useRef<HTMLDivElement>(null);
  const onScrollToTopRef = useRef(onScrollToTop);
  onScrollToTopRef.current = onScrollToTop;

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  // 上划到顶部触发历史记录加载 — ref pattern, listener only registered once
  useEffect(() => {
    const el = listRef.current;
    if (!el) return;
    const handler = () => {
      if (el.scrollTop < 50) {
        onScrollToTopRef.current?.();
      }
    };
    el.addEventListener('scroll', handler);
    return () => el.removeEventListener('scroll', handler);
  }, []);  // empty deps — never re-register

  if (loading) {
    return (
      <div className="messages-empty">
        <div className="loading-spinner" />
        <p>加载中...</p>
      </div>
    );
  }

  if (messages.length === 0 && !isStreaming) {
    return (
      <div className="messages-empty">
        <h2>开始对话</h2>
        <p>选择或创建一个会话，开始与 AI 助手交流</p>
      </div>
    );
  }

  return (
    <div className="messages-list" ref={listRef}>
      {loadingMore && (
        <div className="history-loading">
          <div className="loading-spinner" />
          <p>加载历史记录...</p>
        </div>
      )}
      {messages.map(m => (
        <MessageBubble key={m.id} role={m.role} content={m.content} createdAt={m.createdAt} />
      ))}
      {isStreaming && streamingContent && (
        <MessageBubble role="assistant" content={streamingContent} />
      )}
      {isStreaming && !streamingContent && (
        <div className="message ai">
          <div className="message-bubble typing-indicator">
            <span className="typing-dot" /><span className="typing-dot" /><span className="typing-dot" />
          </div>
        </div>
      )}
      <div ref={bottomRef} />
    </div>
  );
}
