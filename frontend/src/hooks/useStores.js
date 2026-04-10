import { useState, useCallback } from 'react';
import * as storesApi from '../api/stores';

export default function useStores() {
  const [stores, setStores] = useState([]);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const fetchStores = useCallback(async (params = {}) => {
    setLoading(true);
    setError(null);
    try {
      const { data } = await storesApi.getStores(params);
      setStores(data.data.content);
      setTotalPages(data.data.totalPages);
    } catch (err) {
      setError(err.response?.data?.message || '매장 목록 조회에 실패했습니다.');
    } finally {
      setLoading(false);
    }
  }, []);

  return { stores, totalPages, loading, error, fetchStores };
}
