import { useEffect, useRef, useState } from 'react';
import Navbar from '../components/Navbar';
import { getWidgetKeys, createWidgetKey, deleteWidgetKey } from '../api/widgetKeys';

const BACKEND_URL = (import.meta.env.VITE_API_URL || 'http://localhost:8080/api').replace(/\/api$/, '');

export default function WidgetPage() {
  const [keys, setKeys] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [name, setName] = useState('');
  const [allowedOrigin, setAllowedOrigin] = useState('');
  const [creating, setCreating] = useState(false);
  const [selectedKey, setSelectedKey] = useState(null);
  const [copied, setCopied] = useState(false);
  const previewRef = useRef(null);

  const loadKeys = async () => {
    try {
      setLoading(true);
      const res = await getWidgetKeys();
      const data = res.data.data || [];
      setKeys(data);
      if (data.length > 0 && !selectedKey) {
        setSelectedKey(data[0]);
      }
    } catch (e) {
      setError('위젯 키 목록을 불러오지 못했습니다.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { loadKeys(); }, []);

  useEffect(() => {
    if (!selectedKey) return;
    const container = previewRef.current;
    if (!container) return;

    container.innerHTML = '';
    const script = document.createElement('script');
    script.src = `${BACKEND_URL}/widget/bizmap-widget.js?key=${selectedKey.apiKey}`;
    script.async = true;
    document.head.appendChild(script);

    return () => {
      if (script.parentNode) script.parentNode.removeChild(script);
      if (container) container.innerHTML = '';
    };
  }, [selectedKey?.id, selectedKey?.apiKey]);

  const handleCreate = async (e) => {
    e.preventDefault();
    if (!name.trim()) return;
    try {
      setCreating(true);
      const res = await createWidgetKey({ name: name.trim(), allowedOrigin: allowedOrigin.trim() || null });
      const created = res.data.data;
      setKeys((prev) => [created, ...prev]);
      setSelectedKey(created);
      setName('');
      setAllowedOrigin('');
    } catch (e) {
      alert('위젯 키 발급에 실패했습니다.');
    } finally {
      setCreating(false);
    }
  };

  const handleDelete = async (id) => {
    if (!confirm('이 위젯 키를 삭제하시겠습니까?')) return;
    try {
      await deleteWidgetKey(id);
      setKeys((prev) => prev.filter((k) => k.id !== id));
      if (selectedKey?.id === id) {
        setSelectedKey(null);
      }
    } catch (e) {
      alert('삭제에 실패했습니다.');
    }
  };

  const buildEmbedCode = (key) =>
    `<script src="${BACKEND_URL}/widget/bizmap-widget.js?key=${key.apiKey}"></script>\n<div id="bizmap-widget" style="width:100%;height:400px;"></div>`;

  const handleCopy = (key) => {
    navigator.clipboard.writeText(buildEmbedCode(key));
    setCopied(true);
    setTimeout(() => setCopied(false), 1500);
  };

  return (
    <div style={{ minHeight: '100vh', backgroundColor: '#f5f5f5' }}>
      <Navbar />
      <div style={styles.container}>
        <h2>위젯 관리</h2>
        <p style={{ color: '#666', fontSize: '14px' }}>
          외부 사이트에 매장 지도 위젯을 임베드하기 위한 API 키를 발급하고 관리합니다.
        </p>

        <div style={styles.card}>
          <h3 style={styles.cardTitle}>새 위젯 키 발급</h3>
          <form onSubmit={handleCreate} style={styles.form}>
            <input
              type="text"
              placeholder="위젯 이름 (예: 메인 홈페이지)"
              value={name}
              onChange={(e) => setName(e.target.value)}
              style={styles.input}
              required
            />
            <input
              type="text"
              placeholder="허용 도메인 (선택, 예: https://example.com)"
              value={allowedOrigin}
              onChange={(e) => setAllowedOrigin(e.target.value)}
              style={styles.input}
            />
            <button type="submit" disabled={creating} style={styles.primaryBtn}>
              {creating ? '발급 중...' : '발급'}
            </button>
          </form>
        </div>

        <div style={styles.card}>
          <h3 style={styles.cardTitle}>발급된 위젯 키 ({keys.length})</h3>
          {loading ? (
            <p style={{ color: '#999' }}>로딩 중...</p>
          ) : error ? (
            <p style={{ color: '#d32f2f' }}>{error}</p>
          ) : keys.length === 0 ? (
            <p style={{ color: '#999' }}>발급된 위젯 키가 없습니다.</p>
          ) : (
            <table style={{ width: '100%', borderCollapse: 'collapse' }}>
              <thead>
                <tr>
                  <th style={styles.th}>이름</th>
                  <th style={styles.th}>API 키</th>
                  <th style={styles.th}>허용 도메인</th>
                  <th style={styles.th}>발급일</th>
                  <th style={styles.th}></th>
                </tr>
              </thead>
              <tbody>
                {keys.map((k) => (
                  <tr key={k.id} style={selectedKey?.id === k.id ? { backgroundColor: '#e3f2fd' } : null}>
                    <td style={styles.td}>{k.name}</td>
                    <td style={{ ...styles.td, fontFamily: 'monospace', fontSize: '12px' }}>
                      {k.apiKey}
                    </td>
                    <td style={styles.td}>{k.allowedOrigin || '-'}</td>
                    <td style={styles.td}>{k.createdAt?.substring(0, 10)}</td>
                    <td style={styles.td}>
                      <button onClick={() => setSelectedKey(k)} style={styles.smallBtn}>미리보기</button>
                      <button onClick={() => handleDelete(k.id)} style={styles.dangerBtn}>삭제</button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>

        {selectedKey && (
          <div style={styles.card}>
            <h3 style={styles.cardTitle}>임베드 코드 - {selectedKey.name}</h3>
            <div style={styles.codeBox}>
              <pre style={styles.pre}>{buildEmbedCode(selectedKey)}</pre>
              <button onClick={() => handleCopy(selectedKey)} style={styles.copyBtn}>
                {copied ? '복사됨!' : '복사'}
              </button>
            </div>

            <h4 style={{ marginTop: '24px' }}>미리보기</h4>
            <div
              ref={previewRef}
              id="bizmap-widget"
              style={styles.previewBox}
            />
            <p style={{ fontSize: '12px', color: '#888' }}>
              실제 임베드와 동일한 방식으로 위젯 스크립트를 로드해 표시합니다.
            </p>
          </div>
        )}
      </div>
    </div>
  );
}

const styles = {
  container: { maxWidth: '1000px', margin: '0 auto', padding: '24px' },
  card: {
    backgroundColor: '#fff', padding: '24px', borderRadius: '8px',
    boxShadow: '0 1px 4px rgba(0,0,0,0.1)', marginTop: '16px',
  },
  cardTitle: { margin: '0 0 16px', fontSize: '16px' },
  form: { display: 'flex', gap: '8px', flexWrap: 'wrap' },
  input: {
    flex: '1 1 200px', padding: '8px 12px', fontSize: '14px',
    border: '1px solid #ccc', borderRadius: '4px',
  },
  primaryBtn: {
    padding: '8px 20px', backgroundColor: '#1976d2', color: '#fff',
    border: 'none', borderRadius: '4px', cursor: 'pointer', fontSize: '14px',
  },
  smallBtn: {
    padding: '4px 10px', marginRight: '6px', backgroundColor: '#1976d2',
    color: '#fff', border: 'none', borderRadius: '4px', cursor: 'pointer', fontSize: '12px',
  },
  dangerBtn: {
    padding: '4px 10px', backgroundColor: '#d32f2f', color: '#fff',
    border: 'none', borderRadius: '4px', cursor: 'pointer', fontSize: '12px',
  },
  th: { padding: '8px', borderBottom: '2px solid #e0e0e0', textAlign: 'left', fontSize: '13px', color: '#666' },
  td: { padding: '8px', borderBottom: '1px solid #f0f0f0', fontSize: '14px' },
  codeBox: { position: 'relative' },
  pre: {
    backgroundColor: '#263238', color: '#eceff1', padding: '16px',
    borderRadius: '4px', fontSize: '12px', overflowX: 'auto', margin: 0,
  },
  copyBtn: {
    position: 'absolute', top: '8px', right: '8px', padding: '4px 10px',
    backgroundColor: '#fff', color: '#263238', border: 'none',
    borderRadius: '4px', cursor: 'pointer', fontSize: '12px',
  },
  previewBox: {
    width: '100%', height: '500px', border: '1px solid #e0e0e0',
    borderRadius: '4px', marginTop: '12px',
  },
};
