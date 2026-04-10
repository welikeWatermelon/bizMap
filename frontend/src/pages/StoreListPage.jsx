import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Navbar from '../components/Navbar';
import useStores from '../hooks/useStores';

export default function StoreListPage() {
  const navigate = useNavigate();
  const { stores, totalPages, loading, error, fetchStores } = useStores();
  const [keyword, setKeyword] = useState('');
  const [category, setCategory] = useState('');
  const [page, setPage] = useState(0);

  useEffect(() => {
    const params = { page, size: 10 };
    if (keyword) params.keyword = keyword;
    if (category) params.category = category;
    fetchStores(params);
  }, [page, fetchStores]);

  const handleSearch = () => {
    setPage(0);
    const params = { page: 0, size: 10 };
    if (keyword) params.keyword = keyword;
    if (category) params.category = category;
    fetchStores(params);
  };

  return (
    <div style={{ minHeight: '100vh', backgroundColor: '#f5f5f5' }}>
      <Navbar />
      <div style={styles.container}>
        <div style={styles.header}>
          <h2 style={{ margin: 0 }}>매장 관리</h2>
          <button onClick={() => navigate('/stores/new')} style={styles.addBtn}>+ 매장 등록</button>
        </div>

        <div style={styles.filterBar}>
          <input
            type="text" placeholder="매장명 검색" value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
            style={styles.searchInput}
          />
          <select value={category} onChange={(e) => setCategory(e.target.value)} style={styles.select}>
            <option value="">전체 카테고리</option>
            <option value="RETAIL">소매</option>
            <option value="FOOD">음식</option>
            <option value="SERVICE">서비스</option>
            <option value="OTHER">기타</option>
          </select>
          <button onClick={handleSearch} style={styles.searchBtn}>검색</button>
        </div>

        {error && <p style={{ color: '#d32f2f' }}>{error}</p>}

        <table style={styles.table}>
          <thead>
            <tr>
              <th style={styles.th}>ID</th>
              <th style={styles.th}>매장명</th>
              <th style={styles.th}>카테고리</th>
              <th style={styles.th}>주소</th>
              <th style={styles.th}>전화번호</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr><td colSpan="5" style={styles.td}>로딩 중...</td></tr>
            ) : stores.length === 0 ? (
              <tr><td colSpan="5" style={styles.td}>등록된 매장이 없습니다.</td></tr>
            ) : (
              stores.map((s) => (
                <tr key={s.id} onClick={() => navigate(`/stores/${s.id}`)} style={{ cursor: 'pointer' }}>
                  <td style={styles.td}>{s.id}</td>
                  <td style={styles.td}>{s.name}</td>
                  <td style={styles.td}>{s.category}</td>
                  <td style={styles.td}>{s.address}</td>
                  <td style={styles.td}>{s.phone || '-'}</td>
                </tr>
              ))
            )}
          </tbody>
        </table>

        <div style={styles.pagination}>
          <button onClick={() => setPage(p => Math.max(0, p - 1))} disabled={page === 0}>이전</button>
          <span style={{ margin: '0 12px' }}>{page + 1} / {Math.max(1, totalPages)}</span>
          <button onClick={() => setPage(p => p + 1)} disabled={page >= totalPages - 1}>다음</button>
        </div>
      </div>
    </div>
  );
}

const styles = {
  container: { maxWidth: '1000px', margin: '0 auto', padding: '24px' },
  header: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' },
  addBtn: {
    padding: '8px 16px', backgroundColor: '#1976d2', color: '#fff',
    border: 'none', borderRadius: '4px', cursor: 'pointer',
  },
  filterBar: { display: 'flex', gap: '8px', marginBottom: '16px' },
  searchInput: { flex: 1, padding: '8px', border: '1px solid #ddd', borderRadius: '4px' },
  select: { padding: '8px', border: '1px solid #ddd', borderRadius: '4px' },
  searchBtn: {
    padding: '8px 16px', backgroundColor: '#1976d2', color: '#fff',
    border: 'none', borderRadius: '4px', cursor: 'pointer',
  },
  table: { width: '100%', borderCollapse: 'collapse', backgroundColor: '#fff' },
  th: { padding: '12px', borderBottom: '2px solid #e0e0e0', textAlign: 'left', fontSize: '13px', color: '#666' },
  td: { padding: '12px', borderBottom: '1px solid #f0f0f0', fontSize: '14px' },
  pagination: { display: 'flex', justifyContent: 'center', alignItems: 'center', marginTop: '16px' },
};
