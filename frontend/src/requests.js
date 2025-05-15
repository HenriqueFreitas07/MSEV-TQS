import axios from 'axios';

const api = axios.create({
  baseURL: 'http://backend:8080/api', // might change
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  }
});

api.interceptors.response.use(
  response => response,
  error => {
    if (error.response) {
      // Server responded with status outside 2xx
      console.error('Response error:', error.response.status, error.response.data);
    } else if (error.request) {
      // Request made but no response
      console.error('Request error - no response received:', error.request);
    } else {
      // Request setup error
      console.error('Error setting up request:', error.message);
    }
    return Promise.reject(error);
  }
);


// How to declare the requests to the backend endpoints 
// export const ServiceName = {
//   method: async () => {
//     try {
//       const response = await api.get('/restaurants');
//       return response.data;
//     } catch (error) {
//       console.error('Error fetching restaurants:', error);
//       throw error;
//     }
//   },
  
//   otherMethod: async (id) => {
//     try {
//       const response = await api.get(`/restaurants/${id}`);
//       return response.data;
//     } catch (error) {
//       console.error(`Error fetching restaurant with id ${id}:`, error);
//       throw error;
//     }
//   }
// };