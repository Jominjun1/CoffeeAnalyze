<template>
  <div class="coffee-compare">
    <!-- 홈 버튼 -->
    <div class="home-section">
      <button v-if="!isAdminLoggedIn" @click="showLoginModal" class="login-btn">🔐 관리자 로그인</button>
    </div>
    
    <!-- 통합 관리자 메뉴 (로그인 후 표시) -->
    <div v-if="isAdminLoggedIn" class="admin-menu">
      <button @click="toggleAdminMenu" class="menu-btn">
        <span class="menu-icon">☰</span>
        <span class="menu-text">메뉴</span>
      </button>
      <div v-if="showAdminMenu" class="admin-dropdown">
        <!-- 메뉴 업데이트 섹션 -->
        <div class="menu-section">
          <h4 class="menu-section-title">📊 메뉴 업데이트</h4>
          <div class="menu-items">
            <button @click="startCrawling('mega_coffee')" :disabled="isCrawling" class="menu-item mega">
              <span class="menu-item-icon">☕</span>
              <span class="menu-item-text">메가커피</span>
            </button>
            <button @click="startCrawling('paiks')" :disabled="isCrawling" class="menu-item paiks">
              <span class="menu-item-icon">☕</span>
              <span class="menu-item-text">빽다방</span>
            </button>
            <button @click="startCrawling('starBucks')" :disabled="isCrawling" class="menu-item starbucks">
              <span class="menu-item-icon">☕</span>
              <span class="menu-item-text">스타벅스</span>
            </button>
            <button @click="startCrawling('ediya')" :disabled="isCrawling" class="menu-item ediya">
              <span class="menu-item-icon">☕</span>
              <span class="menu-item-text">이디야</span>
            </button>
            <button @click="startCrawling('compose')" :disabled="isCrawling" class="menu-item compose">
              <span class="menu-item-icon">☕</span>
              <span class="menu-item-text">컴포즈커피</span>
            </button>
          </div>
        </div>
        
        <!-- 관리 기능 섹션 -->
        <div class="menu-section">
          <h4 class="menu-section-title">⚙️ 관리 기능</h4>
          <div class="menu-items">
            <button v-if="!isEditMode" @click="enterEditMode" class="menu-item edit">
              <span class="menu-item-icon">✏️</span>
              <span class="menu-item-text">수정하기</span>
            </button>
            <button v-else @click="closeEditMode" class="menu-item edit">
              <span class="menu-item-icon">⏹</span>
              <span class="menu-item-text">수정모드 종료</span>
            </button>
            <button @click="logout" class="menu-item logout">
              <span class="menu-item-icon">🚪</span>
              <span class="menu-item-text">로그아웃</span>
            </button>
          </div>
        </div>
      </div>
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
          <button @click="onSearch" :disabled="isLoading" class="search-btn">검색</button>
          <button v-if="groupedResults.length > 0" @click="clearSearch" class="clear-btn">초기화</button>
        </div>
        <div class="search-tips">
          💡 검색 팁: "아메리카노", "라떼", "카페" 등으로 검색해보세요
        </div>
      </div>

      <!-- 로딩 및 빈 상태 -->
      <div v-if="isLoading" class="loading-spinner">검색 중...</div>
      <div v-else-if="!isLoading && groupedResults.length === 0 && searchQuery" class="no-results">
        <div class="no-results-content">
          <span class="no-results-icon">🔍</span>
          <h3>검색 결과가 없습니다</h3>
          <p>다른 키워드로 검색해보세요</p>
        </div>
      </div>

      <!-- 검색 결과 (매장별 분리) -->
      <div v-if="groupedResults.length > 0" class="results-section">
        <div class="results-header">
          <h3>검색 결과 ({{ totalResults }}개)</h3>
          <button v-if="selectedCoffees.length > 0" @click="showComparisonModal" class="compare-btn">
            {{ selectedCoffees.length === 1 ? '📊 상세 정보 보기' : '📊 비교 결과 보기' }}
          </button>
        </div>
        
        <div class="brand-sections">
          <div v-for="brandGroup in groupedResults" :key="brandGroup.brand" class="brand-section">
            <h4 class="brand-title" :style="{ backgroundColor: getBrandColor(brandGroup.brand) }">
              {{ brandGroup.brand }} ({{ brandGroup.coffees.length }}개)
            </h4>
            <ul class="brand-results">
              <li v-for="coffee in brandGroup.coffees" :key="coffee.brand + coffee.name" class="result-item">
                <span class="coffee-name">{{ coffee.name }}</span>
                <button 
                  @click="toggleCoffeeSelection(coffee)" 
                  class="select-btn" 
                  :class="{ selected: isAlreadySelected(coffee) }"
                  :disabled="!canSelectMore && !isAlreadySelected(coffee)"
                >
                  {{ isAlreadySelected(coffee) ? '선택됨' : '선택' }}
                </button>
              </li>
            </ul>
          </div>
        </div>
      </div>

      <!-- 선택된 커피 표시 -->
      <div v-if="selectedCoffees.length > 0" class="selected-section">
        <h3>{{ selectedCoffees.length === 1 ? '선택된 커피' : `선택된 커피 (${selectedCoffees.length}/4)` }}</h3>
        <div class="selected-list">
          <div v-for="(coffee, index) in selectedCoffees" :key="index" class="selected-item">
            <span class="brand-chip" :style="{ backgroundColor: getBrandColor(coffee.brand) }">{{ coffee.brand }}</span>
            <span class="coffee-name">{{ coffee.name }}</span>
            <button @click="removeCoffee(index)" class="remove-btn">×</button>
          </div>
        </div>
      </div>

      <!-- 비교 결과 -->
      <div class="comparison-section" v-if="selectedCoffees.length > 0">
        <button @click="resetComparison" class="reset-btn">
          {{ selectedCoffees.length === 1 ? '선택 해제' : '비교 초기화' }}
        </button>
      </div>

      <!-- 비교 결과 모달 -->
      <div v-if="showComparison" class="modal-overlay" @click="closeComparisonModal">
        <div class="comparison-modal" @click.stop>
          <div class="modal-header">
            <h3>{{ selectedCoffees.length === 1 ? '☕ 커피 상세 정보' : '☕ 커피 비교 결과' }}</h3>
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
                <p v-if="coffee.size" class="coffee-size">{{ coffee.size }}</p>
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
              <label for="admin_id">아이디</label>
              <input id="admin_id" v-model="loginForm.admin_id" type="text" placeholder="관리자 아이디를 입력하세요" required/>
            </div>
            <div class="form-group">
              <label for="admin_pw">비밀번호</label>
              <input id="admin_pw" v-model="loginForm.admin_pw" type="password" placeholder="비밀번호를 입력하세요" required/>
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
          <div class="edit-header-left">
            <h2>✏️ {{ isMultiEditMode ? '메뉴 다중 수정' : '메뉴 수정' }}</h2>
            <button class="help-btn" @click="showHelp = true" title="수정 절차 안내">
              <span>❓</span>
            </button>
          </div>
          <div class="edit-header-right">
            <button 
              @click="toggleEditMode" 
              class="mode-toggle-btn"
              :class="{ active: isMultiEditMode }"
            >
              {{ isMultiEditMode ? '단일 수정' : '다중 수정' }}
            </button>
          </div>
        </div>
        <div v-if="showHelp" class="help-modal-overlay" @click.self="showHelp = false">
          <div class="help-modal">
            <h3>📝 {{ isMultiEditMode ? '메뉴 다중 수정' : '메뉴 수정' }} 절차 안내</h3>
            <ol v-if="isMultiEditMode">
              <li><b>항목 검색 및 선택</b><br>수정할 커피/디저트 이름을 검색하고, 원하는 항목들을 체크하세요.</li>
              <li><b>정보 수정</b><br>선택한 항목들의 이름, 카테고리, 영양정보 등을 한 번에 수정할 수 있습니다.</li>
              <li><b>저장</b><br>모든 수정을 마쳤으면 <b>저장(미리보기)</b> 버튼을 눌러주세요.<br><span style="color:#888">(실제 저장은 추후 구현 예정)</span></li>
            </ol>
            <ol v-else>
              <li><b>항목 검색</b><br>수정할 커피/디저트 이름을 검색하세요.</li>
              <li><b>항목 선택</b><br>검색 결과에서 수정할 항목을 하나 선택하세요.</li>
              <li><b>정보 수정</b><br>선택한 항목의 정보를 수정하세요.</li>
              <li><b>저장</b><br>수정을 마쳤으면 <b>저장(미리보기)</b> 버튼을 눌러주세요.</li>
            </ol>
            <button class="close-help-btn" @click="showHelp = false">닫기</button>
          </div>
        </div>
        
        <div v-if="editFormData.length === 0">
          <!-- 검색 및 선택 UI -->
          <div class="edit-search-section">
            <div class="search-input-group">
              <input 
                v-model="editSearchQuery" 
                @keyup.enter="onEditSearch" 
                placeholder="수정할 커피/디저트 이름을 입력하세요" 
                class="search-input" 
              />
              <button @click="onEditSearch" class="search-btn">🔍 검색</button>
            </div>
            <div class="search-tips">
              💡 검색 팁: "아메리카노", "라떼", "카페" 등으로 검색해보세요
            </div>
          </div>
          
          <div v-if="editSearchResults.length > 0" class="edit-results-section">
            <div class="results-header">
              <h3>검색 결과 ({{ editSearchResults.length }}개)</h3>
              <div class="selection-info">
                <span v-if="editSelectedItems.length > 0" class="selected-count">
                  선택됨: {{ editSelectedItems.length }}개
                </span>
                <button 
                  v-if="editSelectedItems.length > 0 && isMultiEditMode" 
                  @click="editSelectedItems = []" 
                  class="clear-selection-btn"
                >
                  선택 해제
                </button>
                <button 
                  v-if="isMultiEditMode"
                  :disabled="editSelectedItems.length === 0" 
                  @click="openEditForm" 
                  class="edit-proceed-btn"
                >
                  ✏️ 선택한 {{ editSelectedItems.length }}개 항목 수정하기
                </button>
              </div>
            </div>
            
            <div class="brand-sections">
              <div v-for="brandGroup in groupedEditSearchResults" :key="brandGroup.brand" class="brand-section">
                <h4 class="brand-title" :style="{ backgroundColor: getBrandColor(brandGroup.brand) }">
                  {{ brandGroup.brand }} ({{ brandGroup.coffees.length }}개)
                </h4>
                <ul class="brand-results">
                  <li v-for="coffee in brandGroup.coffees" :key="coffee.brand + coffee.name" class="result-item">
                    <span class="coffee-name">{{ coffee.name }}</span>
                    <button 
                      @click="toggleEditSelect(coffee)" 
                      class="select-btn" 
                      :class="{ selected: isEditSelected(coffee) }"
                      :disabled="!isMultiEditMode && editSelectedItems.length > 0 && !isEditSelected(coffee)"
                    >
                      {{ isEditSelected(coffee) ? '선택됨' : '선택' }}
                    </button>
                  </li>
                </ul>
              </div>
            </div>
          </div>
          
          <div v-else-if="editSearchQuery && !isLoading" class="no-results">
            <div class="no-results-content">
              <span class="no-results-icon">🔍</span>
              <h3>검색 결과가 없습니다</h3>
              <p>다른 키워드로 검색해보세요</p>
            </div>
          </div>
        </div>
        
        <div v-else>
          <!-- 수정 폼 -->
          <div class="edit-form-header">
            <h3>📝 {{ editFormData.length }}개 항목 수정</h3>
            <div class="edit-form-header-right">
              <button @click="editFormData = []" class="back-to-search-btn">← 검색으로 돌아가기</button>
            </div>
          </div>
          
          <form @submit.prevent="saveEditForm" class="edit-form">
            <div class="edit-results-container">
              <div v-for="(group, brand) in groupedEditFormData" :key="brand" class="edit-brand-section">
                <h4 class="edit-brand-title" :style="{ backgroundColor: getBrandColor(brand) }">
                  {{ brand }} ({{ group.length }}개)
                </h4>
                <div class="edit-brand-results">
                  <div v-for="(item, idx) in group" :key="item.brand + item.name" class="edit-result-item">
                    <div class="edit-item-content">
                      <span class="edit-coffee-name">{{ item.name }}</span>
                      <button type="button" @click="removeEditItem(brand, idx)" class="edit-remove-btn">×</button>
                    </div>
                    
                    <div class="edit-fields">
                      <div class="edit-field-row">
                        <div class="edit-field-group">
                          <label>이름</label>
                          <input v-model="item.name" @input="handleEditInput(getItemIndex(brand, idx), 'name', item.name)" placeholder="메뉴 이름" />
                        </div>
                        
                        <div class="edit-field-group">
                          <label>카테고리</label>
                          <input v-model="item.note" @input="handleEditInput(getItemIndex(brand, idx), 'note', item.note)" placeholder="예: [음료], [푸드]" />
                        </div>
                        
                        <div class="edit-field-group">
                          <label>가격 (원)</label>
                          <input v-model.number="item.price" @input="handleEditInput(getItemIndex(brand, idx), 'price', item.price)" type="number" step="100" placeholder="0" />
                        </div>
                      </div>
                      
                      <div class="edit-field-row">
                        <div class="edit-field-group">
                          <label>칼로리 (kcal)</label>
                          <input v-model.number="item.ingredientDTO.kcal" @input="handleEditIngredientInput(getItemIndex(brand, idx), 'kcal', item.ingredientDTO.kcal)" type="number" step="0.1" placeholder="0" />
                        </div>
                        
                        <div class="edit-field-group">
                          <label>카페인 (mg)</label>
                          <input v-model.number="item.ingredientDTO.caffeine" @input="handleEditIngredientInput(getItemIndex(brand, idx), 'caffeine', item.ingredientDTO.caffeine)" type="number" step="0.1" placeholder="0" />
                        </div>
                        
                        <div class="edit-field-group">
                          <label>나트륨 (mg)</label>
                          <input v-model.number="item.ingredientDTO.sodium" @input="handleEditIngredientInput(getItemIndex(brand, idx), 'sodium', item.ingredientDTO.sodium)" type="number" step="0.1" placeholder="0" />
                        </div>
                      </div>
                      
                      <div class="edit-field-row">
                        <div class="edit-field-group">
                          <label>당류 (g)</label>
                          <input v-model.number="item.ingredientDTO.sugar" @input="handleEditIngredientInput(getItemIndex(brand, idx), 'sugar', item.ingredientDTO.sugar)" type="number" step="0.1" placeholder="0" />
                        </div>
                        
                        <div class="edit-field-group">
                          <label>포화지방 (g)</label>
                          <input v-model.number="item.ingredientDTO.saturatedFat" @input="handleEditIngredientInput(getItemIndex(brand, idx), 'saturatedFat', item.ingredientDTO.saturatedFat)" type="number" step="0.1" placeholder="0" />
                        </div>
                        
                        <div class="edit-field-group">
                          <label>단백질 (g)</label>
                          <input v-model.number="item.ingredientDTO.protein" @input="handleEditIngredientInput(getItemIndex(brand, idx), 'protein', item.ingredientDTO.protein)" type="number" step="0.1" placeholder="0" />
                        </div>
                      </div>
                      
                      <div class="edit-field-row">
                        <div class="edit-field-group full-width">
                          <label>알레르기 정보</label>
                          <input v-model="item.ingredientDTO.allergicIngredients" @input="handleEditIngredientInput(getItemIndex(brand, idx), 'allergicIngredients', item.ingredientDTO.allergicIngredients)" placeholder="예: 우유, 견과류" />
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            
            <div class="edit-form-actions">
              <button type="submit" class="edit-save-btn">💾 저장 (미리보기)</button>
            </div>
          </form>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { useCoffeeStore } from '../stores/coffee';
