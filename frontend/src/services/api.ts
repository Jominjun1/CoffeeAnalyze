import axios from 'axios';
import type { CoffeeDTO } from '@/types/coffee';

const API_BASE_URL = 'http://localhost:8080';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

export const coffeeApi = {
  // 커피 검색
  searchCoffee: async (name: string): Promise<CoffeeDTO[]> => {
    try {
      const response = await api.get(`/getCoffee/search?name=${encodeURIComponent(name)}`);
      return response.data;
    } catch (error) {
      console.error('커피 검색 중 오류 발생:', error);
      return [];
    }
  },

  // 모든 커피 브랜드 검색
  searchAllBrands: async (name: string): Promise<CoffeeDTO[]> => {
    try {
      const response = await api.get(`/getCoffee/search?name=${encodeURIComponent(name)}`);
      return response.data;
    } catch (error) {
      console.error('전체 브랜드 검색 중 오류 발생:', error);
      return [];
    }
  }
};

export default api; 