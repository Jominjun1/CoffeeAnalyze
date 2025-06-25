<template>
  <div class="coffee-compare">
    <!-- 홈 버튼 -->
    <div class="home-section">
      <button v-if="!isAdminLoggedIn" @click="showLoginModal" class="login-btn">🔐 관리자 로그인</button>
    </div>
    
    <!-- 관리자 메뉴 업데이트 버튼 (로그인 후 표시) -->
    <div v-if="isAdminLoggedIn" class="admin-section">
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
        <button @click="startCrawling('compose')" :disabled="isCrawling" class="submenu-btn compose">
          컴포즈커피
        </button>
      </div>
      <button v-if="!isEditMode" @click="enterEditMode" class="edit-btn">✏️ 수정하기</button>
      <button v-else @click="closeEditMode" class="edit-btn">⏹ 수정모드 종료</button>
      <button @click="logout" class="logout-btn">🚪 로그아웃</button>
    </div>
    
    <!-- 검색/비교 화면: 수정 모드가 아닐 때만 -->
    <template v-if="!isEditMode">
      <h2>☕ 커피 비교</h2>
      
      <!-- 검색 섹션 -->
      <div class="search-section">
        <div class="search-input-group">
          <input v-model="searchQuery" @keyup.enter="onSearch" placeholder="커피 이름을 입력하세요" class="search-input" />
          <select v-model="selectedCategory" class="category-select">
            <option value="">전체 카테고리</option>
            <option value="음료">음료</option>
            <option value="푸드">푸드</option>
            <option value="디저트">디저트</option>
            <option value="음식">음식</option>
          </select>
        </div>
        <div class="search-buttons">
          <button @click="onSearch" :disabled="isLoading" class="search-btn">검색</button>
          <button @click="clearSearch" class="clear-btn">초기화</button>
        </div>
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
                <!-- 카페인 주의 문구 -->
                <div v-if="coffee.ingredientDTO.caffeine > 30" class="caffeine-warning">
                  ⚠️ 고카페인 함량 섭취시 주의
                </div>
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
    </template>

    <!-- 수정 모드 화면: isEditMode일 때만 -->
    <template v-else>
      <div class="edit-section">
        <div class="edit-header">
          <h2>✏️ 메뉴 다중 수정</h2>
        </div>
        <div v-if="editFormData.length === 0">
          <!-- 검색 및 다중 선택 UI -->
          <div class="edit-search-section">
            <input v-model="editSearchQuery" @keyup.enter="onEditSearch" placeholder="수정할 커피/디저트 이름을 입력하세요" class="search-input" />
            <button @click="onEditSearch" class="search-btn">검색</button>
          </div>
          <div v-if="editSearchResults.length > 0" class="edit-results-section">
            <ul class="edit-results-list">
              <li v-for="item in editSearchResults" :key="item.brand + item.name" class="edit-result-item">
                <input type="checkbox" :id="'edit-' + item.brand + item.name" :checked="editSelectedItems.some(sel => sel.brand === item.brand && sel.name === item.name)" @change="toggleEditSelect(item)" />
                <label :for="'edit-' + item.brand + item.name">
                  <b>{{ item.brand }}</b> - {{ item.name }}
                </label>
              </li>
            </ul>
            <button :disabled="editSelectedItems.length === 0" @click="openEditForm" class="edit-btn">선택한 항목 수정</button>
          </div>
        </div>
        <div v-else>
          <!-- 다중 수정 폼 -->
          <form @submit.prevent="saveEditForm">
            <div v-for="(item, idx) in editFormData" :key="item.brand + item.name" class="edit-form-item">
              <h4>{{ item.brand }} - {{ item.name }}</h4>
              <div class="edit-form-fields">
                <label>이름: <input v-model="item.name" @input="handleEditInput(idx, 'name', item.name)" /></label>
                <label>카테고리(note): <input v-model="item.note" @input="handleEditInput(idx, 'note', item.note)" /></label>
                <label>칼로리: <input v-model.number="item.ingredientDTO.kcal" @input="handleEditIngredientInput(idx, 'kcal', item.ingredientDTO.kcal)" type="number" /></label>
                <label>카페인: <input v-model.number="item.ingredientDTO.caffeine" @input="handleEditIngredientInput(idx, 'caffeine', item.ingredientDTO.caffeine)" type="number" /></label>
                <label>나트륨: <input v-model.number="item.ingredientDTO.sodium" @input="handleEditIngredientInput(idx, 'sodium', item.ingredientDTO.sodium)" type="number" /></label>
                <label>당류: <input v-model.number="item.ingredientDTO.sugar" @input="handleEditIngredientInput(idx, 'sugar', item.ingredientDTO.sugar)" type="number" /></label>
                <label>포화지방: <input v-model.number="item.ingredientDTO.saturatedFat" @input="handleEditIngredientInput(idx, 'saturatedFat', item.ingredientDTO.saturatedFat)" type="number" /></label>
                <label>단백질: <input v-model.number="item.ingredientDTO.protein" @input="handleEditIngredientInput(idx, 'protein', item.ingredientDTO.protein)" type="number" /></label>
                <label>알레르기: <input v-model="item.ingredientDTO.allergicIngredients" @input="handleEditIngredientInput(idx, 'allergicIngredients', item.ingredientDTO.allergicIngredients)" /></label>
              </div>
            </div>
            <button type="submit" class="save-btn">저장(미리보기)</button>
          </form>
        </div>
      </div>
    </template>
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

