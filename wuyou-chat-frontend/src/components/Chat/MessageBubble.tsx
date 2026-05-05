import { MarkdownRenderer } from '../Markdown/MarkdownRenderer';

interface Props {
  role: 'user' | 'assistant';
  content: string;
  createdAt?: string;
}

function formatTime(isoStr?: string): string {
  if (!isoStr) return '';
  const d = new Date(isoStr);
  if (isNaN(d.getTime())) return '';
  const now = new Date();
  const isToday = d.toDateString() === now.toDateString();
  const hh = String(d.getHours()).padStart(2, '0');
  const mm = String(d.getMinutes()).padStart(2, '0');
  if (isToday) return `${hh}:${mm}`;
  const month = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${month}-${day} ${hh}:${mm}`;
}

export function MessageBubble({ role, content, createdAt }: Props) {
  return (
    <div className={`message ${role}`}>
      <div className={`message-avatar ${role}`}>
        {role === 'user' ? (
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
            <circle cx="12" cy="7" r="4"/>
          </svg>
        ) : (
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
            <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
          </svg>
        )}
      </div>
      <div className="message-content">
        {role === 'assistant' ? (
          <MarkdownRenderer content={content} />
        ) : (
          <p>{content}</p>
        )}
        {createdAt && <span className="message-time">{formatTime(createdAt)}</span>}
      </div>
    </div>
  );
}
