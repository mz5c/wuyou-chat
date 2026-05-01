import { useState, type FormEvent } from 'react';
import { api, setToken } from '../services/api';

interface Props {
  onLogin: () => void;
}

export function LoginPage({ onLogin }: Props) {
  const [isRegister, setIsRegister] = useState(false);
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [email, setEmail] = useState('');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    setLoading(true);

    try {
      if (isRegister) {
        const res = await api.register(username, password, email || undefined);
        setToken(res.accessToken);
        onLogin();
      } else {
        const res = await api.login(username, password);
        setToken(res.accessToken);
        onLogin();
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : '操作失败');
    } finally {
      setLoading(false);
    }
  };

  const switchMode = () => {
    setIsRegister(!isRegister);
    setError('');
    setSuccess('');
  };

  return (
    <div className="login-page">
      <div className="login-card">
        <h1>友聊</h1>
        <p className="subtitle">{isRegister ? '创建新账号' : '登录到友聊'}</p>
        <form className="login-form" onSubmit={handleSubmit}>
          <div>
            <label>用户名</label>
            <input
              type="text"
              value={username}
              onChange={e => setUsername(e.target.value)}
              placeholder="请输入用户名"
              required
            />
          </div>
          <div>
            <label>密码</label>
            <input
              type="password"
              value={password}
              onChange={e => setPassword(e.target.value)}
              placeholder="请输入密码"
              required
            />
          </div>
          {isRegister && (
            <div>
              <label>邮箱（选填）</label>
              <input
                type="email"
                value={email}
                onChange={e => setEmail(e.target.value)}
                placeholder="请输入邮箱"
              />
            </div>
          )}
          {error && <div className="login-error">{error}</div>}
          {success && <div className="login-success">{success}</div>}
          <button className="btn-login" type="submit" disabled={loading}>
            {loading ? '处理中...' : isRegister ? '注册' : '登录'}
          </button>
          <div className="switch-auth">
            {isRegister ? '已有账号？' : '没有账号？'}
            <a onClick={switchMode}>{isRegister ? '去登录' : '去注册'}</a>
          </div>
        </form>
      </div>
    </div>
  );
}
