import type { StreamChunk } from '../types';

export function createSSEConnection(
  sessionId: number,
  message: string,
  onChunk: (chunk: StreamChunk) => void,
  onError: (error: string) => void,
  onDone: () => void
): AbortController {
  const controller = new AbortController();
  const token = localStorage.getItem('accessToken') || '';

  fetch('/api/chat/ask/stream', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify({ sessionId, message }),
    signal: controller.signal,
  }).then(async (response) => {
    if (!response.body) {
      onError('No response body');
      return;
    }
    const reader = response.body.getReader();
    const decoder = new TextDecoder();
    let buffer = '';

    while (true) {
      const { done, value } = await reader.read();
      if (done) break;

      buffer += decoder.decode(value, { stream: true });
      const lines = buffer.split('\n');
      buffer = lines.pop() || '';

      for (const line of lines) {
        if (line.startsWith('data: ')) {
          const data = line.substring(6).trim();
          if (data === '[DONE]') {
            onDone();
            return;
          }
          try {
            const parsed = JSON.parse(data);
            onChunk({ content: parsed.content || '', type: parsed.type || 'text' });
          } catch {
            // ignore malformed JSON
          }
        }
      }
    }
    onDone();
  }).catch((err) => {
    if (err.name !== 'AbortError') {
      onError(err.message || 'SSE connection failed');
    }
  });

  return controller;
}
