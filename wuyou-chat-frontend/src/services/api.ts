import type { Session, ChatRecord, LoginResponse } from '../types';

const API_BASE = '/api';

function getToken(): string {
  return localStorage.getItem('accessToken') || '';
}

async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  const token = getToken();
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...(options.headers as Record<string, string>),
  };
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }
  const res = await fetch(`${API_BASE}${path}`, { ...options, headers });

  if (!res.ok) {
    if (res.status === 401 || res.status === 403) {
      clearToken();
      window.dispatchEvent(new CustomEvent('auth:expired'));
      throw new Error('未登录或登录已过期，请重新登录');
    }
    throw new Error(`请求失败 (${res.status})`);
  }

  const text = await res.text();
  if (!text) return undefined as T;

  let json;
  try {
    json = JSON.parse(text);
  } catch {
    throw new Error('服务器返回数据格式异常');
  }

  if (json.code !== 200) {
    throw new Error(json.message || '请求失败');
  }
  return json.data;
}

export function setToken(token: string) {
  localStorage.setItem('accessToken', token);
}

export function clearToken() {
  localStorage.removeItem('accessToken');
}

export function isLoggedIn(): boolean {
  return !!localStorage.getItem('accessToken');
}

export const api = {
  login(username: string, password: string) {
    return request<LoginResponse>('/auth/login', {
      method: 'POST',
      body: JSON.stringify({ username, password }),
    });
  },
  register(username: string, password: string, email?: string) {
    return request<LoginResponse>('/auth/register', {
      method: 'POST',
      body: JSON.stringify({ username, password, email }),
    });
  },
  // 会话管理
  createSession(title?: string, roleType?: string) {
    return request<Session>('/session/create', {
      method: 'POST',
      body: JSON.stringify({ title, roleType }),
    });
  },
  listSessions() {
    return request<Session[]>('/session/list');
  },
  getSession(id: number) {
    return request<Session>(`/session/${id}`);
  },
  renameSession(id: number, title: string) {
    return request<Session>(`/session/${id}/rename`, {
      method: 'PUT',
      body: JSON.stringify({ title }),
    });
  },
  updateRole(id: number, roleType: string) {
    return request<Session>(`/session/${id}/role`, {
      method: 'PUT',
      body: JSON.stringify({ title: roleType }),
    });
  },
  deleteSession(id: number) {
    return request<void>(`/session/${id}`, { method: 'DELETE' });
  },

  // 聊天记录
  getHistoryBySession(sessionId: number) {
    return request<ChatRecord[]>(`/chat/record/list/${sessionId}`);
  },
};
