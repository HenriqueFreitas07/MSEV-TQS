import axios, { AxiosError } from "axios";
import type { LoginDTO, SignupDTO, User } from './types/user';
import type { Station } from './types/Station';
import type { Charger } from './types/Charger';
import type { Reservation } from './types/reservation';
import { showToast } from "./alerts";


const apiKey = import.meta.env.VITE_GOOGLE_MAPS_API_KEY

const googlePlace = axios.create({
  baseURL: "https://places.googleapis.com/v1/places",
  timeout: 10000,
  headers: {
    "Content-Type": "application/json",
    "X-Goog-Api-key": apiKey
  },
});

const api = axios.create({
  baseURL: import.meta.env.VITE_BACKEND_URL, // might change
  timeout: 10000,
  withCredentials: true,
  headers: {
    "Content-Type": "application/json",
  },
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response) {
      // Server responded with status outside 2xx
      console.error(
        "Response error:",
        error.response.status,
        error.response.data
      );
      showToast(`Error (${error.response.status}): ${error.response.data}`, "error", "top-end")
    } else if (error.request) {
      // Request made but no response
      console.error("Request error - no response received:", error.request);
      showToast(`Error: ${error.request}`, "error", "top-end")
    } else {
      // Request setup error
      console.error("Error setting up request:", error.message);
      showToast(`Error: ${error.message}`, "error", "top-end")
    }
    return Promise.reject(error);
  }
);

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
      const response = await api.get("/stations");
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
export const GoogleService = {
  autocompletePlaces: async (text: string) => {
    try {
      const response = await googlePlace.post(":autocomplete/", {
        input: text,
      });
      return response.data;
    } catch (error) {
      console.error("Error fetching restaurants:", error);
      throw error;
    }
  },
  getPlace: async (id: string) => {
    try {
      const response = await googlePlace.get("/" + id, {
        params: {
          key: apiKey,
          fields: 'id,displayName,location,formattedAddress',
          languageCode: 'en'
        },
      });
      return response.data;
    } catch (error) {
      console.error("Error fetching restaurants:", error);
      throw error;
    }
  },
};


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
  },

  lockCharger: async (id: string) => {
    try {
      api.patch(`/chargers/${id}/lock`);
    } catch (error) {
      console.error('Error locking charger:', error);
      throw error;
    }
  },

  unlockCharger: async (id: string) => {
    try {
      api.patch(`/chargers/${id}/unlock`);
    } catch (error) {
      console.error('Error unlocking charger:', error);
      throw error;
    }
  }
}
