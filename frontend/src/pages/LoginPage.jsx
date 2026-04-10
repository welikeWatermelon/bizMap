import { useState } from 'react';
import { Link } from 'react-router-dom';
import useAuth from '../hooks/useAuth';

export default function LoginPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const { loading, error, doLogin } = useAuth();

  const handleSubmit = (e) => {
    e.preventDefault();
    doLogin(email, password);
  };

  return (
    <div style={styles.container}>
      <div style={styles.card}>
        <h1 style={styles.title}>BizMap</h1>
        <p style={styles.subtitle}>매장 관리 + 지도 시각화</p>
        <form onSubmit={handleSubmit}>
          <input
            type="email" placeholder="이메일" value={email}
            onChange={(e) => setEmail(e.target.value)}
            style={styles.input} required
          />
          <input
            type="password" placeholder="비밀번호" value={password}
            onChange={(e) => setPassword(e.target.value)}
            style={styles.input} required
          />
          {error && <p style={styles.error}>{error}</p>}
          <button type="submit" disabled={loading} style={styles.button}>
            {loading ? '로그인 중...' : '로그인'}
          </button>
        </form>
        <p style={styles.linkText}>
          계정이 없으신가요? <Link to="/register">회원가입</Link>
        </p>
      </div>
    </div>
  );
}

const styles = {
  container: {
    display: 'flex', justifyContent: 'center', alignItems: 'center',
    minHeight: '100vh', backgroundColor: '#f5f5f5',
  },
  card: {
    backgroundColor: '#fff', padding: '48px', borderRadius: '8px',
    boxShadow: '0 2px 8px rgba(0,0,0,0.1)', width: '100%', maxWidth: '400px',
  },
  title: { textAlign: 'center', margin: '0 0 4px', color: '#1976d2' },
  subtitle: { textAlign: 'center', margin: '0 0 32px', color: '#666', fontSize: '14px' },
  input: {
    width: '100%', padding: '12px', marginBottom: '16px', border: '1px solid #ddd',
    borderRadius: '4px', fontSize: '14px', boxSizing: 'border-box',
  },
  button: {
    width: '100%', padding: '12px', backgroundColor: '#1976d2', color: '#fff',
    border: 'none', borderRadius: '4px', fontSize: '16px', cursor: 'pointer',
  },
  error: { color: '#d32f2f', fontSize: '13px', margin: '0 0 12px' },
  linkText: { textAlign: 'center', marginTop: '16px', fontSize: '14px' },
};
