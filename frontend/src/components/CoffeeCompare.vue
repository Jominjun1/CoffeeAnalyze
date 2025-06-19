<template>
  <div class="coffee-compare">
    <!-- 홈 버튼 -->
    <div class="home-section">
      <button @click="showLoginModal" class="login-btn">🔐 관리자 로그인</button>
    </div>
    
    <!-- 관리자 메뉴 업데이트 버튼 (로그인 후 표시) -->
    <div v-if="isAdminLoggedIn" class="admin-section">
      <div class="update-menu">
        <button @click="toggleUpdateMenu" :disabled="isCrawling" class="update-btn">
          {{ isCrawling ? '🔄 크롤링 중...' : '📊 메뉴 업데이트' }}
        </button>
        <div v-if="showUpdateMenu" class="update-submenu">
          <button @click="startCrawling('mega_coffee')" :disabled="isCrawling" class="submenu-btn mega">
            메가커피
          </button>
          <button @click="startCrawling('paiks')" :disabled="isCrawling" class="submenu-btn paiks">
            빽다방
          </button>
          <button @click="startCrawling('starBucks')" :disabled="isCrawling" class="submenu-btn starbucks">
            스타벅스
          </button>
          <button @click="startCrawling('ediya')" :disabled="isCrawling" class="submenu-btn ediya">
            이디야
          </button>
        </div>
      </div>
      <button @click="logout" class="logout-btn">🚪 로그아웃</button>
    </div>
    
    <h2>☕ 커피 비교</h2>
    
    <!-- 검색 섹션 -->
    <div class="search-section">
      <input v-model="searchQuery" @keyup.enter="onSearch" placeholder="커피 이름을 입력하세요" class="search-input" />
      <button @click="onSearch" :disabled="isLoading" class="search-btn">검색</button>
      <button @click="clearSearch" class="clear-btn">초기화</button>
    </div>

    <!-- 로딩 및 빈 상태 -->
    <div v-if="isLoading" class="loading-spinner">검색 중...</div>
    <div v-if="!isLoading && groupedResults.length === 0 && searchQuery" class="empty-msg">
      검색 결과가 없습니다.
    </div>

    <!-- 검색 결과 (매장별 분리) -->
    <div v-if="groupedResults.length > 0" class="results-section">
      <div class="results-header">
        <h3>검색 결과 ({{ totalResults }}개)</h3>
        <button v-if="selectedCoffees.length > 1" @click="showComparisonModal" class="compare-btn">📊 비교 결과 보기</button>
      </div>
      
      <div class="brand-sections">
        <div v-for="brandGroup in groupedResults" :key="brandGroup.brand" class="brand-section">
          <h4 class="brand-title" :style="{ backgroundColor: getBrandColor(brandGroup.brand) }">
            {{ brandGroup.brand }} ({{ brandGroup.coffees.length }}개)
          </h4>
          <ul class="brand-results">
            <li v-for="coffee in brandGroup.coffees" :key="coffee.brand + coffee.name" class="result-item">
              <span class="coffee-name">{{ coffee.name }}</span>
              <button @click="selectCoffee(coffee)" class="select-btn" :disabled="!canSelectMore || isAlreadySelected(coffee)">
                {{ isAlreadySelected(coffee) ? '선택됨' : '선택' }}
              </button>
            </li>
          </ul>
        </div>
      </div>
    </div>

    <!-- 선택된 커피 표시 -->
    <div v-if="selectedCoffees.length > 0" class="selected-section">
      <h3>선택된 커피 ({{ selectedCoffees.length }}/4)</h3>
      <div class="selected-list">
        <div v-for="(coffee, index) in selectedCoffees" :key="index" class="selected-item">
          <span class="brand-chip" :style="{ backgroundColor: getBrandColor(coffee.brand) }">{{ coffee.brand }}</span>
          <span class="coffee-name">{{ coffee.name }}</span>
          <button @click="removeCoffee(index)" class="remove-btn">×</button>
        </div>
      </div>
    </div>

    <!-- 비교 결과 -->
    <div class="comparison-section" v-if="selectedCoffees.length > 1">
      <button @click="resetComparison" class="reset-btn">비교 초기화</button>
    </div>

    <!-- 비교 결과 모달 -->
    <div v-if="showComparison" class="modal-overlay" @click="closeComparisonModal">
      <div class="comparison-modal" @click.stop>
        <div class="modal-header">
          <h3>☕ 커피 비교 결과</h3>
          <button @click="closeComparisonModal" class="close-btn">×</button>
        </div>
        <div class="compare-table" :class="`compare-${selectedCoffees.length}`">
          <div v-for="(coffee, index) in selectedCoffees" :key="index" class="compare-col card">
            <h4>커피 {{ index + 1 }}</h4>
            <div class="coffee-content">
              <img :src="coffee.imageUrl" :alt="`커피${index + 1} 이미지`" class="coffee-img" />
              <p class="coffee-title">
                <b>{{ coffee.brand }}</b><br>{{ coffee.name }}
              </p>
              <ul class="info-list">
                <li :class="getComparisonClass('kcal', coffee.ingredientDTO.kcal, index)">
                  <span>칼로리</span> <b>{{ coffee.ingredientDTO.kcal }}</b>
                </li>
                <li :class="getComparisonClass('caffeine', coffee.ingredientDTO.caffeine, index)">
                  <span>카페인</span> <b>{{ coffee.ingredientDTO.caffeine }}</b>
                </li>
                <li :class="getComparisonClass('sodium', coffee.ingredientDTO.sodium, index)">
                  <span>나트륨</span> <b>{{ coffee.ingredientDTO.sodium }}</b>
                </li>
                <li :class="getComparisonClass('sugar', coffee.ingredientDTO.sugar, index)">
                  <span>당류</span> <b>{{ coffee.ingredientDTO.sugar }}</b>
                </li>
                <li :class="getComparisonClass('saturatedFat', coffee.ingredientDTO.saturatedFat, index)">
                  <span>포화지방</span> <b>{{ coffee.ingredientDTO.saturatedFat }}</b>
                </li>
                <li :class="getComparisonClass('protein', coffee.ingredientDTO.protein, index)">
                  <span>단백질</span> <b>{{ coffee.ingredientDTO.protein }}</b>
                </li>
                <li>
                  <span>알레르기</span> <b>{{ formatAllergic(coffee.ingredientDTO.allergicIngredients) }}</b>
                </li>
              </ul>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 로그인 모달 -->
    <div v-if="showLogin" class="modal-overlay" @click="closeLoginModal">
      <div class="login-modal" @click.stop>
        <h3>🔐 관리자 로그인</h3>
        <form @submit.prevent="login" class="login-form">
          <div class="form-group">
            <label for="adminId">아이디</label>
            <input id="adminId" v-model="loginForm.adminId" type="text" placeholder="관리자 아이디를 입력하세요" required/>
          </div>
          <div class="form-group">
            <label for="password">비밀번호</label>
            <input id="password" v-model="loginForm.password" type="password" placeholder="비밀번호를 입력하세요" required/>
          </div>
          <div class="modal-buttons">
            <button type="submit" :disabled="isLoggingIn" class="login-submit-btn">
              {{ isLoggingIn ? '로그인 중...' : '로그인' }}
            </button>
            <button type="button" @click="closeLoginModal" class="cancel-btn">취소</button>
          </div>
        </form>
        <div v-if="loginError" class="login-error">{{ loginError }}</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useCoffeeStore } from '../stores/coffee';
