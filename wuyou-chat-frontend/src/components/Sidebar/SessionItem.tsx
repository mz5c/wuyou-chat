import type { Session } from '../../types';

interface Props {
  session: Session;
  isActive: boolean;
  onClick: () => void;
  onRename: (title: string) => void;
  onDelete: () => void;
}

export function SessionItem({ session, isActive, onClick, onRename, onDelete }: Props) {
  const handleContextMenu = (e: React.MouseEvent) => {
    e.preventDefault();
    const newTitle = prompt('重命名会话', session.title);
    if (newTitle && newTitle.trim()) onRename(newTitle.trim());
  };

  return (
    <div
      className={`session-item ${isActive ? 'active' : ''}`}
      onClick={onClick}
      onContextMenu={handleContextMenu}
      title={session.title}
    >
      <div className="session-avatar">
        {session.title.charAt(0).toUpperCase()}
      </div>
      <div className="session-info">
        <div className="session-title">{session.title}</div>
        <div className="session-role">{session.roleDisplayName}</div>
      </div>
      <div className="session-actions">
        <button
          className="session-rename"
          onClick={(e) => { e.stopPropagation(); handleContextMenu(e); }}
          title="重命名会话"
        >
          <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <path d="M17 3a2.85 2.85 0 1 1 4 4L7.5 20.5 2 22l1.5-5.5Z"/>
          </svg>
        </button>
        <button
          className="session-delete"
          onClick={(e) => { e.stopPropagation(); onDelete(); }}
          title="删除会话"
        >
          ×
        </button>
      </div>
    </div>
  );
}
