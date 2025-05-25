import axios, { AxiosError } from 'axios';
import type { LoginDTO, SignupDTO, User } from './types/user';
import type { Station } from './types/Station';
import type { Charger } from './types/Charger';
import type { Reservation } from './types/reservation';

const api = axios.create({
  baseURL: import.meta.env.VITE_BACKEND_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true
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
//
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

export const AuthService = {
  signup: async (dto: SignupDTO) => {
    try {
      const { data } = await api.post("/signup", dto);
      return data;
    } catch (error) {
      const err = error as AxiosError;

      throw err.response?.data;
    }
  },

  login: async (dto: LoginDTO) => {
    try {
      const { data } = await api.post("/login", dto);
      return data;
    } catch (error) {
      const err = error as AxiosError;

      throw err.response?.data;
    }
  },

  logout: async () => {
    try {
      const { data } = await api.post("/logout");
      return data;
    } catch (error) {
      console.error("Error logging out", error);
    }
  }
}

export const UserService = {
  getSelfUser: async (): Promise<User> => {
    const { data } = await api.get("/users/self");

    return data;
  }
}


export const StationService = {
  getAllStations: async () => {
    try {
      const response = await api.get("/stations/");
      return response.data as Station[];
    } catch (error) {
      console.error('Error fetching stations:', error);
      throw error;
    }
  },
  getStationById: async (id: string) => {
    try {
      const response = await api.get(`/stations/${id}`);
      return response.data as Station;
    } catch (error) {
      console.error('Error fetching station:', error);
      throw error;
    }
  },
  searchStationByName: async (name: string) => {
    try {
      const response = await api.get("/stations/search-by-name", { params: { name } });
      return response.data as Station[];
    } catch (error) {
      console.error('Error fetching station:', error);
      throw error;
    }
  },
  searchStationByAddress: async (address: string) => {
    try {
      const response = await api.get("/stations/search-by-address", { params: { address } });
      return response.data as Station[];
    } catch (error) {
      console.error('Error fetching station:', error);
      throw error;
    }
  }
}

export const ChargerService = {
  getChargerByStation: async (id: string) => {
    try {
      const response = await api.get(`/chargers/station/${id}`);
      return response.data as Charger[];
    } catch (error) {
      console.error('Error fetching chargers:', error);
      throw error;
    }
  },

  getChargerById: async (id: string) => {
    try {
      const response = await api.get(`/chargers/${id}`);
      return response.data as Charger;
    } catch (error) {
      console.error('Error fetching charger:', error);
      throw error;
    }
  },

  getChargerReservationsForNextDays: async (id: string): Promise<Reservation[]> => {
    const { data } = await api.get(`/chargers/${id}/reservations`)

    return data;
  }
}
