import axios from "axios";
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
    } else if (error.request) {
      // Request made but no response
      console.error("Request error - no response received:", error.request);
    } else {
      // Request setup error
      console.error("Error setting up request:", error.message);
    }
    return Promise.reject(error);
  }
);
export const StationService={
  getAllStations:async()=>{
    try {
      const response = await api.get("/api/v1/stations/");
      return response.data;
    } catch (error) {
      console.error("Error fetching restaurants:", error);
      throw error;
    }
  },
  searchByName:async(name:string)=>{
    try {
      const response = await api.get("/api/v1/stations/search-by-name", {
        params:{
          "name":name
        }
      });
      return response.data;
    } catch (error) {
      console.error("Error fetching restaurants:", error);
      throw error;
    }
  },
  searchByAddress:async(add:string)=>{
    try {
      const response = await api.get("/api/v1/stations/search-by-address", {
        params:{
          "address":add
        }
      });
      return response.data;
    } catch (error) {
      console.error("Error fetching restaurants:", error);
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

