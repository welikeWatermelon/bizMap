# BizMap PROGRESS

## 현재 상태
- [x] 프로젝트 초기 세팅
- [x] Common (예외처리/응답 래퍼)
- [x] Auth (회원가입/로그인/JWT)
- [x] Store CRUD
- [x] Store 반경 검색
- [x] Frontend 기반 세팅
- [x] 지도 페이지
- [x] 매장 목록/등록 페이지
- [x] 대시보드
- [x] Docker 배포
- [x] 재고 관리 시스템 (Product/ProductSize/StoreInventory)
- [x] 재고 기반 매장 찾기 API (PostGIS)
- [x] 매장 찾기 프론트엔드 페이지 (/find)
- [x] 코드 분석 및 리팩토링 (7건)
- [x] Places Autocomplete (주소 자동완성)
- [x] Routes API 경로 안내
- [x] 위젯 임베드 시스템 (외부 도메인 매장 지도)

## 완료된 작업
- 프로젝트 초기 세팅 (Spring Boot 3.2, Vite+React 18, Docker Compose)
- Common: ErrorCode, BizMapException, ApiResponse, GlobalExceptionHandler(로깅 추가), SecurityUtils
- Auth: Company/RefreshToken Entity, JWT(JwtProvider/JwtAuthFilter), SecurityConfig, AuthService, AuthController
- Store: Store Entity, StoreRepository(Haversine), CRUD + 반경검색 + 지도핀 API
- Dashboard: 집계 API (totalStores, categoryStats, recentStores)
- Frontend: Axios 인터셉터, 커스텀 훅 6개, 전체 페이지 7개
- 더미 데이터 (회사 2개, 매장 각 10개)
- README.md 작성
- API 동작 검증 완료 (회원가입/로그인/매장CRUD/반경검색/대시보드)

