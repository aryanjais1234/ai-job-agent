import client from './client';

export const getProfile = () =>
  client.get('/users/profile').then(r => r.data);

export const updateProfile = (data) =>
  client.put('/users/profile', data).then(r => r.data);

export const getPreferences = () =>
  client.get('/users/preferences').then(r => r.data);

export const updatePreferences = (data) =>
  client.put('/users/preferences', data).then(r => r.data);
