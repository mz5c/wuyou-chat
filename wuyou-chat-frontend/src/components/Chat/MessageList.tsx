import { useEffect, useRef } from 'react';
import { MessageBubble } from './MessageBubble';
import type { Message } from '../../types';

interface Props {
  messages: Message[];
  streamingContent?: string;
  isStreaming: boolean;
}

export function MessageList({ messages, streamingContent, isStreaming }: Props) {
  const bottomRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages, streamingContent]);

  if (messages.length === 0 && !isStreaming) {
    return (
      <div className="messages-empty">
        <h2>开始对话</h2>
        <p>选择或创建一个会话，开始与 AI 助手交流</p>
      </div>
    );
  }

  return (
    <div className="messages-list">
      {messages.map(m => (
        <MessageBubble key={m.id} role={m.role} content={m.content} createdAt={m.createdAt} />
      ))}
      {isStreaming && streamingContent && (
        <MessageBubble role="assistant" content={streamingContent} />
      )}
      <div ref={bottomRef} />
    </div>
  );
}
