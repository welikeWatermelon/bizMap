import { useEffect, useRef, useState, useCallback } from 'react';
import useInventorySearch from '../hooks/useInventorySearch';
import useMap from '../hooks/useMap';
import useRoute from '../hooks/useRoute';

const DEFAULT_CENTER = { lat: 37.5665, lng: 126.9780 };

export default function FindStorePage() {
  const { products, sizes, results, loading, fetchProducts, fetchSizes, search } = useInventorySearch();
  const { mapInstance, initMap, addMarkers, showInfoWindow, clearMarkers } = useMap();
  const { selectedStore, urls, selectStore, clearRoute } = useRoute();
  const mapContainerRef = useRef(null);

  const [selectedProduct, setSelectedProduct] = useState('');
  const [selectedSize, setSelectedSize] = useState('');
  const [radius, setRadius] = useState(30);
  const [debouncedRadius, setDebouncedRadius] = useState(30);
  const [userLocation, setUserLocation] = useState(DEFAULT_CENTER);
  const [mapReady, setMapReady] = useState(false);
  const [clickedStoreId, setClickedStoreId] = useState(null);

  useEffect(() => {
    fetchProducts();
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (pos) => {
          const lat = pos.coords.latitude;
          const lng = pos.coords.longitude;
          if (lat >= 37.4 && lat <= 37.7 && lng >= 126.7 && lng <= 127.2) {
            setUserLocation({ lat, lng });
          } else {
            setUserLocation(DEFAULT_CENTER);
          }
        },
        () => setUserLocation(DEFAULT_CENTER)
      );
    }
  }, [fetchProducts]);

  useEffect(() => {
    if (mapContainerRef.current && !mapReady) {
      initMap(mapContainerRef.current, { center: userLocation, zoom: 12 });
      setMapReady(true);
    } else if (mapReady && mapInstance.current) {
      mapInstance.current.panTo(userLocation);
    }
  }, [userLocation, initMap, mapReady, mapInstance]);

  useEffect(() => {
    const timer = setTimeout(() => setDebouncedRadius(radius), 500);
    return () => clearTimeout(timer);
  }, [radius]);

  const handleProductChange = (e) => {
    const productId = e.target.value;
    setSelectedProduct(productId);
    setSelectedSize('');
    setClickedStoreId(null);
    clearRoute();
    clearMarkers();
    if (productId) fetchSizes(Number(productId));
  };

  const handleSizeChange = (e) => {
    setSelectedSize(e.target.value);
    setClickedStoreId(null);
    clearRoute();
  };

  const handleSearch = useCallback(() => {
    if (!selectedProduct || !selectedSize) return;
    search({
      lat: userLocation.lat,
      lng: userLocation.lng,
      productId: Number(selectedProduct),
      size: selectedSize,
      radius: debouncedRadius,
    });
  }, [selectedProduct, selectedSize, userLocation, debouncedRadius, search]);

  useEffect(() => {
    if (selectedProduct && selectedSize) {
      handleSearch();
    }
  }, [selectedProduct, selectedSize, debouncedRadius, handleSearch]);

  useEffect(() => {
    if (!mapReady) return;
    clearMarkers();
    if (results.length > 0) {
      const pins = results.map((r) => ({
        id: r.storeId,
        name: r.storeName,
        latitude: r.latitude,
        longitude: r.longitude,
      }));
      addMarkers(pins, (pin, marker) => {
        const store = results.find((r) => r.storeId === pin.id);
        showInfoWindow(
          `<div style="min-width:180px">
            <strong>${store.storeName}</strong><br/>
            <span style="color:#666">${store.address}</span><br/>
            <span>재고: <b>${store.quantity}개</b></span><br/>
            <span>거리: ${store.distance}km</span>
          </div>`,
          marker
        );
      });
    }
  }, [results, mapReady, addMarkers, showInfoWindow, clearMarkers]);

  const handleStoreClick = (store) => {
    if (!mapInstance.current) return;
    setClickedStoreId(store.storeId);
    mapInstance.current.panTo({ lat: store.latitude, lng: store.longitude });
    mapInstance.current.setZoom(15);

    selectStore(store.storeName, store.address);
  };

  const handleOpenMap = (travelMode) => {
    if (urls) {
      window.open(travelMode === 'walking' ? urls.walking : urls.driving, '_blank');
    }
  };

  const handleClearRoute = () => {
    clearRoute();
    setClickedStoreId(null);
  };

  return (
    <div style={styles.container}>
      <div style={styles.header}>
        <h2 style={styles.title}>매장 찾기</h2>
        <div style={styles.filters}>
          <select value={selectedProduct} onChange={handleProductChange} style={styles.select}>
            <option value="">상품 선택</option>
            {products.map((p) => (
              <option key={p.id} value={p.id}>{p.name}</option>
            ))}
          </select>
          <select value={selectedSize} onChange={handleSizeChange} style={styles.select} disabled={!selectedProduct}>
            <option value="">사이즈 선택</option>
            {sizes.map((s) => (
              <option key={s.id} value={s.size}>{s.size}</option>
            ))}
          </select>
          <div style={styles.radiusWrap}>
            <input
              type="number"
              value={radius}
              onChange={(e) => setRadius(Number(e.target.value) || 1)}
              min="1"
              max="100"
              style={styles.radiusInput}
            />
            <span style={styles.radiusLabel}>km</span>
          </div>
        </div>
      </div>

      <div style={styles.content}>
        <div ref={mapContainerRef} style={styles.map} />
        <div style={styles.list}>
          <h3 style={styles.listTitle}>
            {loading ? '검색 중...' : results.length > 0 ? `재고 보유 매장 (${results.length}개)` : '매장을 검색해 주세요'}
          </h3>
          {results.map((store) => (
            <div
              key={store.storeId}
              style={{
                ...styles.card,
                ...(clickedStoreId === store.storeId ? styles.cardSelected : {}),
              }}
              onClick={() => handleStoreClick(store)}
            >
              <div style={styles.cardTop}>
                <strong style={styles.storeName}>{store.storeName}</strong>
                <span style={styles.distance}>{store.distance}km</span>
              </div>
              <div style={styles.address}>{store.address}</div>
              <div style={styles.quantity}>재고 {store.quantity}개</div>
            </div>
          ))}
          {!loading && selectedProduct && selectedSize && results.length === 0 && (
            <div style={styles.empty}>반경 {radius}km 내 재고 보유 매장이 없습니다.</div>
          )}

          {selectedStore && (
            <div style={styles.routePanel}>
              <div style={styles.routeHeader}>경로 안내</div>

              <div style={styles.routeDetail}>
                <div style={styles.routeRow}>
                  <span style={styles.routeLabel}>출발</span>
                  <span>내 현재 위치</span>
                </div>
                <div style={styles.routeRow}>
                  <span style={styles.routeLabel}>도착</span>
                  <span>{selectedStore.storeName}</span>
                </div>
                <div style={{ fontSize: '12px', color: '#888', marginBottom: '12px' }}>{selectedStore.address}</div>
              </div>

              <div style={styles.linkButtons}>
                <button style={styles.drivingBtn} onClick={() => handleOpenMap('driving')}>
                  자동차로 길찾기
                </button>
                <button style={styles.walkingBtn} onClick={() => handleOpenMap('walking')}>
                  도보로 길찾기
                </button>
              </div>

              <button style={styles.clearBtn} onClick={handleClearRoute}>선택 해제</button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

const styles = {
  container: { display: 'flex', flexDirection: 'column', height: '100vh', backgroundColor: '#f5f5f5' },
  header: {
    display: 'flex', justifyContent: 'space-between', alignItems: 'center',
    padding: '16px 24px', backgroundColor: '#1976d2', color: '#fff',
  },
  title: { margin: 0, fontSize: '20px' },
  filters: { display: 'flex', gap: '12px', alignItems: 'center' },
  select: {
    padding: '8px 12px', borderRadius: '4px', border: 'none',
    fontSize: '14px', minWidth: '160px', cursor: 'pointer',
  },
  content: { display: 'flex', flex: 1, overflow: 'hidden' },
  map: { flex: 1, minHeight: '400px' },
  list: {
    width: '360px', overflowY: 'auto', padding: '16px',
    backgroundColor: '#fff', borderLeft: '1px solid #e0e0e0',
  },
  listTitle: { margin: '0 0 16px 0', fontSize: '16px', color: '#333' },
  card: {
    padding: '14px', marginBottom: '10px', borderRadius: '8px',
    border: '1px solid #e0e0e0', cursor: 'pointer', transition: 'box-shadow 0.2s',
  },
  cardSelected: {
    border: '2px solid #1976d2', backgroundColor: '#e3f2fd',
  },
  cardTop: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '6px' },
  storeName: { fontSize: '15px', color: '#333' },
  distance: { fontSize: '13px', color: '#1976d2', fontWeight: 'bold' },
  address: { fontSize: '13px', color: '#666', marginBottom: '4px' },
  quantity: { fontSize: '13px', color: '#4caf50', fontWeight: 'bold' },
  radiusWrap: { display: 'flex', alignItems: 'center', gap: '4px' },
  radiusInput: {
    width: '60px', padding: '8px', borderRadius: '4px', border: 'none',
    fontSize: '14px', textAlign: 'center',
  },
  radiusLabel: { color: '#fff', fontSize: '14px' },
  empty: { textAlign: 'center', color: '#999', padding: '40px 0', fontSize: '14px' },
  routePanel: {
    marginTop: '16px', padding: '16px', borderRadius: '8px',
    border: '1px solid #e0e0e0', backgroundColor: '#fafafa',
  },
  routeHeader: { fontSize: '15px', fontWeight: 'bold', color: '#333', marginBottom: '12px' },
  routeDetail: { marginBottom: '12px' },
  routeRow: { display: 'flex', gap: '8px', alignItems: 'center', fontSize: '13px', marginBottom: '4px' },
  routeLabel: {
    display: 'inline-block', width: '36px', fontWeight: 'bold', color: '#555',
    fontSize: '12px', flexShrink: 0,
  },
  linkButtons: { display: 'flex', gap: '8px', marginBottom: '12px' },
  drivingBtn: {
    flex: 1, padding: '10px', backgroundColor: '#1976d2', color: '#fff',
    border: 'none', borderRadius: '4px', cursor: 'pointer',
    fontSize: '13px', fontWeight: 'bold',
  },
  walkingBtn: {
    flex: 1, padding: '10px', backgroundColor: '#4caf50', color: '#fff',
    border: 'none', borderRadius: '4px', cursor: 'pointer',
    fontSize: '13px', fontWeight: 'bold',
  },
  clearBtn: {
    width: '100%', padding: '10px', backgroundColor: '#fff', color: '#888',
    border: '1px solid #ddd', borderRadius: '4px', cursor: 'pointer',
    fontSize: '13px', transition: 'all 0.2s',
  },
};
