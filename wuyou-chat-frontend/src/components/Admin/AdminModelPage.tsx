import { useState, useEffect } from 'react';
import { api } from '../../services/api';

interface ModelForm {
  name: string;
  provider: string;
  apiUrl: string;
  apiKey: string;
  model: string;
  isEnabled: number;
  sortOrder: number;
}

const emptyForm: ModelForm = {
  name: '', provider: '', apiUrl: '', apiKey: '', model: '', isEnabled: 1, sortOrder: 0,
};

interface Props {
  onBack: () => void;
}

export function AdminModelPage({ onBack }: Props) {
  const [models, setModels] = useState<any[]>([]);
  const [showForm, setShowForm] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [form, setForm] = useState<ModelForm>(emptyForm);
  const [loading, setLoading] = useState(false);

  const loadModels = async () => {
    setLoading(true);
    try {
      const data = await api.getAllModels();
      setModels(data);
    } catch (e: any) {
      alert('加载模型列表失败: ' + e.message);
    }
    setLoading(false);
  };

  useEffect(() => { loadModels(); }, []);

  const openCreate = () => {
    setEditingId(null);
    setForm(emptyForm);
    setShowForm(true);
  };

  const openEdit = (m: any) => {
    setEditingId(m.id);
    setForm({
      name: m.name || '',
      provider: m.provider || '',
      apiUrl: m.apiUrl || '',
      apiKey: m.apiKey || '',
      model: m.model || '',
      isEnabled: m.isEnabled ?? 1,
      sortOrder: m.sortOrder ?? 0,
    });
    setShowForm(true);
  };

  const handleSave = async () => {
    try {
      if (editingId) {
        await api.updateModel(editingId, form);
      } else {
        await api.addModel(form);
      }
      setShowForm(false);
      loadModels();
    } catch (e: any) {
      alert('保存失败: ' + e.message);
    }
  };

  const handleDelete = async (id: number) => {
    if (!confirm('确定删除此模型配置？')) return;
    try {
      await api.deleteModel(id);
      loadModels();
    } catch (e: any) {
      alert('删除失败: ' + e.message);
    }
  };

  return (
    <div className="admin-page">
      <div className="admin-header">
        <h2>模型配置管理</h2>
        <div className="admin-header-actions">
          <button className="btn-send" onClick={openCreate}>新增模型</button>
          <button className="btn-back" onClick={onBack}>返回聊天</button>
        </div>
      </div>

      {showForm && (
        <div className="admin-form">
          <h3>{editingId ? '编辑模型' : '新增模型'}</h3>
          <div className="admin-form-grid">
            <label>名称 <input value={form.name} onChange={e => setForm({...form, name: e.target.value})} placeholder="如 通义千问 3.5" /></label>
            <label>厂商 <input value={form.provider} onChange={e => setForm({...form, provider: e.target.value})} placeholder="如 qwen" /></label>
            <label>API 地址 <input value={form.apiUrl} onChange={e => setForm({...form, apiUrl: e.target.value})} placeholder="https://api.openai.com/v1/chat/completions" /></label>
            <label>API Key <input value={form.apiKey} onChange={e => setForm({...form, apiKey: e.target.value})} placeholder="sk-..." /></label>
            <label>模型名 <input value={form.model} onChange={e => setForm({...form, model: e.target.value})} placeholder="如 qwen3.5-122b" /></label>
            <label>排序 <input type="number" value={form.sortOrder} onChange={e => setForm({...form, sortOrder: Number(e.target.value)})} /></label>
            <label className="admin-form-checkbox">
              <input type="checkbox" checked={form.isEnabled === 1} onChange={e => setForm({...form, isEnabled: e.target.checked ? 1 : 0})} />
              启用
            </label>
          </div>
          <div className="admin-form-actions">
            <button className="btn-send" onClick={handleSave}>保存</button>
            <button className="btn-back" onClick={() => setShowForm(false)}>取消</button>
          </div>
        </div>
      )}

      {loading ? (
        <div className="sidebar-loading">加载中...</div>
      ) : models.length === 0 ? (
        <div className="sidebar-empty">暂无模型配置，点击"新增模型"添加</div>
      ) : (
        <table className="admin-table">
          <thead>
            <tr>
              <th>ID</th><th>名称</th><th>厂商</th><th>模型</th><th>API 地址</th><th>启用</th><th>排序</th><th>操作</th>
            </tr>
          </thead>
          <tbody>
            {models.map(m => (
              <tr key={m.id}>
                <td>{m.id}</td>
                <td>{m.name}</td>
                <td>{m.provider}</td>
                <td>{m.model}</td>
                <td style={{maxWidth: 200, overflow: 'hidden', textOverflow: 'ellipsis'}}>{m.apiUrl}</td>
                <td>{m.isEnabled === 1 ? '是' : '否'}</td>
                <td>{m.sortOrder}</td>
                <td>
                  <button className="btn-sm" onClick={() => openEdit(m)}>编辑</button>
                  <button className="btn-sm btn-sm-danger" onClick={() => handleDelete(m.id)}>删除</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}