## Phase 2 완료 작업
- PostGIS 도입: docker-compose를 postgis/postgis:15-3.4 이미지로 변경, hibernate-spatial 추가
- Inventory 패키지 (com.bizmap.inventory): Product, ProductSize, StoreInventory 엔티티
- 상품 API: GET /api/products, GET /api/products/{id}/sizes (비인증)
- 재고 기반 매장 검색 API: GET /api/stores/search (PostGIS ST_DWithin/ST_Distance, 비인증)
- 프론트엔드 /find 페이지: 상품/사이즈 선택 → 지도 + 매장 목록 (거리순)
- 더미 데이터: 상품 5개, 사이즈 4종, 매장 10곳 재고 배치 (일부 재고 0)
- SecurityConfig: /api/products/**, /api/stores/search permitAll 추가
- ErrorCode: PRODUCT_NOT_FOUND 추가

## Phase 3 리팩토링 (TROUBLESHOOTING.md 참조)
- getMapPins() SELECT 최적화: 전체 엔티티 → 4개 필드 프로젝션 쿼리
- getNearbyStores() 거리 이중 계산 제거: DB 결과에 distance 포함, Java 재계산 제거
- getSizesByProductId() 불필요한 엔티티 로드 → existsById()로 교체
- GlobalExceptionHandler: MissingServletRequestParameterException, MethodArgumentTypeMismatchException 핸들러 추가 (500→400)
- FindStorePage handleStoreClick 버그 수정: mapRef(항상 null) → mapInstance
- FindStorePage geolocation 응답 후 지도 panTo 추가
- FindStorePage radius 입력 500ms 디바운스 적용

## Phase 4 Places Autocomplete
- [x] useAddressSearch.js 훅: AutocompleteSuggestion + 300ms 디바운싱 + 한국 주소 제한
- [x] AddressSearchInput.jsx 컴포넌트: 드롭다운 자동완성 UI
- [x] StoreFormPage.jsx 수정: Geocoding → Autocomplete 전환, 위도/경도 자동 세팅
- [x] AutocompleteService → AutocompleteSuggestion 마이그레이션 (deprecated 대응)
- [x] Places API (New) 별도 활성화

## Phase 5 경로 안내 (Google Maps 딥링크)
- [x] useRoute.js 훅: Google Maps 딥링크 URL 생성 (destination=주소, origin 생략)
- [x] FindStorePage.jsx 수정: 매장 클릭 시 경로 안내 패널, 자동차/도보 길찾기 버튼 → 구글맵 새 탭
- [x] Routes API v2 / DirectionsService 시도 및 실패 기록
- [x] geolocation 서울 범위 유효성 검사 추가
- [x] 매장 클릭/버튼 클릭 동작 분리 (탭 중복 열림 해결)

## Phase 6 트러블슈팅 문서 최종 정리
- [x] TROUBLESHOOTING.md: 총 14개 항목 (기존 1~10 + 신규 11~14)
- [x] PROGRESS.md 업데이트

## Phase 7 위젯 임베드 시스템
- [x] WidgetKey 엔티티/Repository (id, company_id, name, api_key, allowed_origin)
- [x] 위젯 키 발급 API: POST/GET/DELETE /api/widget-keys (인증 필요)
- [x] WidgetKeyService: UUID 기반 api_key 생성, 소유권 검증
- [x] 위젯 JS 동적 생성 엔드포인트: GET /widget/bizmap-widget.js?key=
- [x] 위젯 공개 API: GET /widget/api/stores, /widget/api/stores/nearby
- [x] SecurityConfig: /widget/** permitAll
- [x] CORS: /widget/** 경로별 와일드카드 origin 정책
- [x] ErrorCode: WIDGET_KEY_NOT_FOUND 추가
- [x] 프론트엔드 WidgetPage (/widget): 키 발급/목록/삭제 + embed 코드 복사 + iframe 미리보기
- [x] Navbar에 "위젯 관리" 메뉴 추가
- [x] backend/static/widget-test.html 작성 (key 쿼리 파라미터 지원)
- [x] TROUBLESHOOTING.md 15번 항목 추가
- [x] PROGRESS.md 업데이트

## Phase 8 위젯 안정화 & 시드 데이터 정리
- [x] 시드 계정 정리: 검증 불가 BCrypt 해시(알파/베타) 제거 → 단일 유니클로 계정으로 통합 (uniqlo@bizmap.com / password123)
- [x] data.sql 멱등 마이그레이션 블록 추가 (UPDATE/DELETE/INSERT)
- [x] 위젯 JS Google Maps 동적 로더 추가: WidgetPublicController에 @Value 주입, ensureMapsLoaded + 콜백 큐 + 단일 로드 가드
- [x] WidgetPage 미리보기 iframe → 직접 스크립트 주입 방식 전환 (X-Frame-Options 회피, 실제 임베드와 동일 동작)

## Phase 9 포트폴리오 README 작성
- [x] README.md 포트폴리오 버전 재작성: 8개 섹션 (소개/스택/기능/아키텍처/Maps API/트러블슈팅/실행/개발과정)
- [x] Google Maps Platform API 활용 경험 명시 (Maps JS / Places New / Geocoding)
- [x] 트러블슈팅 하이라이트 3건 요약 (Places deprecated / Routes 정책 / PostGIS CAST)
- [x] 기술 스택 뱃지 형식 적용

## 다음 작업
없음 - 모든 항목 완료

## 특이사항
- 로컬 PostgreSQL과 Docker 포트 충돌로 Docker 포트를 5433으로 변경 (docker-compose.yml, application.yml)
- Windows curl에서 한글 UTF-8 인코딩 문제 있음 - Content-Type에 charset=UTF-8 명시 필요
- Google Maps API 키를 frontend/.env에 설정해야 지도 기능 동작
- PostGIS 도입으로 docker-compose down -v 후 재기동 필요 (기존 postgres:15 → postgis/postgis:15-3.4)
