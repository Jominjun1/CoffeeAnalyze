import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

export const coffeeApi = {
  searchCoffee: async (name) => {
    try {
      const response = await api.get(`/getCoffee/search?name=${encodeURIComponent(name)}`);
      return response.data;
    } catch (error) {
      console.error('커피 검색 중 오류 발생:', error);
      return [];
    }
  }
};

export default api; 