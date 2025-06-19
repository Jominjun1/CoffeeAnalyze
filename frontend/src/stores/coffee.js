import { defineStore } from 'pinia';

export const useCoffeeStore = defineStore('coffee', {
  state: () => ({
    searchResults: [],
    selectedCoffees: [], // 최대 4개까지 선택 가능
    isLoading: false,
    searchQuery: '',
    selectedBrand: 'all', // 브랜드 필터
    brands: ['all', '스타벅스', '메가커피', '빽다방', '이디야', '컴포즈', '투썸'],
    isAdminLoggedIn: false, // 관리자 로그인 상태
    isCrawling: false // 크롤링 상태
  }),
  getters: {
    filteredResults: (state) => {
      if (state.selectedBrand === 'all') {
        return state.searchResults;
      }
      return state.searchResults.filter(coffee => coffee.brand === state.selectedBrand);
    },
    canSelectMore: (state) => {
      return state.selectedCoffees.length < 4;
    }
  },
  actions: {
    async searchCoffee(name) {
      this.isLoading = true;
      const { coffeeApi } = await import('../services/api.js');
      this.searchResults = await coffeeApi.searchCoffee(name);
      this.isLoading = false;
    },
    selectCoffee(coffee) {
      if (this.selectedCoffees.length < 4 && !this.selectedCoffees.find(c => c.brand === coffee.brand && c.name === coffee.name)) {
        this.selectedCoffees.push(coffee);
      }
    },
    removeCoffee(index) {
      this.selectedCoffees.splice(index, 1);
    },
    resetComparison() {
      this.selectedCoffees = [];
    },
    setBrandFilter(brand) {
      this.selectedBrand = brand;
    },
    setAdminLoggedIn(status) {
      this.isAdminLoggedIn = status;
    },
    setCrawling(status) {
      this.isCrawling = status;
    }
  }
}); 