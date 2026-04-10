import api from './axios';

export const getStores = (params) =>
  api.get('/stores', { params });

export const createStore = (data) =>
  api.post('/stores', data);

export const getStore = (id) =>
  api.get(`/stores/${id}`);

export const updateStore = (id, data) =>
  api.put(`/stores/${id}`, data);

export const deleteStore = (id) =>
  api.delete(`/stores/${id}`);

export const getMapPins = () =>
  api.get('/stores/map');

export const getNearby = (lat, lng, radius) =>
  api.get('/stores/nearby', { params: { lat, lng, radius } });
