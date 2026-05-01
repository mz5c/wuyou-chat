import { MarkdownRenderer } from '../Markdown/MarkdownRenderer';

interface Props {
  role: 'user' | 'assistant';
  content: string;
  createdAt?: string;
}

export function MessageBubble({ role, content, createdAt }: Props) {
  return (
    <div className={`message ${role}`}>
      <div className="message-avatar">
        {role === 'user' ? '👤' : '🤖'}
      </div>
      <div className="message-content">
        {role === 'assistant' ? (
          <MarkdownRenderer content={content} />
        ) : (
          <p>{content}</p>
        )}
        {createdAt && <span className="message-time">{createdAt}</span>}
      </div>
    </div>
  );
}
