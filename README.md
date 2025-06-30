<div align="center">

# ☕ CoffeeAnalyze - 커피 비교 분석 시스템

**다양한 커피 브랜드의 메뉴를 비교하고 분석할 수 있는 웹 애플리케이션**

[![Vue.js](https://img.shields.io/badge/Vue.js-3.x-4FC08D?style=for-the-badge&logo=vue.js)](https://vuejs.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-6DB33F?style=for-the-badge&logo=spring-boot)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=java)](https://www.oracle.com/java/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql)](https://www.mysql.com/)

</div>

## 📋 목차
- [프로젝트 개요](#프로젝트-개요)
- [주요 기능](#주요-기능)
- [기술 스택](#기술-스택)
- [프로젝트 구조](#프로젝트-구조)
- [설치 및 실행](#설치-및-실행)
- [사용법](#사용법)

## 🎯 프로젝트 개요

현대사회에서 커피는 한 잔의 음료를 넘어서 삶의 일부가 되었습니다. 대한민국에는 많은 커피 브랜드와 전문점이 있는데, 각 프랜차이즈의 커피를 비교하는 시스템을 만들고자 합니다.

### 주요 특징
- **다중 브랜드 지원**: 스타벅스, 메가커피, 빽다방, 이디야, 컴포즈, 투썸플레이스
- **실시간 비교**: 최대 4개의 커피를 동시에 비교 분석
- **영양 정보 분석**: 칼로리, 카페인, 당류, 나트륨, 포화지방, 단백질 비교
- **용량 정보 표시**: 각 메뉴의 용량(ml) 정보 제공
- **관리자 기능**: 웹 크롤링을 통한 메뉴 데이터 자동 업데이트 및 다중 수정
- **세션 관리**: 페이지 닫기 시 자동 로그아웃되는 안전한 세션 관리
- **반응형 디자인**: 모바일과 데스크톱에서 모두 최적화된 사용자 경험

## ✨ 주요 기능

### 👥 일반 사용자 기능
- **커피 검색**: 브랜드별 커피 메뉴 검색 (정확도 기반 정렬)
- **카테고리 필터링**: 음료, 푸드, 디저트 등 카테고리별 필터링
- **다중 선택**: 최대 4개 커피 동시 선택 (토글 방식)
- **상세 비교**: 영양 정보, 용량, 알레르기 정보 비교
- **시각적 표시**: 색상 코딩을 통한 직관적인 비교 결과
- **모달 뷰**: 전체 화면 모달에서 상세 비교 결과 확인
- **토스트 알림**: 사용자 친화적인 알림 시스템

### 🔐 관리자 기능
- **JWT 인증**: 안전한 관리자 로그인 시스템
- **세션 관리**: 페이지 닫기 시 자동 로그아웃
- **통합 관리자 메뉴**: 드롭다운 형태의 직관적인 관리자 인터페이스
- **메뉴 업데이트**: 브랜드별 선택적 크롤링
- **실시간 크롤링**: Selenium을 활용한 웹 스크래핑
- **다중 수정 기능**: 여러 메뉴를 동시에 수정할 수 있는 관리 도구
- **데이터 관리**: MySQL 데이터베이스 관리

### 🎨 UI/UX 특징
- **모던 디자인**: 그라데이션과 그림자 효과
- **브랜드 컬러**: 각 브랜드별 고유 색상 적용
- **애니메이션**: 부드러운 호버 효과와 전환
- **직관적 인터페이스**: 사용자 친화적인 레이아웃
- **반응형 그리드**: 검색 결과의 반응형 카드 레이아웃
- **선택 상태 표시**: 명확한 선택/미선택 상태 구분

## 🛠️ 기술 스택

### Frontend
- **Vue.js 3**: Composition API 기반 반응형 프레임워크
- **Pinia**: 상태 관리 라이브러리
- **Vite**: 빠른 개발 서버 및 빌드 도구
- **CSS3**: 모던 스타일링 및 애니메이션

### Backend
- **Spring Boot 3**: Java 기반 웹 프레임워크
- **Spring Security**: JWT 기반 인증 시스템
- **Spring Data JPA**: 데이터베이스 ORM
- **Selenium**: 웹 크롤링 도구

### Database
- **MySQL 8.0**: 관계형 데이터베이스
- **JPA/Hibernate**: 객체 관계 매핑

### DevOps
- **Gradle**: 빌드 도구
- **ChromeDriver**: 웹 크롤링 드라이버

## 📁 프로젝트 구조

```
CoffeeAnalyze/
├── frontend/                 # Vue.js 프론트엔드
│   ├── src/
│   │   ├── components/       # Vue 컴포넌트
│   │   │   ├── CoffeeCompare.vue  # 메인 컴포넌트
│   │   │   └── Toast.vue     # 토스트 알림 컴포넌트
│   │   ├── stores/          # Pinia 상태 관리
│   │   ├── services/        # API 서비스
│   │   ├── assets/          # CSS 및 이미지
│   │   └── types/           # TypeScript 타입 정의
│   └── package.json
├── src/main/java/
│   └── com/example/coffeeproject/
│       ├── Coffee/          # 커피 관련 모듈
│       │   ├── Controller/  # REST API 컨트롤러
│       │   ├── DTO/         # 데이터 전송 객체
│       │   ├── Model/       # 엔티티 클래스
│       │   ├── Repository/  # 데이터 접근 계층
│       │   └── service/     # 비즈니스 로직
│       ├── User/            # 사용자/관리자 모듈
│       │   ├── Controller/  # 관리자 컨트롤러
│       │   ├── model/       # 관리자 모델
│       │   ├── repository/  # 관리자 리포지토리
│       │   └── Service/     # 관리자 서비스
│       └── Utill/           # 유틸리티
│           ├── config/      # 설정 클래스
│           ├── filter/      # JWT 필터
│           └── util/        # 유틸리티 클래스
└── src/main/resources/
    └── application.yml     # 애플리케이션 설정
```

## 🚀 설치 및 실행

### Prerequisites
- Java 17 이상
- Node.js 16 이상
- MySQL 8.0 이상
- Chrome 브라우저

### 방법 1: 개별 실행 (권장)

#### Backend 실행
```bash
# 프로젝트 루트 디렉토리에서
./gradlew bootRun
```

#### Frontend 실행
```bash
# frontend 디렉토리에서
cd frontend
npm install
npm run dev
```

### 방법 2: 통합 실행

#### Windows 배치 파일 사용
```bash
# 배치 파일 실행
start-dev.bat
```

#### PowerShell 스크립트 사용
```powershell
# PowerShell에서 실행
.\start-dev.ps1
```

### 데이터베이스 설정
1. MySQL 데이터베이스 생성
2. `src/main/resources/application.yml`에서 데이터베이스 연결 정보 설정
3. 애플리케이션 실행 시 자동으로 테이블 생성

## 📖 사용법

### 일반 사용자
1. **커피 검색**: 검색창에 커피 이름 입력
2. **카테고리 필터**: 드롭다운에서 카테고리 선택 (선택사항)
3. **커피 선택**: 원하는 커피들을 클릭하여 선택 (최대 4개, 토글 방식)
4. **비교 결과 확인**: "📊 비교 결과 보기" 버튼 클릭
5. **상세 분석**: 모달에서 영양 정보, 용량, 알레르기 정보 비교 확인

### 관리자
1. **로그인**: 좌상단 "🔐 관리자 로그인" 버튼 클릭
2. **관리자 메뉴**: 우측상단 "☰ 메뉴" 버튼 클릭
3. **메뉴 업데이트**: "📊 메뉴 업데이트" 섹션에서 원하는 브랜드 선택
4. **다중 수정**: "✏️ 수정하기" 버튼으로 다중 수정 모드 진입
5. **로그아웃**: "🚪 로그아웃" 버튼으로 세션 종료

## 🔌 API 문서

### 인증 API
- `POST /admin/login` - 관리자 로그인
- `POST /admin/logout` - 관리자 로그아웃
- `GET /admin/me` - 현재 로그인된 관리자 정보

### 커피 API
- `GET /getCoffee/search?name={name}` - 커피 검색
- `POST /getCoffee/update` - 커피 정보 업데이트 (인증 필요)

### 크롤링 API (인증 필요)
- `GET /craw/mega_coffee` - 메가커피 크롤링
- `GET /craw/paiks` - 빽다방 크롤링
- `GET /craw/starBucks` - 스타벅스 크롤링
- `GET /craw/ediya` - 이디야 크롤링
- `GET /craw/compose` - 컴포즈커피 크롤링


<div align="center">

**☕ CoffeeAnalyze로 더 나은 커피 선택을 경험해보세요! ☕**

</div>

