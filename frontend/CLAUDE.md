# Frontend Lead Agent

## 기술 스택
React 18, React Router v6, Axios, Chart.js, @googlemaps/js-api-loader

## 디렉토리 구조
src/
├── api/          Axios 인스턴스 + API 함수 모음
├── hooks/        커스텀 훅
├── pages/        라우팅 단위 페이지
├── components/   재사용 컴포넌트
└── utils/        유틸 함수

## 라우팅 구조
/login          LoginPage       (비인증)
/register       RegisterPage    (비인증)
/map            MapPage         (인증 필요, 메인)
/stores         StoreListPage   (인증 필요)
/stores/new     StoreFormPage   (인증 필요)
/stores/:id     StoreDetailPage (인증 필요)
/dashboard      DashboardPage   (인증 필요)

## Axios 인스턴스 규칙 (api/axios.js)
- baseURL: import.meta.env.VITE_API_URL
- 요청 인터셉터: localStorage의 accessToken → Authorization 헤더 자동 주입
- 응답 인터셉터:
  - 401 수신 → /api/auth/refresh 호출 → 성공 시 원래 요청 재시도
  - refresh 실패 → localStorage 초기화 → /login 리다이렉트

## 커스텀 훅 목록 (hooks/)
- useAuth.js       로그인/로그아웃, 토큰 저장/삭제, 인증 상태
- useStores.js     매장 목록, 검색/필터, 페이징
- useStore.js      단건 조회, 수정, 삭제
- useMap.js        구글맵 인스턴스 초기화, 마커 생성/제거
- useNearby.js     반경 검색 요청, 결과 상태
- useDashboard.js  대시보드 집계 데이터 조회

## Google Maps 규칙
- API Key: import.meta.env.VITE_GOOGLE_MAPS_KEY
- @googlemaps/js-api-loader로 로드
- 마커 클릭 → InfoWindow에 매장명/주소/전화번호 표시
- 매장 등록: 주소 입력 → Geocoding API → lat/lng 자동 세팅

## 구현 순서
1. api/axios.js (인터셉터 포함)
2. hooks/ 전체
3. pages/ (Login → Register → Map → StoreList → StoreForm → Dashboard)
4. components/ (공통 컴포넌트는 pages 작업 중 추출)