import { storeToRefs } from 'pinia';
import { ref, computed } from 'vue';
import '../assets/coffee-compare.css';

const coffeeStore = useCoffeeStore();
const { 
  searchResults, 
  selectedCoffees, 
  isLoading, 
  searchQuery,
  canSelectMore,
  isAdminLoggedIn,
  isCrawling
} = storeToRefs(coffeeStore);

const { 
  searchCoffee, 
  selectCoffee, 
  removeCoffee,
  resetComparison
} = coffeeStore;

// 로그인 관련 상태
const showLogin = ref(false);
const isLoggingIn = ref(false);
const loginError = ref('');
const loginForm = ref({
  adminId: '',
  password: ''
});

// 서브메뉴 상태
const showUpdateMenu = ref(false);

// 로그인 상태 확인
const checkLoginStatus = () => {
  const token = localStorage.getItem('adminToken');
  if (token) {
    coffeeStore.setAdminLoggedIn(true);
  }
};

// 페이지 로드 시 로그인 상태 확인
checkLoginStatus();

// 매장별로 그룹화된 결과
const groupedResults = computed(() => {
  const groups = {};
  searchResults.value.forEach(coffee => {
    if (!groups[coffee.brand]) {
      groups[coffee.brand] = [];
    }
    groups[coffee.brand].push(coffee);
  });
  
  return Object.keys(groups).map(brand => ({
    brand,
    coffees: groups[brand]
  })).sort((a, b) => a.brand.localeCompare(b.brand));
});

