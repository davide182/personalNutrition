import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080/api',
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  console.log(`📨 [${config.method?.toUpperCase()}] ${config.url} - Token:`, token ? '✅ presente' : '❌ mancante');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  } else {
    console.warn('⚠️ Nessun token trovato per la richiesta:', config.url);
  }
  return config;
}, (error) => {
  return Promise.reject(error);
});

export default api;