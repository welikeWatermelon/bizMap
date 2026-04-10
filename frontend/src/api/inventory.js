import api from './axios';

export const getProducts = () =>
  api.get('/products');

export const getProductSizes = (productId) =>
  api.get(`/products/${productId}/sizes`);

export const searchStores = (params) =>
  api.get('/stores/search', { params });
