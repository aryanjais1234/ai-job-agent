import client from './client';

export const getApplications = (status, page = 0, size = 10) => {
  const params = { page, size };
  if (status) params.status = status;
  return client.get('/applications', { params }).then(r => r.data);
};

export const createApplication = (jobId) =>
  client.post('/applications', { jobId }).then(r => r.data);

export const updateApplication = (id, data) =>
  client.put(`/applications/${id}`, data).then(r => r.data);

export const deleteApplication = (id) =>
  client.delete(`/applications/${id}`);