// 카테고리 필터 상태
const selectedCategory = ref('');

// 수정 모드 상태
const isEditMode = ref(false);
const editSearchQuery = ref('');
const editSearchResults = ref([]);
const editSelectedItems = ref([]);
const showEditModal = ref(false);
const editFormData = ref([]);

// 로그인 상태 확인
const checkLoginStatus = () => {
  const token = localStorage.getItem('adminToken');
  if (token) {
    coffeeStore.setAdminLoggedIn(true);
  }
};

// 페이지 로드 시 로그인 상태 확인
checkLoginStatus();

// 카테고리별 필터링된 결과
const filteredResults = computed(() => {
  if (!selectedCategory.value) {
    return searchResults.value;
  }
  
  return searchResults.value.filter(coffee => {
    if (!coffee.note) return false;
    
    // 음료 카테고리의 경우 커피, 빽스치노도 포함
    if (selectedCategory.value === '음료') {
      return coffee.note.includes(`[${selectedCategory.value}]`) ||
             coffee.note.includes('[커피]') ||
             coffee.note.includes('[빽스치노]');
    }
    
    return coffee.note.includes(`[${selectedCategory.value}]`);
  });
});

// 매장별로 그룹화된 결과
const groupedResults = computed(() => {
  const groups = {};
  filteredResults.value.forEach(coffee => {
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
  return filteredResults.value.length;
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
  if (!allergic || allergic.trim() === '') {
    return '알레르기 정보 없음';
  }
  
  let cleaned = allergic.trim();
  
  // 다양한 형태의 알레르기 관련 접두사 제거
  const prefixes = [
    /^※\s*알레르기\s*유발\s*성분\s*:\s*/i,
    /^알레르기\s*성분\s*정보\s*:\s*/i,
    /^알레르기\s*성분\s*:\s*/i,
    /^알레르기\s*유발\s*성분\s*:\s*/i,
    /^알레르기\s*정보\s*:\s*/i,
    /^알레르기\s*:\s*/i
  ];
  
  for (const prefix of prefixes) {
    cleaned = cleaned.replace(prefix, '');
  }
  
  // 앞뒤 공백 제거
  cleaned = cleaned.trim();
  
  // 빈 문자열이거나 의미없는 텍스트인 경우
  if (!cleaned || 
      cleaned === '알레르기 성분:' ||
      cleaned === '알레르기 성분 :' ||
      cleaned === '알레르기 정보:' ||
      cleaned === '알레르기 정보 :' ||
      cleaned === '알레르기 성분 정보:' ||
      cleaned === '알레르기 성분 정보 :') {
    return '알레르기 정보 없음';
  }
  
  return cleaned;
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
  selectedCategory.value = '';
  searchResults.value = [];
  // 선택된 커피 초기화
  selectedCoffees.value = [];
}

// 수정 모드 관련 함수
function enterEditMode() {
  isEditMode.value = true;
  showEditModal.value = true;
  editSearchQuery.value = '';
  editSearchResults.value = [];
  editSelectedItems.value = [];
  editFormData.value = [];
}

function closeEditMode() {
  isEditMode.value = false;
  showEditModal.value = false;
}

async function onEditSearch() {
  if (!editSearchQuery.value.trim()) return;
  // 기존 검색 API 재사용
  const { coffeeApi } = await import('../services/api.js');
  editSearchResults.value = await coffeeApi.searchCoffee(editSearchQuery.value);
  // 선택 초기화
  editSelectedItems.value = [];
}

function toggleEditSelect(item) {
  const idx = editSelectedItems.value.findIndex(sel => sel.brand === item.brand && sel.name === item.name);
  if (idx === -1) {
    editSelectedItems.value.push(item);
  } else {
    editSelectedItems.value.splice(idx, 1);
  }
}

function openEditForm() {
  // 선택된 항목들의 복사본을 폼 데이터로 사용
  editFormData.value = editSelectedItems.value.map(item => ({ ...item }));
}

function handleEditInput(idx, key, value) {
  editFormData.value[idx][key] = value;
}

function handleEditIngredientInput(idx, key, value) {
  editFormData.value[idx].ingredientDTO[key] = value;
}

function saveEditForm() {
  // 실제 저장은 하지 않고, 콘솔에 미리보기만 출력
  console.log('수정될 데이터:', JSON.parse(JSON.stringify(editFormData.value)));
  alert('저장 미리보기(콘솔 확인)');
  // 이후 4단계에서 실제 저장 연동
}
</script>

<style scoped>
.edit-section {
  /* 모달 스타일 제거: 페이지 전체를 넓게 사용 */
  background: none;
  border-radius: 0;
  max-width: none;
  margin: 0;
  padding: 0;
  box-shadow: none;
  position: static;
}
.edit-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}
.edit-search-section {
  display: flex;
  gap: 0.8rem;
  margin-bottom: 1rem;
}
.edit-results-section {
  margin-top: 1rem;
}
.edit-results-list {
  list-style: none;
  padding: 0;
  margin: 0 0 1rem 0;
}
.edit-result-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 0;
}
.edit-btn {
  background: linear-gradient(135deg, #fbbf24 0%, #f59e42 100%);
  color: white;
  border: none;
  border-radius: 20px;
  padding: 0.6rem 1.2rem;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(251, 191, 36, 0.3);
}
.edit-btn:disabled {
  background: #bdc3c7;
  cursor: not-allowed;
}
.edit-form-item {
  background: #f8fafc;
  border-radius: 12px;
  padding: 1rem;
  margin-bottom: 1.5rem;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}
.edit-form-fields label {
  display: block;
  margin-bottom: 0.5rem;
  font-size: 0.95rem;
}
.edit-form-fields input {
  margin-left: 0.5rem;
  padding: 0.3rem 0.7rem;
  border: 1px solid #e0e6ed;
  border-radius: 8px;
  font-size: 0.95rem;
}
.save-btn {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  color: white;
  border: none;
  border-radius: 20px;
  padding: 0.7rem 1.5rem;
  font-size: 1.1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.3);
  margin-top: 1rem;
  width: 100%;
}
.save-btn:disabled {
  background: #bdc3c7;
  cursor: not-allowed;
}
</style> 