// 전체 결과 개수
const totalResults = computed(() => {
  return searchResults.value.length;
});

function onSearch() {
  if (searchQuery.value.trim()) {
    searchCoffee(searchQuery.value);
  }
}

// 로그인 관련 함수들
function showLoginModal() {
  showLogin.value = true;
  loginError.value = '';
  loginForm.value = { adminId: '', password: '' };
}

function closeLoginModal() {
  showLogin.value = false;
  loginError.value = '';
  loginForm.value = { adminId: '', password: '' };
}

async function login() {
  if (!loginForm.value.adminId || !loginForm.value.password) {
    loginError.value = '아이디와 비밀번호를 모두 입력해주세요.';
    return;
  }

  isLoggingIn.value = true;
  loginError.value = '';

  try {
    const response = await fetch('http://localhost:8080/admin/login', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        admin_id: loginForm.value.adminId,
        admin_pw: loginForm.value.password
      })
    });

    if (response.ok) {
      const data = await response.json();
      if (data.token) {
        // 토큰을 로컬 스토리지에 저장
        localStorage.setItem('adminToken', data.token);
        // 로그인 상태 업데이트
        coffeeStore.setAdminLoggedIn(true);
        alert('로그인 성공!');
        closeLoginModal();
      } else {
        loginError.value = '로그인에 실패했습니다.';
      }
    } else {
      loginError.value = '아이디 또는 비밀번호가 올바르지 않습니다.';
    }
  } catch (error) {
    console.error('로그인 오류:', error);
    loginError.value = '로그인 중 오류가 발생했습니다.';
  } finally {
    isLoggingIn.value = false;
  }
}

function formatAllergic(allergic) {
  if (!allergic || 
      allergic === '알레르기 성분: ' || 
      allergic === '알레르기 성분:' ||
      allergic === '알레르기 성분 : ' ||
      allergic === '알레르기 성분 :' ||
      allergic.trim() === '' || 
      allergic.trim() === '알레르기 성분:' ||
      allergic.trim() === '알레르기 성분 :') {
    return '알레르기 정보 없음';
  }
  
  // "알레르기 성분: " 또는 "알레르기 성분 : " 제거
  return allergic.replace(/^알레르기 성분\s*:\s*/, '');
}

function isAlreadySelected(coffee) {
  return selectedCoffees.value.find(c => c.brand === coffee.brand && c.name === coffee.name);
}

function getBrandColor(brand) {
  const colors = {
    '스타벅스': '#006241',
    '메가커피': '#FF6B35',
    '빽다방': '#FFD700',
    '이디야': '#1E3A8A',
    '컴포즈': '#059669',
    '투썸': '#DC2626'
  };
  return colors[brand] || '#6B7280';
}

