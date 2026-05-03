export interface ModelInfo {
  id: number;
  name: string;
  provider: string;
  model: string;
  sortOrder: number;
}

export interface Session {
  id: number;
  title: string;
  roleType: string;
  roleDisplayName: string;
  modelId?: number;
  status: number;
  createdAt: string;
  updatedAt: string;
}

export interface ChatRecord {
  id: number;
  userId: number;
  question: string;
  answer: string;
  reasoningContent?: string;
  conversationId: string;
  sessionId: number;
  status: number;
  createdAt: string;
}

export interface RoleType {
  value: string;
  displayName: string;
}

export const ROLE_TYPES: RoleType[] = [
  { value: 'GENERAL', displayName: '通用助手' },
  { value: 'TRANSLATOR', displayName: '翻译助手' },
  { value: 'CODE_REVIEW', displayName: '代码审查' },
  { value: 'WRITER', displayName: '写作助手' },
];

export interface StreamChunk {
  content: string;
  type: 'text' | 'done' | 'error';
}

export interface Message {
  id: string;
  role: 'user' | 'assistant';
  content: string;
  createdAt: string;
}

export interface LoginResponse {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
  userId: number;
  username: string;
  nickname: string;
  refreshToken: string;
}
