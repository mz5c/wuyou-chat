import { SessionItem } from './SessionItem';
import type { Session } from '../../types';

interface Props {
  sessions: Session[];
  activeId: number | null;
  loading: boolean;
  onSelect: (id: number) => void;
  onCreate: () => void;
  onRename: (id: number, title: string) => void;
  onDelete: (id: number) => void;
}

export function Sidebar({ sessions, activeId, loading, onSelect, onCreate, onRename, onDelete }: Props) {
  return (
    <aside className="sidebar">
      <div className="sidebar-header">
        <h3>友聊</h3>
        <button className="btn-new" onClick={onCreate} title="新建会话">+</button>
      </div>
      <div className="sidebar-list">
        {loading ? (
          <div className="sidebar-loading">加载中...</div>
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
