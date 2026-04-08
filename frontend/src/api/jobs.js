import client from './client';

export const getJobs = (page = 0, size = 10) =>
  client.get('/jobs', { params: { page, size } }).then(r => r.data);

export const getJob = (id) =>
  client.get(`/jobs/${id}`).then(r => r.data);

export const getMatches = (page = 0, size = 10) =>
  client.get('/jobs/matches', { params: { page, size } }).then(r => r.data);
