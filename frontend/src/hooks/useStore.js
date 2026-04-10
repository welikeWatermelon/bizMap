import { useState, useCallback } from 'react';
import * as storesApi from '../api/stores';

export default function useStore() {
  const [store, setStore] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const fetchStore = useCallback(async (id) => {
    setLoading(true);
    setError(null);
    try {
      const { data } = await storesApi.getStore(id);
      setStore(data.data);
    } catch (err) {
      setError(err.response?.data?.message || '매장 조회에 실패했습니다.');
    } finally {
      setLoading(false);
    }
  }, []);

  const doUpdate = useCallback(async (id, updateData) => {
    setLoading(true);
    setError(null);
    try {
      const { data } = await storesApi.updateStore(id, updateData);
      setStore(data.data);
      return true;
    } catch (err) {
      setError(err.response?.data?.message || '매장 수정에 실패했습니다.');
      return false;
    } finally {
      setLoading(false);
    }
  }, []);

  const doDelete = useCallback(async (id) => {
    setLoading(true);
    setError(null);
    try {
      await storesApi.deleteStore(id);
      return true;
    } catch (err) {
      setError(err.response?.data?.message || '매장 삭제에 실패했습니다.');
      return false;
    } finally {
      setLoading(false);
    }
  }, []);

  return { store, loading, error, fetchStore, doUpdate, doDelete };
}
