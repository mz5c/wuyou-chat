import { useState, useCallback, useEffect } from 'react';
import { Sidebar } from './components/Sidebar/Sidebar';
import { ChatArea } from './components/Chat/ChatArea';
import { LoginPage } from './components/LoginPage';
import { useSessions } from './hooks/useSessions';
import { isLoggedIn, clearToken } from './services/api';
import type { Session } from './types';
import './styles/global.css';

function App() {
  const [authenticated, setAuthenticated] = useState(isLoggedIn);

  useEffect(() => {
    const handler = () => setAuthenticated(false);
    window.addEventListener('auth:expired', handler);
    return () => window.removeEventListener('auth:expired', handler);
  }, []);

  const {
    sessions, activeId, loading, error,
    setActiveId, createSession, renameSession, deleteSession, updateRole, refresh,
  } = useSessions();

  const [currentSession, setCurrentSession] = useState<Session | null>(null);

  const handleLogin = useCallback(() => {
    setAuthenticated(true);
    refresh();
  }, [refresh]);

  const handleLogout = useCallback(() => {
    clearToken();
    setAuthenticated(false);
    setCurrentSession(null);
  }, []);

  const handleSelect = useCallback((id: number) => {
    setActiveId(id);
    const found = sessions.find(s => s.id === id);
    if (found) setCurrentSession(found);
  }, [sessions, setActiveId]);

  const handleCreate = useCallback(async () => {
    try {
      const session = await createSession();
      setCurrentSession(session);
    } catch (err) {
      console.error('Failed to create session', err);
    }
  }, [createSession]);

  const handleDelete = useCallback(async (id: number) => {
    if (!confirm('确定删除此会话？')) return;
    await deleteSession(id);
    if (currentSession?.id === id) setCurrentSession(null);
  }, [deleteSession, currentSession]);

  const handleRoleChange = useCallback(async (roleType: string) => {
    if (!currentSession) return;
    await updateRole(currentSession.id, roleType);
    setCurrentSession(prev => prev ? { ...prev, roleType } : null);
  }, [currentSession, updateRole]);

  const handleFirstMessage = useCallback(() => {
    refresh();
  }, [refresh]);

  if (!authenticated) {
    return <LoginPage onLogin={handleLogin} />;
  }

  return (
    <div className="app-layout">
      <Sidebar
        sessions={sessions}
        activeId={activeId}
        loading={loading}
        error={error}
        onSelect={handleSelect}
        onCreate={handleCreate}
        onRename={renameSession}
        onDelete={handleDelete}
        onRetry={refresh}
        onLogout={handleLogout}
      />
      <ChatArea
        session={currentSession}
        onRoleChange={handleRoleChange}
        onFirstMessage={handleFirstMessage}
      />
    </div>
  );
}

export default App;
