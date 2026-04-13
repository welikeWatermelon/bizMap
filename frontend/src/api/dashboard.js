import api from './axios';

export const getSummary = () =>
  api.get('/dashboard/summary');

export const getUsageSummary = (days = 7) =>
  api.get(`/dashboard/usage?days=${days}`);
