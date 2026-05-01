export interface Session {
  id: number;
  title: string;
  roleType: string;
  roleDisplayName: string;
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
