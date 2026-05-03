import { useState, useRef, useCallback } from 'react';
import { createSSEConnection } from '../services/sse';
import type { StreamChunk } from '../types';

export function useSSE() {
  const [isStreaming, setIsStreaming] = useState(false);
  const abortRef = useRef<AbortController | null>(null);

  const startStream = useCallback((
    sessionId: number,
    message: string,
    onChunk: (chunk: string) => void,
    onDone: () => void,
    onError: (err: string) => void,
    modelId?: number | null,
  ) => {
    setIsStreaming(true);
    abortRef.current = createSSEConnection(
      sessionId,
      message,
      (chunk: StreamChunk) => {
        if (chunk.type === 'text') {
          onChunk(chunk.content);
        }
      },
      (err) => {
        setIsStreaming(false);
        onError(err);
      },
      () => {
        setIsStreaming(false);
        onDone();
      },
      modelId,
    );
  }, []);

  const stopStream = useCallback(() => {
    if (abortRef.current) {
      abortRef.current.abort();
      abortRef.current = null;
    }
    setIsStreaming(false);
  }, []);

  return { isStreaming, startStream, stopStream };
}
