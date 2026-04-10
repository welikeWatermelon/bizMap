import { useState, useCallback } from 'react';
import { getProducts, getProductSizes, searchStores } from '../api/inventory';

export default function useInventorySearch() {
  const [products, setProducts] = useState([]);
  const [sizes, setSizes] = useState([]);
  const [results, setResults] = useState([]);
  const [loading, setLoading] = useState(false);

  const fetchProducts = useCallback(async () => {
    try {
      const res = await getProducts();
      setProducts(res.data.data);
    } catch (err) {
      console.error('상품 목록 조회 실패', err);
    }
  }, []);

  const fetchSizes = useCallback(async (productId) => {
    try {
      setSizes([]);
      const res = await getProductSizes(productId);
      setSizes(res.data.data);
    } catch (err) {
      console.error('사이즈 조회 실패', err);
    }
  }, []);

  const search = useCallback(async ({ lat, lng, productId, size, radius = 10 }) => {
    setLoading(true);
    try {
      const res = await searchStores({ lat, lng, productId, size, radius });
      setResults(res.data.data);
    } catch (err) {
      console.error('매장 검색 실패', err);
      setResults([]);
    } finally {
      setLoading(false);
    }
  }, []);

  return { products, sizes, results, loading, fetchProducts, fetchSizes, search };
}