function getComparisonClass(type, value, currentIndex) {
  if (selectedCoffees.value.length < 2) return '';
  
  const values = selectedCoffees.value.map(coffee => {
    switch(type) {
      case 'kcal': return coffee.ingredientDTO.kcal;
      case 'caffeine': return coffee.ingredientDTO.caffeine;
      case 'sodium': return coffee.ingredientDTO.sodium;
      case 'sugar': return coffee.ingredientDTO.sugar;
      case 'saturatedFat': return coffee.ingredientDTO.saturatedFat;
      case 'protein': return coffee.ingredientDTO.protein;
      default: return 0;
    }
  });
  
  const currentValue = values[currentIndex];
  const maxValue = Math.max(...values);
  const minValue = Math.min(...values);
  
  // 모든 값이 같은 경우 체크
  const allSame = values.every(v => v === currentValue);
  if (allSame) {
    return 'same';
  }
  
  if (type === 'protein') {
    // 단백질은 높을수록 좋지만, 더 높은 값이 빨간색으로 표시
    return currentValue === maxValue ? 'lower' : currentValue === minValue ? 'higher' : '';
  } else {
    // 나머지는 낮을수록 좋음 (낮은 값이 파란색, 높은 값이 빨간색)
    return currentValue === minValue ? 'higher' : currentValue === maxValue ? 'lower' : '';
  }
}

// 서브메뉴 토글 함수
function toggleUpdateMenu() {
  showUpdateMenu.value = !showUpdateMenu.value;
}

// 브랜드별 크롤링 함수
async function startCrawling(brand) {
  const token = localStorage.getItem('adminToken');
  if (!token) {
    alert('로그인이 필요합니다.');
    return;
  }

  try {
    coffeeStore.setCrawling(true);
    showUpdateMenu.value = false; // 서브메뉴 닫기
    
    const response = await fetch(`http://localhost:8080/craw/${brand}`, {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json',
      }
    });

    if (response.ok) {
      const result = await response.text();
      alert(`${getBrandName(brand)} 크롤링이 완료되었습니다.`);
    } else {
      const errorData = await response.text();
      alert(`${getBrandName(brand)} 크롤링 실패: ${errorData}`);
    }
  } catch (error) {
    console.error('크롤링 오류:', error);
    alert(`${getBrandName(brand)} 크롤링 중 오류가 발생했습니다.`);
  } finally {
    coffeeStore.setCrawling(false);
  }
}

// 브랜드명 매핑 함수
function getBrandName(brand) {
  const brandNames = {
    'mega_coffee': '메가커피',
    'paiks': '빽다방',
    'starBucks': '스타벅스',
    'ediya': '이디야'
  };
  return brandNames[brand] || brand;
}

// 로그아웃 함수
async function logout() {
  const token = localStorage.getItem('adminToken');
  
  try {
    // 백엔드에 로그아웃 요청
    if (token) {
      await fetch('http://localhost:8080/admin/logout', {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json',
        }
      });
    }
  } catch (error) {
    console.error('로그아웃 요청 오류:', error);
  } finally {
    // 로컬 스토리지에서 토큰 제거
    localStorage.removeItem('adminToken');
    // 로그인 상태 업데이트
    coffeeStore.setAdminLoggedIn(false);
    // 서브메뉴 닫기
    showUpdateMenu.value = false;
    
    alert('로그아웃되었습니다.');
  }
}

// 비교 결과 모달 관련 상태
const showComparison = ref(false);

function showComparisonModal() {
  showComparison.value = true;
}

function closeComparisonModal() {
  showComparison.value = false;
}

function clearSearch() {
  // 검색어와 검색 결과 초기화
  searchQuery.value = '';
  searchResults.value = [];
  // 선택된 커피 초기화
  selectedCoffees.value = [];
  // 페이지 새로고침 (홈 버튼 효과)
  window.location.reload();
}
</script> 