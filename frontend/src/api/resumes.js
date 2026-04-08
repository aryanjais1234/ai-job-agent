import client from './client';

export const uploadResume = (file) => {
  const formData = new FormData();
  formData.append('file', file);
  return client.post('/resumes', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  }).then(r => r.data);
};

export const getResumes = () =>
  client.get('/resumes').then(r => r.data);

export const getResume = (id) =>
  client.get(`/resumes/${id}`).then(r => r.data);

export const setPrimary = (id) =>
  client.put(`/resumes/${id}/primary`).then(r => r.data);

export const deleteResume = (id) =>
  client.delete(`/resumes/${id}`);
