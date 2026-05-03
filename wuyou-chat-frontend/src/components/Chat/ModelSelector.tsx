import { useState, useEffect } from 'react';
import { api } from '../../services/api';
import type { ModelInfo } from '../../types';

interface Props {
  currentModelId: number | null;
  onChange: (modelId: number | null) => void;
}

export function ModelSelector({ currentModelId, onChange }: Props) {
  const [models, setModels] = useState<ModelInfo[]>([]);

  useEffect(() => {
    api.getEnabledModels().then(data => {
      setModels(data);
      // Auto-select first model if none selected
      if (data.length > 0 && currentModelId === null) {
        onChange(data[0].id);
      }
    }).catch(() => {
      setModels([]);
    });
  }, []);

  if (models.length === 0) return null;

  return (
    <select
      className="model-selector"
      value={currentModelId ?? ''}
      onChange={(e) => {
        const val = e.target.value;
        onChange(val ? Number(val) : null);
      }}
    >
      {models.map(m => (
        <option key={m.id} value={m.id}>{m.name}</option>
      ))}
    </select>
  );
}
