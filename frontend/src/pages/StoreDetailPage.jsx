import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import Navbar from '../components/Navbar';
import useStore from '../hooks/useStore';

export default function StoreDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { store, loading, error, fetchStore, doUpdate, doDelete } = useStore();
  const [editing, setEditing] = useState(false);
  const [form, setForm] = useState({});

  useEffect(() => { fetchStore(id); }, [id, fetchStore]);

  useEffect(() => {
    if (store) {
      setForm({
        name: store.name, category: store.category, address: store.address,
        latitude: store.latitude, longitude: store.longitude,
        phone: store.phone || '', openTime: store.openTime || '', closeTime: store.closeTime || '',
      });
    }
  }, [store]);

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSave = async () => {
    const success = await doUpdate(id, {
      ...form,
      latitude: Number(form.latitude),
      longitude: Number(form.longitude),
      openTime: form.openTime || null,
      closeTime: form.closeTime || null,
    });
    if (success) setEditing(false);
  };

  const handleDelete = async () => {
    if (!window.confirm('정말 삭제하시겠습니까?')) return;
    const success = await doDelete(id);
    if (success) navigate('/stores');
  };

  if (loading && !store) return <div><Navbar /><p style={{ padding: '24px' }}>로딩 중...</p></div>;
  if (error && !store) return <div><Navbar /><p style={{ padding: '24px', color: '#d32f2f' }}>{error}</p></div>;
  if (!store) return null;

  return (
    <div style={{ minHeight: '100vh', backgroundColor: '#f5f5f5' }}>
      <Navbar />
      <div style={styles.container}>
        <div style={styles.header}>
          <h2 style={{ margin: 0 }}>{store.name}</h2>
          <div style={{ display: 'flex', gap: '8px' }}>
            {!editing ? (
              <>
                <button onClick={() => setEditing(true)} style={styles.editBtn}>수정</button>
                <button onClick={handleDelete} style={styles.deleteBtn}>삭제</button>
              </>
            ) : (
              <>
                <button onClick={handleSave} style={styles.editBtn}>저장</button>
                <button onClick={() => setEditing(false)} style={styles.cancelBtn}>취소</button>
              </>
            )}
          </div>
        </div>

        <div style={styles.card}>
          {['name', 'category', 'address', 'latitude', 'longitude', 'phone', 'openTime', 'closeTime'].map((field) => (
            <div key={field} style={styles.row}>
              <span style={styles.label}>{fieldLabels[field]}</span>
              {editing ? (
                field === 'category' ? (
                  <select name={field} value={form[field]} onChange={handleChange} style={styles.input}>
                    <option value="RETAIL">소매</option>
                    <option value="FOOD">음식</option>
                    <option value="SERVICE">서비스</option>
                    <option value="OTHER">기타</option>
                  </select>
                ) : (
                  <input name={field} value={form[field]} onChange={handleChange} style={styles.input}
                    type={field.includes('Time') ? 'time' : 'text'} />
                )
              ) : (
                <span>{store[field] || '-'}</span>
              )}
            </div>
          ))}
          {error && <p style={{ color: '#d32f2f', fontSize: '13px', marginTop: '8px' }}>{error}</p>}
        </div>
      </div>
    </div>
  );
}

const fieldLabels = {
  name: '매장명', category: '카테고리', address: '주소',
  latitude: '위도', longitude: '경도', phone: '전화번호',
  openTime: '오픈시간', closeTime: '마감시간',
};

const styles = {
  container: { maxWidth: '700px', margin: '0 auto', padding: '24px' },
  header: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' },
  card: { backgroundColor: '#fff', padding: '24px', borderRadius: '8px', boxShadow: '0 1px 4px rgba(0,0,0,0.1)' },
  row: { display: 'flex', alignItems: 'center', padding: '10px 0', borderBottom: '1px solid #f0f0f0' },
  label: { width: '100px', fontWeight: 'bold', fontSize: '13px', color: '#666' },
  input: { flex: 1, padding: '6px 8px', border: '1px solid #ddd', borderRadius: '4px', fontSize: '14px' },
  editBtn: { padding: '6px 16px', backgroundColor: '#1976d2', color: '#fff', border: 'none', borderRadius: '4px', cursor: 'pointer' },
  deleteBtn: { padding: '6px 16px', backgroundColor: '#d32f2f', color: '#fff', border: 'none', borderRadius: '4px', cursor: 'pointer' },
  cancelBtn: { padding: '6px 16px', backgroundColor: '#999', color: '#fff', border: 'none', borderRadius: '4px', cursor: 'pointer' },
};
