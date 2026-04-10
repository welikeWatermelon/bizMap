import api from './axios';

export const getWidgetKeys = () =>
  api.get('/widget-keys');

export const createWidgetKey = (data) =>
  api.post('/widget-keys', data);

export const deleteWidgetKey = (id) =>
  api.delete(`/widget-keys/${id}`);
