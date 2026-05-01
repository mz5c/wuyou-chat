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
      <button
        className="session-delete"
        onClick={(e) => { e.stopPropagation(); onDelete(); }}
        title="删除会话"
      >
        ×
      </button>
    </div>
  );
}
