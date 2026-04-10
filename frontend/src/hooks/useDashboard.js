import { useState, useCallback } from 'react';
import * as dashboardApi from '../api/dashboard';

export default function useDashboard() {
  const [summary, setSummary] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const fetchSummary = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const { data } = await dashboardApi.getSummary();
      setSummary(data.data);
    } catch (err) {
      setError(err.response?.data?.message || '대시보드 데이터 조회에 실패했습니다.');
    } finally {
      setLoading(false);
    }
  }, []);

  return { summary, loading, error, fetchSummary };
}
