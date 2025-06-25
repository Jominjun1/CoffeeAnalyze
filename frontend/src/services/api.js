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
  },
  
  updateCoffees: async (coffeeUpdates) => {
    try {
      // 로컬 스토리지에서 토큰 가져오기
      const token = localStorage.getItem('adminToken');
      
      if (!token) {
        throw new Error('관리자 로그인이 필요합니다.');
      }

      const response = await api.put('/getCoffee/update', coffeeUpdates, {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json',
        }
      });
      return response.data;
    } catch (error) {
      console.error('메뉴 수정 중 오류 발생:', error);
      
      // 403 오류인 경우 로그인 상태 확인
      if (error.response?.status === 403) {
        throw new Error('권한이 없습니다. 관리자로 다시 로그인해주세요.');
      }
      
      throw error;
    }
  }
};

export default api; 