import { storeToRefs } from 'pinia';
import { ref, computed, onMounted } from 'vue';
import '../assets/coffee-compare.css';
import { coffeeApi } from '../services/api.js';

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
  admin_id: '',
  admin_pw: ''
});

// 서브메뉴 상태
const showAdminMenu = ref(false);

// 카테고리 필터 상태
const selectedCategory = ref('');

// 수정 모드 상태
const isEditMode = ref(false);
const editSearchQuery = ref('');
const editSearchResults = ref([]);
const editSelectedItems = ref([]);
const editFormData = ref([]);
const showHelp = ref(false);
const isMultiEditMode = ref(false);

// 로그인 상태 확인
const checkLoginStatus = () => {
  const token = sessionStorage.getItem('adminToken');
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
  
  // 각 브랜드 내에서 정확히 일치하는 항목을 맨 위로 정렬
  Object.keys(groups).forEach(brand => {
    groups[brand].sort((a, b) => {
      const query = searchQuery.value.toLowerCase();
      const aName = a.name.toLowerCase();
      const bName = b.name.toLowerCase();
      
      // 정확히 일치하는 항목이 맨 위로
      const aExact = aName === query;
      const bExact = bName === query;
      
      if (aExact && !bExact) return -1;
      if (!aExact && bExact) return 1;
      
      // 그 다음에는 검색어로 시작하는 항목
      const aStartsWith = aName.startsWith(query);
      const bStartsWith = bName.startsWith(query);
      
      if (aStartsWith && !bStartsWith) return -1;
      if (!aStartsWith && bStartsWith) return 1;
      
      // 마지막에는 알파벳 순으로 정렬
      return aName.localeCompare(bName);
    });
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
    // 검색할 때마다 선택된 항목들 초기화
    selectedCoffees.value = [];
    searchCoffee(searchQuery.value);
  }
}

// 로그인 관련 함수들
function showLoginModal() {
  showLogin.value = true;
  loginError.value = '';
  loginForm.value = { admin_id: '', admin_pw: '' };
}

function closeLoginModal() {
  showLogin.value = false;
  loginError.value = '';
  loginForm.value = { admin_id: '', admin_pw: '' };
}

async function login() {
  isLoggingIn.value = true;
  loginError.value = '';
  
  try {
    const response = await fetch('http://localhost:8080/admin/login', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(loginForm.value)
    });

    if (response.ok) {
      const data = await response.json();
      sessionStorage.setItem('adminToken', data.token);
      coffeeStore.setAdminLoggedIn(true);
      showLogin.value = false;
      loginForm.value = { admin_id: '', admin_pw: '' };
      window.showToast('success', '로그인 성공!', '관리자 모드가 활성화되었습니다.');
    } else {
      const errorData = await response.json();
      loginError.value = errorData.message || '로그인에 실패했습니다.';
      window.showToast('error', '로그인 실패', loginError.value);
    }
  } catch (error) {
    console.error('로그인 오류:', error);
    loginError.value = '로그인 중 오류가 발생했습니다.';
    window.showToast('error', '로그인 오류', '네트워크 연결을 확인해주세요.');
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

function toggleCoffeeSelection(coffee) {
  if (isAlreadySelected(coffee)) {
    // 이미 선택된 경우 선택 해제
    const index = selectedCoffees.value.findIndex(c => c.brand === coffee.brand && c.name === coffee.name);
    removeCoffee(index);
  } else {
    // 선택되지 않은 경우 선택 추가
    if (selectedCoffees.value.length < 4) {
      selectCoffee(coffee);
    }
  }
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
function toggleAdminMenu() {
  showAdminMenu.value = !showAdminMenu.value;
}

// 메뉴 외부 클릭 시 닫기
function closeAdminMenu() {
  showAdminMenu.value = false;
}

// 컴포넌트 마운트 시 외부 클릭 이벤트 리스너 추가
onMounted(() => {
  document.addEventListener('click', (event) => {
    const adminMenu = document.querySelector('.admin-menu');
    if (adminMenu && !adminMenu.contains(event.target)) {
      closeAdminMenu();
    }
  });
});

// 브랜드별 크롤링 함수
async function startCrawling(brand) {
  const token = sessionStorage.getItem('adminToken');
  console.log('크롤링 토큰:', token); // 디버깅용
  
  if (!token) {
    window.showToast('warning', '로그인 필요', '관리자 로그인이 필요합니다.');
    return;
  }

  coffeeStore.setCrawling(true);
  showAdminMenu.value = false;

  try {
    console.log('크롤링 요청 URL:', `http://localhost:8080/craw/${brand}`); // 디버깅용
    const response = await fetch(`http://localhost:8080/craw/${brand}`, {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json',
      }
    });

    console.log('크롤링 응답 상태:', response.status); // 디버깅용

    if (response.ok) {
      window.showToast('success', '크롤링 완료', `${getBrandName(brand)} 메뉴가 업데이트되었습니다.`);
    } else {
      const errorData = await response.text();
      console.log('크롤링 에러 응답:', errorData); // 디버깅용
      window.showToast('error', '크롤링 실패', `${getBrandName(brand)}: ${errorData}`);
    }
  } catch (error) {
    console.error('크롤링 오류:', error);
    window.showToast('error', '크롤링 오류', `${getBrandName(brand)} 크롤링 중 오류가 발생했습니다.`);
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
  const token = sessionStorage.getItem('adminToken');
  
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
    // 세션 스토리지에서 토큰 제거
    sessionStorage.removeItem('adminToken');
    // 로그인 상태 업데이트
    coffeeStore.setAdminLoggedIn(false);
    // 서브메뉴 닫기
    showAdminMenu.value = false;
    
    window.showToast('info', '로그아웃', '로그아웃되었습니다.');
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
  editSearchQuery.value = '';
  editSearchResults.value = [];
  editSelectedItems.value = [];
  editFormData.value = [];
}

function closeEditMode() {
  isEditMode.value = false;
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
  if (isMultiEditMode.value) {
    // 다중 수정 모드: 기존 로직 유지
    const idx = editSelectedItems.value.findIndex(sel => sel.brand === item.brand && sel.name === item.name);
    if (idx === -1) {
      editSelectedItems.value.push(item);
    } else {
      editSelectedItems.value.splice(idx, 1);
    }
  } else {
    // 단일 수정 모드: 선택 시 바로 수정 폼 열기
    editSelectedItems.value = [item];
    openEditForm();
  }
}

function openEditForm() {
  // 선택된 항목 검증
  if (!editSelectedItems.value || editSelectedItems.value.length === 0) {
    window.showToast('error', '오류', '수정할 항목을 선택해주세요.');
    return;
  }

  console.log('선택된 항목들:', editSelectedItems.value);

  // 선택된 항목들의 깊은 복사본을 폼 데이터로 사용
  editFormData.value = editSelectedItems.value.map(item => {
    // 각 항목의 ingredientDTO가 있는지 확인
    if (!item.ingredientDTO) {
      console.error('ingredientDTO가 없는 항목:', item);
      return null;
    }
    
    return {
      ...item,
      ingredientDTO: {
        ...item.ingredientDTO
      }
    };
  }).filter(item => item !== null); // null 항목 제거

  console.log('폼 데이터 설정 완료:', editFormData.value);
}

function handleEditInput(idx, key, value) {
  editFormData.value[idx][key] = value;
}

function handleEditIngredientInput(idx, key, value) {
  if (!editFormData.value[idx] || !editFormData.value[idx].ingredientDTO) {
    console.error('ingredientDTO가 없는 항목:', editFormData.value[idx]);
    return;
  }
  
  editFormData.value[idx].ingredientDTO[key] = value;
}

async function saveEditForm() {
  try {
    // 데이터 검증
    if (!editFormData.value || editFormData.value.length === 0) {
      window.showToast('error', '저장 실패', '수정할 데이터가 없습니다.');
      return;
    }

    console.log('원본 데이터:', editFormData.value);

    // 알레르기 정보가 빈 문자열인 경우 null로 변환
    const processedData = editFormData.value.map(item => {
      // 각 항목의 데이터 검증
      if (!item || !item.ingredientDTO) {
        console.error('잘못된 데이터 구조:', item);
        throw new Error('데이터 구조가 올바르지 않습니다.');
      }

      return {
        ...item,
        ingredientDTO: {
          ...item.ingredientDTO,
          allergicIngredients: item.ingredientDTO.allergicIngredients?.trim() || null
        }
      };
    });

    console.log('처리된 데이터:', processedData);

    // 성공 메시지 표시
    window.showToast('success', '저장 완료', '메뉴가 성공적으로 수정되었습니다.');
    
    // 단일 수정 모드인 경우 자동으로 검색 화면으로 돌아가기
    if (!isMultiEditMode.value) {
      editFormData.value = [];
      editSelectedItems.value = [];
    } else {
      // 다중 수정 모드인 경우 폼 데이터만 초기화
      editFormData.value = [];
    }
    
  } catch (error) {
    console.error('저장 중 오류 발생:', error);
    
    // 오류 메시지 표시
    let errorMessage = '저장 중 오류가 발생했습니다.';
    
    if (error.response?.status === 403) {
      errorMessage = '관리자 권한이 필요합니다. 다시 로그인해주세요.';
      // 로그인 모달 표시
      showLoginModal();
    } else if (error.response?.data) {
      errorMessage = error.response.data;
    } else if (error.message) {
      errorMessage = error.message;
    }
    
    window.showToast('error', '저장 실패', errorMessage);
  }
}

function toggleEditMode() {
  isEditMode.value = true;
  isMultiEditMode.value = !isMultiEditMode.value;
  editSearchQuery.value = '';
  editSearchResults.value = [];
  editSelectedItems.value = [];
  editFormData.value = [];
}

function removeEditItem(brand, idx) {
  // 해당 브랜드의 특정 인덱스 항목을 editFormData에서 제거
  const brandItems = editFormData.value.filter(item => item.brand === brand);
  const itemToRemove = brandItems[idx];
  const globalIndex = editFormData.value.findIndex(item => 
    item.brand === itemToRemove.brand && item.name === itemToRemove.name
  );
  
  if (globalIndex !== -1) {
    editFormData.value.splice(globalIndex, 1);
  }
}

function isEditSelected(coffee) {
  return editSelectedItems.value.some(sel => sel.brand === coffee.brand && sel.name === coffee.name);
}

// 수정 검색 결과를 브랜드별로 그룹화
const groupedEditSearchResults = computed(() => {
  const groups = {};
  editSearchResults.value.forEach(coffee => {
    if (!groups[coffee.brand]) {
      groups[coffee.brand] = { brand: coffee.brand, coffees: [] };
    }
    groups[coffee.brand].coffees.push(coffee);
  });
  return Object.values(groups);
});

// 수정 폼을 브랜드별로 그룹화
const groupedEditFormData = computed(() => {
  const groups = {};
  editFormData.value.forEach(item => {
    if (!groups[item.brand]) {
      groups[item.brand] = [];
    }
    groups[item.brand].push(item);
  });
  return groups;
});

function getItemIndex(brand, idx) {
  // 브랜드별 그룹에서의 인덱스를 전체 editFormData 인덱스로 변환
  const brandItems = editFormData.value.filter(item => item.brand === brand);
  const item = brandItems[idx];
  return editFormData.value.findIndex(dataItem => 
    dataItem.brand === item.brand && dataItem.name === item.name
  );
}
</script>

<style scoped>
.edit-section {
  background: none;
  border-radius: 0;
  max-width: none;
  margin: 0;
  padding: 0;
  box-shadow: none;
  position: static;
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
}

.edit-header {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 2rem;
  padding-bottom: 1rem;
  border-bottom: 2px solid #e0e6ed;
  width: 100%;
  max-width: 800px;
}

.edit-header-left {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.edit-header-right {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.help-btn {
  background: #e0e6ed;
  border: none;
  border-radius: 50%;
  width: 36px;
  height: 36px;
  font-size: 1.2rem;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s;
  margin-right: 0.5rem;
}

.help-btn:hover {
  background: #667eea;
  color: #fff;
}

.edit-header h2 {
  margin: 0;
  color: #2c3e50;
  font-size: 1.8rem;
}

.edit-search-section {
  margin-bottom: 2rem;
  width: 100%;
  max-width: 1200px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.search-input-group {
  display: flex;
  gap: 1rem;
  margin-bottom: 1rem;
  width: 100%;
  justify-content: center;
  align-items: center;
}

.search-input-group .search-input {
  flex: 1;
  max-width: 400px;
}

.search-input-group .search-btn {
  flex-shrink: 0;
}

.search-tips {
  color: #6c757d;
  font-size: 0.9rem;
  font-style: italic;
  text-align: center;
  width: 100%;
}

.results-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
  width: 100%;
  max-width: 1200px;
  padding: 0 1rem;
}

.results-header h3 {
  margin: 0;
  color: #2c3e50;
  font-size: 1.3rem;
}

.selection-info {
  display: flex;
  align-items: center;
  gap: 1rem;
  flex-wrap: wrap;
  justify-content: center;
}

.selected-count {
  color: #667eea;
  font-weight: 600;
}

.clear-selection-btn {
  background: #e0e6ed;
  color: #6c757d;
  border: none;
  border-radius: 15px;
  padding: 0.4rem 0.8rem;
  font-size: 0.8rem;
  cursor: pointer;
  transition: all 0.3s ease;
}

.clear-selection-btn:hover {
  background: #cbd5e0;
}

.edit-proceed-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 20px;
  padding: 0.6rem 1.2rem;
  font-size: 0.9rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
  white-space: nowrap;
}

.edit-proceed-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.4);
}

.edit-proceed-btn:disabled {
  background: #bdc3c7;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.edit-results-section {
  width: 100%;
  max-width: 1200px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.card-header input[type="checkbox"] {
  width: 18px;
  height: 18px;
  accent-color: #667eea;
}

.edit-proceed-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 25px;
  padding: 1rem 2rem;
  font-size: 1.1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.edit-proceed-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.4);
}

.edit-proceed-btn:disabled {
  background: #bdc3c7;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.no-results {
  text-align: center;
  padding: 3rem 1rem;
}

.no-results-content {
  max-width: 400px;
  margin: 0 auto;
}

.no-results-icon {
  font-size: 4rem;
  display: block;
  margin-bottom: 1rem;
}

.no-results h3 {
  color: #6c757d;
  margin-bottom: 0.5rem;
}

.no-results p {
  color: #adb5bd;
}

.edit-form-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
  padding-bottom: 1rem;
  border-bottom: 2px solid #e0e6ed;
}

