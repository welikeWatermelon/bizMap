import { useState, useCallback } from 'react';
import * as storesApi from '../api/stores';

export default function useNearby() {
  const [nearbyStores, setNearbyStores] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const searchNearby = useCallback(async (lat, lng, radius) => {
    setLoading(true);
    setError(null);
    try {
      const { data } = await storesApi.getNearby(lat, lng, radius);
      setNearbyStores(data.data);
    } catch (err) {
      setError(err.response?.data?.message || '반경 검색에 실패했습니다.');
    } finally {
      setLoading(false);
    }
  }, []);

  return { nearbyStores, loading, error, searchNearby };
}
