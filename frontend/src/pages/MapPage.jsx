import { useEffect, useRef, useState } from 'react';
import Navbar from '../components/Navbar';
import useMap from '../hooks/useMap';
import useNearby from '../hooks/useNearby';
import * as storesApi from '../api/stores';

export default function MapPage() {
  const mapContainerRef = useRef(null);
  const { initMap, addMarkers, showInfoWindow, getCenter, loaded } = useMap();
  const { nearbyStores, loading: nearbyLoading, searchNearby } = useNearby();
  const [radius, setRadius] = useState(5);
  const [pins, setPins] = useState([]);

  useEffect(() => {
    if (mapContainerRef.current && !loaded) {
      initMap(mapContainerRef.current).then(() => loadPins());
    }
  }, []);

  const loadPins = async () => {
    try {
      const { data } = await storesApi.getMapPins();
      setPins(data.data);
    } catch {
      // ignore
    }
  };

  useEffect(() => {
    if (loaded && pins.length > 0) {
      addMarkers(pins, (pin, marker) => {
        showInfoWindow(
          `<div><strong>${pin.name}</strong></div>`,
          marker
        );
      });
    }
  }, [loaded, pins]);

  const handleNearbySearch = () => {
    const center = getCenter();
    if (center) {
      searchNearby(center.lat, center.lng, radius);
    }
  };

  return (
    <div style={{ height: '100vh', display: 'flex', flexDirection: 'column' }}>
      <Navbar />
      <div style={{ display: 'flex', flex: 1 }}>
        <div ref={mapContainerRef} style={{ flex: 1 }} />
        <div style={styles.panel}>
          <h3 style={{ margin: '0 0 16px' }}>반경 검색</h3>
          <div style={{ marginBottom: '12px' }}>
            <label style={{ fontSize: '13px', color: '#666' }}>반경 (km)</label>
            <input
              type="number" value={radius}
              onChange={(e) => setRadius(Number(e.target.value))}
              style={styles.input} min="1" max="50"
            />
          </div>
          <button onClick={handleNearbySearch} disabled={nearbyLoading} style={styles.button}>
            {nearbyLoading ? '검색 중...' : '현재 지도 중심 기준 검색'}
          </button>

          {nearbyStores.length > 0 && (
            <div style={{ marginTop: '16px' }}>
              <h4 style={{ margin: '0 0 8px' }}>검색 결과 ({nearbyStores.length})</h4>
              {nearbyStores.map((s) => (
                <div key={s.id} style={styles.resultItem}>
                  <strong>{s.name}</strong>
                  <span style={{ fontSize: '12px', color: '#666' }}>{s.distance}km</span>
                  <p style={{ margin: '4px 0 0', fontSize: '12px', color: '#999' }}>{s.address}</p>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

const styles = {
  panel: {
    width: '300px', padding: '16px', overflowY: 'auto',
    borderLeft: '1px solid #e0e0e0', backgroundColor: '#fafafa',
  },
  input: {
    width: '100%', padding: '8px', border: '1px solid #ddd',
    borderRadius: '4px', fontSize: '14px', boxSizing: 'border-box', marginTop: '4px',
  },
  button: {
    width: '100%', padding: '10px', backgroundColor: '#1976d2', color: '#fff',
    border: 'none', borderRadius: '4px', cursor: 'pointer', fontSize: '14px',
  },
  resultItem: {
    padding: '8px', borderBottom: '1px solid #eee',
    display: 'flex', flexDirection: 'column',
  },
};
