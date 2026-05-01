import { SessionItem } from './SessionItem';
import type { Session } from '../../types';

interface Props {
  sessions: Session[];
  activeId: number | null;
  loading: boolean;
  error: string | null;
  onSelect: (id: number) => void;
  onCreate: () => void;
  onRename: (id: number, title: string) => void;
  onDelete: (id: number) => void;
  onRetry: () => void;
}

export function Sidebar({ sessions, activeId, loading, error, onSelect, onCreate, onRename, onDelete, onRetry }: Props) {
  return (
    <aside className="sidebar">
      <div className="sidebar-header">
        <h3>友聊</h3>
        <button className="btn-new" onClick={onCreate} title="新建会话">+</button>
      </div>
      <div className="sidebar-list">
        {loading ? (
          <div className="sidebar-loading">加载中...</div>
        ) : error ? (
          <div className="sidebar-error">
            <p>{error}</p>
            <button className="btn-retry" onClick={onRetry}>重试</button>
          </div>
        ) : sessions.length === 0 ? (
          <div className="sidebar-empty">暂无会话</div>
        ) : (
          sessions.map(s => (
            <SessionItem
              key={s.id}
              session={s}
              isActive={s.id === activeId}
              onClick={() => onSelect(s.id)}
              onRename={(title) => onRename(s.id, title)}
              onDelete={() => onDelete(s.id)}
            />
          ))
        )}
      </div>
    </aside>
  );
}
