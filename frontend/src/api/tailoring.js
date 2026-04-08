import client from './client';

export const tailorResume = (jobId) =>
  client.post(`/tailor/${jobId}`).then(r => r.data);

export const getTailoredResume = (id) =>
  client.get(`/tailor/${id}`).then(r => r.data);

export const generateCoverLetter = (jobId) =>
  client.post(`/tailor/cover-letter/${jobId}`).then(r => r.data);

export const getCoverLetter = (id) =>
  client.get(`/tailor/cover-letter/${id}/view`).then(r => r.data);

export const downloadResumePdf = (tailoredId) =>
  client.get(`/documents/resume/${tailoredId}`, { responseType: 'blob' }).then(r => r.data);

export const downloadCoverLetterPdf = (coverId) =>
  client.get(`/documents/cover-letter/${coverId}`, { responseType: 'blob' }).then(r => r.data);
