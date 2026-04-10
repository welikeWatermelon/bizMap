import { useState, useRef, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Navbar from '../components/Navbar';
import AddressSearchInput from '../components/AddressSearchInput';
import useMap from '../hooks/useMap';
import * as storesApi from '../api/stores';

export default function StoreFormPage() {
  const navigate = useNavigate();
  const mapContainerRef = useRef(null);
  const { initMap, addMarkers, loaded } = useMap();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [form, setForm] = useState({
    name: '', category: 'RETAIL', address: '',
    latitude: '', longitude: '', phone: '', openTime: '', closeTime: '',
  });

  useEffect(() => {
    if (mapContainerRef.current && !loaded) {
      initMap(mapContainerRef.current, { zoom: 13 });
    }
  }, []);

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleAddressSelect = ({ address, lat, lng }) => {
    setForm((prev) => ({ ...prev, address, latitude: lat, longitude: lng }));
    if (loaded) {
      addMarkers([{ id: 0, name: form.name || '새 매장', latitude: lat, longitude: lng }]);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    try {
      const payload = {
        ...form,
        latitude: Number(form.latitude),
        longitude: Number(form.longitude),
        openTime: form.openTime || null,
        closeTime: form.closeTime || null,
      };
      await storesApi.createStore(payload);
      navigate('/stores');
    } catch (err) {
      setError(err.response?.data?.message || '매장 등록에 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ minHeight: '100vh', backgroundColor: '#f5f5f5' }}>
      <Navbar />
      <div style={styles.container}>
        <h2>매장 등록</h2>
        <div style={{ display: 'flex', gap: '24px' }}>
          <form onSubmit={handleSubmit} style={{ flex: 1 }}>
            <div style={styles.field}>
              <label>매장명 *</label>
              <input name="name" value={form.name} onChange={handleChange} style={styles.input} required />
            </div>
            <div style={styles.field}>
              <label>카테고리 *</label>
              <select name="category" value={form.category} onChange={handleChange} style={styles.input}>
                <option value="RETAIL">소매</option>
                <option value="FOOD">음식</option>
                <option value="SERVICE">서비스</option>
                <option value="OTHER">기타</option>
              </select>
            </div>
            <div style={styles.field}>
              <label>주소 *</label>
              <AddressSearchInput
                onSelect={handleAddressSelect}
                placeholder="주소를 검색하세요"
                initialValue={form.address}
              />
            </div>
            <div style={styles.field}>
              <label>전화번호</label>
              <input name="phone" value={form.phone} onChange={handleChange} style={styles.input} />
            </div>
            <div style={{ display: 'flex', gap: '8px' }}>
              <div style={{ ...styles.field, flex: 1 }}>
                <label>오픈시간</label>
                <input name="openTime" type="time" value={form.openTime} onChange={handleChange} style={styles.input} />
              </div>
              <div style={{ ...styles.field, flex: 1 }}>
                <label>마감시간</label>
                <input name="closeTime" type="time" value={form.closeTime} onChange={handleChange} style={styles.input} />
              </div>
            </div>
            {error && <p style={{ color: '#d32f2f', fontSize: '13px' }}>{error}</p>}
            <button type="submit" disabled={loading} style={styles.submitBtn}>
              {loading ? '등록 중...' : '등록'}
            </button>
          </form>
          <div ref={mapContainerRef} style={{ flex: 1, minHeight: '400px', borderRadius: '8px' }} />
        </div>
      </div>
    </div>
  );
}

const styles = {
  container: { maxWidth: '1000px', margin: '0 auto', padding: '24px' },
  field: { marginBottom: '12px' },
  input: {
    width: '100%', padding: '8px', border: '1px solid #ddd',
    borderRadius: '4px', fontSize: '14px', boxSizing: 'border-box',
  },
  submitBtn: {
    width: '100%', padding: '12px', backgroundColor: '#1976d2', color: '#fff',
    border: 'none', borderRadius: '4px', fontSize: '16px', cursor: 'pointer', marginTop: '8px',
  },
};
