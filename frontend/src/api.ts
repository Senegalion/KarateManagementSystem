import axios from "axios";

export const API = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || "http://localhost:9191",
});

// API.interceptors.request.use((config) => {
//   const token = localStorage.getItem("token");
//   if (token) {
//     config.headers = config.headers ?? {};
//     config.headers.Authorization = `Bearer ${token}`;
//   }
//   return config;
// });
