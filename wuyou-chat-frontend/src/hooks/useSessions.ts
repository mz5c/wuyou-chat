import { useState, useEffect, useCallback } from 'react';
import { api } from '../services/api';
import type { Session } from '../types';

export function useSessions() {
  const [sessions, setSessions] = useState<Session[]>([]);
  const [activeId, setActiveId] = useState<number | null>(null);
  const [loading, setLoading] = useState(true);

  const loadSessions = useCallback(async () => {
    try {
      const list = await api.listSessions();
      setSessions(list);
    } catch (err) {
      console.error('Failed to load sessions', err);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { loadSessions(); }, [loadSessions]);

  const createSession = useCallback(async (roleType?: string) => {
    const session = await api.createSession(undefined, roleType);
    setSessions(prev => [session, ...prev]);
    setActiveId(session.id);
    return session;
  }, []);

  const renameSession = useCallback(async (id: number, title: string) => {
    await api.renameSession(id, title);
    setSessions(prev => prev.map(s => s.id === id ? { ...s, title } : s));
  }, []);

  const deleteSession = useCallback(async (id: number) => {
    await api.deleteSession(id);
    setSessions(prev => prev.filter(s => s.id !== id));
    if (activeId === id) setActiveId(null);
  }, [activeId]);

  const updateRole = useCallback(async (id: number, roleType: string) => {
    const updated = await api.updateRole(id, roleType);
    setSessions(prev => prev.map(s => s.id === id ? { ...s, ...updated } : s));
  }, []);

  return {
    sessions, activeId, loading,
    setActiveId, createSession, renameSession, deleteSession, updateRole, refresh: loadSessions,
  };
}