.edit-form-header h3 {
  margin: 0;
  color: #2c3e50;
  font-size: 1.5rem;
}

.edit-form-header-right {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.back-to-search-btn {
  background: #6c757d;
  color: white;
  border: none;
  border-radius: 20px;
  padding: 0.6rem 1.2rem;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.3s ease;
}

.back-to-search-btn:hover {
  background: #5a6268;
  transform: translateY(-1px);
}

.edit-form {
  display: flex;
  flex-direction: column;
  gap: 2rem;
  width: 100%;
  max-width: 1200px;
  align-items: center;
}

.item-info h4 {
  margin: 0;
  color: #2c3e50;
  font-size: 1.2rem;
}

.field-group label {
  font-weight: 600;
  color: #2c3e50;
  font-size: 0.9rem;
}

.field-group input {
  padding: 0.8rem;
  border: 2px solid #e0e6ed;
  border-radius: 8px;
  font-size: 0.95rem;
  transition: all 0.3s ease;
}

.field-group input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.help-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0,0,0,0.25);
  z-index: 2000;
  display: flex;
  align-items: center;
  justify-content: center;
}

.help-modal {
  background: #fff;
  border-radius: 18px;
  padding: 2rem 2.5rem;
  box-shadow: 0 8px 32px rgba(44,62,80,0.18);
  max-width: 400px;
  width: 90vw;
  text-align: left;
  position: relative;
}

.help-modal h3 {
  margin-top: 0;
  margin-bottom: 1.2rem;
  color: #232946;
  font-size: 1.3rem;
  font-weight: 700;
}

.help-modal ol {
  padding-left: 1.2rem;
  margin-bottom: 1.5rem;
}

.help-modal li {
  margin-bottom: 1rem;
  font-size: 1rem;
  color: #232946;
}

.close-help-btn {
  background: #667eea;
  color: #fff;
  border: none;
  border-radius: 12px;
  padding: 0.6rem 1.5rem;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s;
  display: block;
  margin: 0 auto;
}

.close-help-btn:hover {
  background: #764ba2;
}

@media (max-width: 768px) {
  .edit-header {
    flex-direction: column;
    gap: 1rem;
    align-items: flex-start;
  }

  .results-header {
    flex-direction: column;
    gap: 1rem;
    align-items: flex-start;
  }
}
</style> 