<div align="center">

# 🗺️ BizMap

### 매장/시설을 지도 위에서 관리하고, 한 줄 스크립트로 외부 사이트에 임베드하는 **B2B 위치 관리 플랫폼**

<img src="./img/dashboard-hero.png" alt="BizMap 관리자 대시보드" width="850" />

</div>

---

## 1. 프로젝트 소개

BizMap은 기업이 자사 매장/시설을 지도 위에서 등록·조회·관리하고, 고객사 또는 자사 외부 사이트에 **매장 찾기 위젯을 한 줄의 스크립트로 임베드**할 수 있는 B2B 위치 관리 플랫폼입니다.

핵심 가치는 다음 세 가지입니다:

- **멀티테넌트 데이터 격리** — 회사별로 매장 데이터가 분리되어 타사 데이터 접근 불가
- **Google Maps Platform 풀스택 활용** — Maps JS / Places (New) / Geocoding 직접 연동
- **임베드 위젯** — 외부 도메인에서 한 줄로 사용 가능한 위치 위젯

---

## 2. 기술 스택

### Backend
![Java](https://img.shields.io/badge/Java-17-007396?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2-6DB33F?logo=springboot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring_Security-JWT-6DB33F?logo=springsecurity&logoColor=white)
![JPA](https://img.shields.io/badge/Spring_Data_JPA-Hibernate-59666C?logo=hibernate&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-4169E1?logo=postgresql&logoColor=white)
![PostGIS](https://img.shields.io/badge/PostGIS-3.4-336791)
![Gradle](https://img.shields.io/badge/Gradle-8-02303A?logo=gradle&logoColor=white)

### Frontend
![React](https://img.shields.io/badge/React-18-61DAFB?logo=react&logoColor=white)
![Vite](https://img.shields.io/badge/Vite-5-646CFF?logo=vite&logoColor=white)
![React Router](https://img.shields.io/badge/React_Router-v6-CA4245?logo=reactrouter&logoColor=white)
![Axios](https://img.shields.io/badge/Axios-1.x-5A29E4?logo=axios&logoColor=white)
![Chart.js](https://img.shields.io/badge/Chart.js-4-FF6384?logo=chartdotjs&logoColor=white)

### Google Maps Platform
![Maps JS](https://img.shields.io/badge/Maps_JavaScript_API-4285F4?logo=googlemaps&logoColor=white)
![Places New](https://img.shields.io/badge/Places_API_(New)-4285F4?logo=googlemaps&logoColor=white)
![Geocoding](https://img.shields.io/badge/Geocoding_API-4285F4?logo=googlemaps&logoColor=white)

### Infra
![Docker Compose](https://img.shields.io/badge/Docker_Compose-2496ED?logo=docker&logoColor=white)

---

## 3. 주요 기능

> 스크린샷 자리: 각 기능별 캡처 이미지를 추후 `docs/screenshots/` 에 추가

### 3-1. 매장 관리 (B2B 멀티테넌트)

- `company_id` 외래키 기반 데이터 격리. JWT payload 의 `companyId` 를 `SecurityContextHolder` → `SecurityUtils.getCurrentCompanyId()` 로 추출
- 모든 Store Service 메서드에서 **company_id 일치 검증** 필수, 불일치 시 `FORBIDDEN (403)` 반환
- CRUD + 카테고리 필터 + 키워드 검색 + 페이징

<div align="center">
  <img src="./img/store-management.png" alt="매장 목록" width="48%" />
  <img src="./img/store-detail.png" alt="매장 상세" width="48%" />
  <p><em>좌: 매장 목록 + 카테고리 필터 · 우: 매장 상세 정보</em></p>
</div>

### 3-2. Google Places Autocomplete

- 매장 등록 시 주소 텍스트 입력 → **자동완성 드롭다운**으로 후보 표시 → 클릭 시 좌표 자동 세팅
- **AutocompleteSuggestion API (2025년 신규 API)** 적용 — 기존 `AutocompleteService` deprecated 대응
- **세션 토큰 (`AutocompleteSessionToken`)** 으로 비용 최적화: Autocomplete 요청 N회 + Place Details 1회를 단일 세션으로 과금
- **300ms 디바운싱** 으로 키 입력마다 API 호출하는 낭비 제거
- 한국 주소만 노출 (`region: 'kr'`, `language: 'ko'`)

### 3-3. PostGIS 공간 쿼리

- `ST_DWithin(geography, geography, radius_meters)` 으로 반경 내 매장 필터
- `ST_Distance(geography, geography)` 로 거리 계산 후 가까운 순 정렬
- WGS84 타원체 측지선 기반이라 Haversine 구면 근사보다 **정확도 향상**
- GiST 공간 인덱스 활용 가능 → 매장 수 증가 시 후보군 사전 필터링으로 성능 확보
- Native query + Hibernate Spatial 의존성

<div align="center">
  <img src="./img/radius-search.png" alt="반경 기준 매장 검색" width="850" />
  <p><em>지정한 반경 내 매장만 거리순으로 정렬되어 지도에 표시</em></p>
</div>

### 3-4. 재고 기반 매장 찾기

- 상품 → 사이즈 선택 → **재고 1개 이상인 매장만 필터**
- 브라우저 `geolocation` 으로 사용자 현재 위치 획득 → **거리순 정렬**
- 매장 클릭 → 경로 안내 패널 → "자동차/도보 길찾기" 버튼 → **Google Maps 딥링크**로 새 탭 오픈
- 반경 입력 500ms 디바운스 적용

<div align="center">
  <img src="./img/inventory-filter.png" alt="상품/사이즈 필터" width="850" />
  <p><em>① 상품과 사이즈 선택 → 재고 보유 매장만 추려냄</em></p>
  <img src="./img/inventory-result-1.png" alt="재고 검색 결과" width="48%" />
  <img src="./img/inventory-result-2.png" alt="매장 상세 + 지도" width="48%" />
  <p><em>② 거리순 정렬 결과 + 매장 핀 표시</em></p>
  <img src="./img/inventory-route.png" alt="길찾기 - 차량 경로 선택" width="850" />
  <p><em>③ 매장 선택 → Google Maps 딥링크로 자동차/도보 길찾기 연결</em></p>
</div>

### 3-5. 매장 찾기 위젯 (핵심 차별점)

- **API 키 발급 시스템** — 회사별로 위젯 키 생성/조회/삭제 (`/api/widget-keys`), `allowedOrigin` 으로 도메인 제한 가능
- 고객사 웹사이트는 **단 두 줄**로 임베드 가능:
  ```html
  <script src="https://bizmap.example.com/widget/bizmap-widget.js?key=YOUR_KEY"></script>
  <div id="bizmap-widget" style="width:100%;height:400px;"></div>
  ```
- 백엔드가 **위젯 JS 를 동적으로 생성**해서 응답 (`@GetMapping("/widget/bizmap-widget.js")`) — 위젯 키와 Google Maps API 키를 서버 사이드에서 인라인 주입
- 위젯 JS 는 **Google Maps 스크립트를 동적 로드** 후 매장 핀 + InfoWindow 렌더링, 로드 실패 시 텍스트 fallback
- **전역 callback 큐** (`__bizmapMapsCallbacks`) 로 한 페이지에 위젯 여러 개 임베드해도 Google Maps 스크립트는 한 번만 로드
- 외부 도메인 CORS 별도 정책 (`/widget/**` 와일드카드 origin)

<div align="center">
  <img src="./img/widget-key-management.png" alt="위젯 키 관리 페이지" width="850" />
  <p><em>위젯 관리 페이지 — 회사별 API 키 발급/삭제 + 허용 도메인 설정</em></p>
  <img src="./img/widget-embed-example.png" alt="외부 사이트 임베드 예시" width="850" />
  <p><em>발급받은 키 한 줄로 외부 사이트에 매장 찾기 위젯 임베드</em></p>
</div>

### 3-6. JWT 인증

- **Access Token 30분 / Refresh Token 7일**
- `JwtAuthFilter (OncePerRequestFilter)` 가 모든 요청에서 `Authorization: Bearer` 추출 → 인증 객체 설정
- 프론트 Axios 응답 인터셉터에서 401 수신 시 `/api/auth/refresh` 자동 호출 → 새 Access Token 으로 원래 요청 재시도
- Refresh Token 은 `refresh_tokens` 테이블에 저장 (rotation 가능 구조)
- 비밀번호는 BCrypt (strength 10) 해시 저장

---

## 4. 아키텍처

### 4-1. 시스템 구성도

```mermaid
graph TB
    subgraph Client["🌐 클라이언트 영역"]
        FE["<b>Frontend (React 18)</b><br/>Vite · React Router v6<br/>/map · /find · /widget · /dashboard"]
        EXT["<b>고객사 웹사이트</b><br/>&lt;script src='…/widget.js'&gt;<br/>&lt;div id='bizmap-widget'&gt;"]
    end

    subgraph Server["⚙️ 백엔드 영역 (Spring Boot 3.2)"]
        SEC["<b>Spring Security + JwtAuthFilter</b><br/>OncePerRequestFilter · BCrypt"]
        CTRL["<b>Controller Layer</b><br/>Auth · Store · Inventory · Dashboard · Widget"]
        SVC["<b>Service Layer</b><br/>company_id 격리 검증 · 비즈니스 로직"]
        REPO["<b>Repository Layer</b><br/>Spring Data JPA + Native SQL"]
        WJS["<b>Widget JS Generator</b><br/>/widget/bizmap-widget.js<br/>키 + Maps API 인라인 주입"]
    end

    subgraph Data["💾 데이터 영역"]
        DB[("<b>PostgreSQL 15 + PostGIS 3.4</b><br/>ST_DWithin · ST_Distance<br/>GiST 공간 인덱스")]
    end

    subgraph GMP["🗺️ Google Maps Platform"]
        MAPS["Maps JavaScript API"]
        PLACES["Places API (New)<br/>AutocompleteSuggestion"]
        GEO["Geocoding API"]
    end

    FE -->|"REST + JWT<br/>Authorization: Bearer"| SEC
    SEC --> CTRL
    CTRL --> SVC
    SVC --> REPO
    REPO --> DB

    FE -.->|"Maps JS / Places / Geocoding"| GMP
    EXT -->|"위젯 스크립트 요청"| WJS
    WJS -->|"동적 생성된 JS 응답"| EXT
    EXT -.->|"Maps JS 동적 로드"| MAPS

    classDef client fill:#e3f2fd,stroke:#1976d2,stroke-width:2px,color:#000
    classDef server fill:#f3e5f5,stroke:#7b1fa2,stroke-width:2px,color:#000
    classDef data fill:#fff3e0,stroke:#f57c00,stroke-width:2px,color:#000
    classDef gmp fill:#e8f5e9,stroke:#388e3c,stroke-width:2px,color:#000
    class FE,EXT client
    class SEC,CTRL,SVC,REPO,WJS server
    class DB data
    class MAPS,PLACES,GEO gmp
```

### 4-2. 인증 플로우 (JWT + Refresh)

```mermaid
sequenceDiagram
    participant U as 사용자
    participant FE as Frontend (Axios)
    participant BE as Backend (JwtAuthFilter)
    participant DB as PostgreSQL

    U->>FE: 로그인 요청
    FE->>BE: POST /api/auth/login
    BE->>DB: Company 조회 + BCrypt 검증
    DB-->>BE: Company
    BE-->>FE: { accessToken (30분), refreshToken (7일) }
    FE->>FE: localStorage 저장

    Note over FE,BE: 이후 모든 요청
    FE->>BE: GET /api/stores (Bearer accessToken)
    BE->>BE: JwtAuthFilter → SecurityContext
    BE-->>FE: 200 OK

    Note over FE,BE: Access Token 만료
    FE->>BE: GET /api/stores (만료된 Token)
    BE-->>FE: 401 Unauthorized
    FE->>BE: POST /api/auth/refresh
    BE->>DB: RefreshToken 검증
    BE-->>FE: 새 accessToken
    FE->>BE: 원래 요청 재시도
    BE-->>FE: 200 OK
```

### 4-3. 위젯 임베드 플로우

```mermaid
sequenceDiagram
    participant Site as 고객사 사이트
    participant BE as BizMap Backend
    participant DB as PostgreSQL
    participant GMaps as Google Maps

    Site->>BE: GET /widget/bizmap-widget.js?key=KEY
    BE->>DB: WidgetKey 검증 + allowedOrigin 확인
    BE->>BE: 위젯 JS 동적 생성<br/>(key + Maps API key 인라인 주입)
    BE-->>Site: JS 응답
    Site->>Site: 스크립트 실행
    Site->>BE: GET /widget/stores?key=KEY
    BE-->>Site: 회사 매장 목록
    Site->>GMaps: Maps JS 동적 로드<br/>(__bizmapMapsCallbacks 큐)
    GMaps-->>Site: Maps API ready
    Site->>Site: 매장 핀 + InfoWindow 렌더
```

- **Frontend (React)** ↔ **Backend (Spring Boot)** — REST + JWT
- **Backend** ↔ **PostgreSQL + PostGIS** — JPA (CRUD) + Native SQL (공간 쿼리)
- **Frontend** + **Backend** ↔ **Google Maps Platform** — Maps JS / Places / Geocoding
- **Backend** → **Widget JS 동적 생성** → **고객사 웹사이트** 임베드

---

## 5. Google Maps Platform API 활용

Google Maps Platform 을 직접 연동

| API | 활용 내용 |
|-----|-----------|
| **Maps JavaScript API** | 매장 지도 시각화, 마커 + InfoWindow, 위젯 동적 로드 (`maps.googleapis.com/maps/api/js?...&libraries=marker&callback=...`), 마커 클릭 이벤트 |
| **Places API (New)** | 주소 자동완성 (`AutocompleteSuggestion.fetchAutocompleteSuggestions`), Place 상세 (`new Place({id}).fetchFields(['formattedAddress','location'])`), **세션 토큰 비용 최적화**, 한국 주소 제한 (`region: 'kr'`) |
| **Geocoding API** | 매장 등록 시 주소 ↔ 좌표 변환 (Autocomplete 도입 전 기본 흐름) |

### 비용/UX 최적화 노하우

- **세션 토큰**: Autocomplete N회 + Place Details 1회를 단일 세션으로 묶어 과금 최소화
- **300ms 디바운싱**: 키 입력마다 API 호출 → 사용자가 입력을 멈춘 후에만 호출
- **`region` / `language` 파라미터**: 불필요한 글로벌 결과 제외, 한국 주소만 노출
- **위젯 Maps 스크립트 단일 로드**: 같은 페이지에 위젯이 여러 개 있어도 `__bizmapMapsLoading` 가드로 Google Maps JS 는 한 번만 로드

---

## 6. 트러블슈팅 하이라이트

전체 14 + 1 건의 트러블슈팅 기록은 [TROUBLESHOOTING.md](./TROUBLESHOOTING.md) 에 정리되어 있습니다. 임팩트 있는 3건 요약:

### 6-1. Places API deprecated 대응 (`AutocompleteService` → `AutocompleteSuggestion`)
2025년 3월부터 Google 이 신규 GCP 프로젝트에서 기존 `google.maps.places.AutocompleteService` 를 비활성화. 콜백 패턴의 기존 코드를 **Promise 기반 신규 API** 로 마이그레이션. 동시에 필드명/옵션 차이도 정리:

| 항목 | 기존 (deprecated) | 신규 |
|------|------------------|------|
| 자동완성 | `AutocompleteService.getPlacePredictions()` (콜백) | `AutocompleteSuggestion.fetchAutocompleteSuggestions()` (Promise) |
| 상세 | `PlacesService.getDetails()` | `new Place({id}).fetchFields()` |
| 국가 제한 | `componentRestrictions: { country: 'kr' }` | `region: 'kr'` |
| 필드명 | `formatted_address`, `geometry` | `formattedAddress`, `location` |
| 세션 토큰 | 명시적 생성/폐기 | API 내부 관리 |

### 6-2. Routes API 정책 변경 대응 — 딥링크 방식으로 전환
경로 안내를 위해 Routes API v2 REST + Maps JS DirectionsService 를 차례로 시도했으나 **신규 GCP 프로젝트의 정책 변경**으로 빈 응답 / `ZERO_RESULTS` 반환 (4차 시도까지 모두 실패). API 의존성을 제거하고 **Google Maps 딥링크** 로 전환:
```
https://www.google.com/maps/dir/?api=1&destination={address}&travelmode=driving
```
- `origin` 을 생략해 구글맵이 사용자의 현재 위치를 자동 사용
- API 호출 0건 → 비용 0 + 정책 변경에 영향 없음
- 카카오맵, 네이버맵 등 실서비스에서 검증된 패턴

### 6-3. PostGIS `::geography` 캐스팅 문법 오류
Native query 에서 PostgreSQL 표준 `::geography` 캐스팅 사용 시 500 에러 발생. **Spring Data JPA 의 네이티브 쿼리 파서가 `:` 를 named parameter 접두사로 인식**하기 때문에 `::geography` 의 두 번째 `:` 가 `geography` 라는 바인딩 파라미터로 오해석됨:
```sql
-- Before (실패)
ST_MakePoint(s.longitude, s.latitude)::geography

-- After (ANSI SQL CAST)
CAST(ST_MakePoint(s.longitude, s.latitude) AS geography)
```
ANSI 표준 `CAST(... AS ...)` 로 교체하면 `:` 를 포함하지 않아 우회 가능.

---

## 7. 로컬 실행 방법

### 1) PostgreSQL + PostGIS 기동
```bash
docker-compose up -d
```
- 컨테이너: `postgis/postgis:15-3.4`
- 포트: `5433` (로컬 PostgreSQL 과 충돌 방지)

### 2) Backend 실행
```bash
cd backend
./gradlew bootRun
```
- 서버: http://localhost:8080
- 환경 변수 필요: `GOOGLE_MAPS_API_KEY` (위젯 JS 가 사용)

### 3) Frontend 실행
```bash
cd frontend
npm install
npm run dev
```
- 클라이언트: http://localhost:5173

### 4) 환경 변수
`frontend/.env`:
```
VITE_API_URL=http://localhost:8080/api
VITE_GOOGLE_MAPS_KEY=your_google_maps_key
```

### 5) 테스트 계정 (시드 데이터)
| 회사 | 이메일 | 비밀번호 |
|------|--------|----------|
| 유니클로 | uniqlo@bizmap.com | password123 |

시드 데이터: 매장 20개, 상품 5종 × 사이즈 4종, 매장별 재고 데이터 포함.

---

## 8. 개발 과정

본 프로젝트는 **Claude Code 멀티에이전트 워크플로우**를 활용하여 설계/구현되었습니다.

### 계층형 CLAUDE.md 구조
- 루트 [`CLAUDE.md`](./CLAUDE.md): 비즈니스 규칙, API 컨벤션, 에러 코드, Agent 위임 기준
- [`backend/CLAUDE.md`](./backend/CLAUDE.md): 백엔드 레이어 규칙 (Controller → Service → Repository), 보안 규칙, 도메인별 위임
- [`backend/src/main/java/com/bizmap/{auth,store,inventory,widget}/CLAUDE.md`](./backend): 도메인별 작업 범위와 엔드포인트 명세
- [`frontend/CLAUDE.md`](./frontend/CLAUDE.md): 프론트엔드 기술 스택, 라우팅, Axios 인터셉터 규칙, 커스텀 훅 목록

### Skills, Hooks 활용
- **Skills**: 코드 simplify, schedule, claude-api 등 재사용 가능한 작업 단위
- **Hooks**: 코드 작성/수정 시점에 자동으로 트리거되는 품질 검증 훅
- **TodoWrite / Plan mode**: 작업 단위 분해와 사전 설계 검증

### 결과
14 + 1 건의 트러블슈팅 기록 ([TROUBLESHOOTING.md](./TROUBLESHOOTING.md)) 과 7개 Phase 의 단계별 진행 기록 ([PROGRESS.md](./PROGRESS.md)) 으로 의사결정 과정과 실패 사례까지 모두 추적 가능합니다.

---

이 프로젝트는 Claude Code 를 활용하여 설계/구현되었습니다.
