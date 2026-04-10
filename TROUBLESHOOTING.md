# BizMap Troubleshooting & Refactoring

## 개요
BizMap 개발 과정에서 발견한 코드 품질 이슈, 버그, API 연동 문제와 해결 과정을 기록합니다.

- 1~8번: Phase 1~3 코드 리팩토링 및 버그 수정
- 9번: Places Autocomplete 도입 (주소 자동완성)
- 10번: 경로 안내 기능 (Directions API → 딥링크 전환)
- 11~14번: Google Maps API 연동 트러블슈팅 (API 키, deprecated API, 이벤트 버블링)
- 15번: 위젯 임베드 시스템 (외부 도메인 CORS, JS 동적 생성, 정적 리소스 인증 우회)

---

## 1. getMapPins() 전체 엔티티 조회 — 불필요한 데이터 로딩

### 문제
지도 핀 API(`GET /api/stores/map`)는 id, name, latitude, longitude 4개 필드만 필요한데,
`findAllActiveByCompanyId()`가 Store 엔티티 전체(13개 컬럼)를 SELECT 하고 있었다.
매장 수가 늘어날수록 불필요한 메모리 사용과 네트워크 전송량이 증가한다.

### 원인
초기 구현 시 기존 Repository 메서드를 재활용하여 빠르게 개발한 결과,
지도 핀 전용 쿼리를 분리하지 않았다.

### Before
```java
// StoreRepository.java
@Query("SELECT s FROM Store s WHERE s.companyId = :companyId AND s.isActive = true")
List<Store> findAllActiveByCompanyId(@Param("companyId") Long companyId);

// StoreService.java
public List<MapPinResponse> getMapPins() {
    Long companyId = SecurityUtils.getCurrentCompanyId();
    return storeRepository.findAllActiveByCompanyId(companyId).stream()
            .map(MapPinResponse::from)
            .toList();
}
```

### After
```java
// StoreRepository.java — 필요한 4개 컬럼만 SELECT하는 전용 쿼리 추가
@Query("SELECT s.id, s.name, s.latitude, s.longitude FROM Store s " +
        "WHERE s.companyId = :companyId AND s.isActive = true")
List<Object[]> findMapPinsByCompanyId(@Param("companyId") Long companyId);

// StoreService.java
public List<MapPinResponse> getMapPins() {
    Long companyId = SecurityUtils.getCurrentCompanyId();
    return storeRepository.findMapPinsByCompanyId(companyId).stream()
            .map(row -> MapPinResponse.builder()
                    .id((Long) row[0])
                    .name((String) row[1])
                    .latitude((Double) row[2])
                    .longitude((Double) row[3])
                    .build())
            .toList();
}
```

### 개선 효과
- SELECT 컬럼 수 13개 → 4개로 감소 (약 70% 데이터 전송량 절감)
- JPA 영속성 컨텍스트에 불필요한 엔티티 적재 방지

---

## 2. getNearbyStores() 거리 이중 계산 — DB와 Java에서 동일 연산 반복

### 문제
`findNearby()` JPQL에서 Haversine 공식으로 반경 필터링을 수행한 뒤,
`NearbyStoreResponse.from(store, lat, lng)`에서 동일한 Haversine 거리 계산을 Java로 다시 수행하고 있었다.
매장 100개 조회 시 Haversine 계산이 200회(DB 100 + Java 100) 실행된다.

### 원인
기존 JPQL이 Store 엔티티만 반환하여 DB에서 계산한 거리 값을 가져올 수 없었고,
DTO 변환 시 거리를 별도로 계산해야 했다.

### Before
```java
// StoreRepository.java — Store 엔티티만 반환, 거리 값 없음
@Query("SELECT s FROM Store s WHERE s.companyId = :companyId AND s.isActive = true " +
        "AND (6371 * acos(...)) <= :radius")
List<Store> findNearby(...);

// NearbyStoreResponse.java — Java에서 거리 재계산
public static NearbyStoreResponse from(Store store, double lat, double lng) {
    double distance = calculateDistance(lat, lng, store.getLatitude(), store.getLongitude());
    return NearbyStoreResponse.builder()
            .id(store.getId()).name(store.getName())
            .address(store.getAddress())
            .distance(Math.round(distance * 100.0) / 100.0)
            .build();
}
```

### After
```java
// StoreRepository.java — distance를 쿼리 결과에 포함 + 정렬
@Query(value = """
        SELECT s.id, s.name, s.address,
               (6371 * acos(cos(radians(:lat)) * cos(radians(s.latitude))
               * cos(radians(s.longitude) - radians(:lng))
               + sin(radians(:lat)) * sin(radians(s.latitude)))) AS distance
        FROM stores s
        WHERE s.company_id = :companyId AND s.is_active = true
        AND (6371 * acos(...)) <= :radius
        ORDER BY distance
        """, nativeQuery = true)
List<Object[]> findNearby(...);

// StoreService.java — DB가 반환한 distance 직접 사용
return storeRepository.findNearby(companyId, lat, lng, radius).stream()
        .map(row -> NearbyStoreResponse.builder()
                .id(((Number) row[0]).longValue())
                .name((String) row[1])
                .address((String) row[2])
                .distance(Math.round(((Number) row[3]).doubleValue() * 100.0) / 100.0)
                .build())
        .toList();
```

### 개선 효과
- Haversine 계산 횟수: N*2 → N으로 50% 감소
- 결과가 DB에서 거리순 정렬되어 반환 (기존에는 정렬 없음)
- NearbyStoreResponse의 calculateDistance() 36줄 사용되지 않는 코드 제거 가능

---

## 3. getSizesByProductId() 불필요한 전체 엔티티 로드

### 문제
상품 사이즈 조회 시 `productRepository.findById(productId)`로 Product 엔티티 전체를 로드한 뒤,
`product.getId()`(= 입력값과 동일)만 사용하여 사이즈를 조회하고 있었다.
존재 확인만 필요한 상황에서 불필요한 엔티티 역직렬화가 발생한다.

### 원인
존재 확인과 FK 참조를 하나의 패턴(`findById().orElseThrow()`)으로 처리하려 한 결과,
실제로는 ID만 필요한데 전체 엔티티를 가져오게 되었다.

### Before
```java
public List<SizeResponse> getSizesByProductId(Long productId) {
    Product product = productRepository.findById(productId)
            .orElseThrow(() -> new BizMapException(ErrorCode.PRODUCT_NOT_FOUND));
    return productSizeRepository.findAllByProductId(product.getId()).stream()
            .map(SizeResponse::from)
            .toList();
}
```

### After
```java
public List<SizeResponse> getSizesByProductId(Long productId) {
    if (!productRepository.existsById(productId)) {
        throw new BizMapException(ErrorCode.PRODUCT_NOT_FOUND);
    }
    return productSizeRepository.findAllByProductId(productId).stream()
            .map(SizeResponse::from)
            .toList();
}
```

### 개선 효과
- 존재 확인 쿼리: `SELECT *` → `SELECT count(id)` (또는 `SELECT 1`)로 변경
- Product 엔티티 역직렬화 및 영속성 컨텍스트 적재 비용 제거

---

## 4. GlobalExceptionHandler 누락 — 필수 파라미터 없으면 500 반환

### 문제
`GET /api/stores/search`에서 필수 파라미터(lat, lng, productId, size)를 누락하면
`MissingServletRequestParameterException`이 발생하는데,
이를 처리하는 핸들러가 없어 `Exception` catch-all에 걸려 500 에러로 응답했다.
클라이언트 입력 오류인데 서버 오류로 보고되는 문제가 있었다.

### 원인
GlobalExceptionHandler 구현 시 Spring MVC의 파라미터 바인딩 예외를 고려하지 않았다.
`@RequestBody` 검증용 `MethodArgumentNotValidException`만 처리하고,
`@RequestParam` 검증용 예외는 누락했다.

### Before
```java
// 400으로 처리하는 핸들러 없음 → Exception catch-all로 500 반환
@ExceptionHandler(Exception.class)
public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
    log.error("Unhandled exception", e);
    return ResponseEntity.status(500)
            .body(ApiResponse.error("INTERNAL_ERROR", "서버 내부 오류가 발생했습니다."));
}
```

### After
```java
@ExceptionHandler(MissingServletRequestParameterException.class)
public ResponseEntity<ApiResponse<Void>> handleMissingParam(MissingServletRequestParameterException e) {
    String message = "필수 파라미터 '" + e.getParameterName() + "'이(가) 누락되었습니다.";
    return ResponseEntity.badRequest()
            .body(ApiResponse.error("INVALID_INPUT", message));
}

@ExceptionHandler(MethodArgumentTypeMismatchException.class)
public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
    String message = "파라미터 '" + e.getName() + "'의 타입이 올바르지 않습니다.";
    return ResponseEntity.badRequest()
            .body(ApiResponse.error("INVALID_INPUT", message));
}
```

### 개선 효과
- 파라미터 누락 시 500 → 400으로 올바른 HTTP 상태 코드 반환
- 어떤 파라미터가 누락/잘못됐는지 구체적 메시지 제공
- 불필요한 서버 에러 로그 감소

---

## 5. FindStorePage handleStoreClick — mapRef.current가 항상 null (버그)

### 문제
매장 목록에서 매장을 클릭하면 지도가 해당 위치로 이동해야 하는데,
`mapRef.current`를 사용하고 있었다. useMap 훅에서 `mapRef`는 빈 ref(`useRef(null)`)로
선언만 되고 실제 Map 인스턴스가 할당되지 않는다.
실제 Map 인스턴스는 `mapInstance`(mapInstanceRef)에 저장되므로,
매장 클릭 시 지도 이동이 전혀 작동하지 않는 버그였다.

### 원인
useMap 훅이 `mapRef`와 `mapInstance` 두 개의 ref를 반환하는데,
FindStorePage에서 잘못된 ref를 사용했다.

### Before
```javascript
// FindStorePage.jsx
const { mapRef, initMap, addMarkers, showInfoWindow, clearMarkers } = useMap();

const handleStoreClick = (store) => {
    if (!mapRef.current) return;  // 항상 null → 항상 early return
    mapRef.current.panTo({ lat: store.latitude, lng: store.longitude });
    mapRef.current.setZoom(15);
};
```

### After
```javascript
const { mapInstance, initMap, addMarkers, showInfoWindow, clearMarkers } = useMap();

const handleStoreClick = (store) => {
    if (!mapInstance.current) return;
    mapInstance.current.panTo({ lat: store.latitude, lng: store.longitude });
    mapInstance.current.setZoom(15);
};
```

### 개선 효과
- 매장 클릭 시 지도 이동 기능이 정상 동작하도록 버그 수정
- 사용자가 매장 목록에서 클릭하면 해당 위치로 지도 panTo + zoom 15 적용

---

## 6. FindStorePage 지도 초기화 후 위치 미반영 — geolocation 응답 무시

### 문제
페이지 로드 시 지도가 DEFAULT_CENTER(서울 시청)로 먼저 초기화된다.
이후 geolocation API가 사용자 실제 위치를 응답하면 `userLocation` state가 업데이트되지만,
`mapReady=true` 가드에 의해 지도 재초기화가 스킵되어 지도가 사용자 위치로 이동하지 않았다.

### 원인
지도 초기화 useEffect에서 `!mapReady` 조건만 확인하고,
이미 초기화된 후의 위치 변경을 처리하는 분기가 없었다.

### Before
```javascript
useEffect(() => {
    if (mapContainerRef.current && !mapReady) {
      initMap(mapContainerRef.current, { center: userLocation, zoom: 12 });
      setMapReady(true);
    }
}, [userLocation, initMap, mapReady]);
```

### After
```javascript
useEffect(() => {
    if (mapContainerRef.current && !mapReady) {
      initMap(mapContainerRef.current, { center: userLocation, zoom: 12 });
      setMapReady(true);
    } else if (mapReady && mapInstance.current) {
      mapInstance.current.panTo(userLocation);
    }
}, [userLocation, initMap, mapReady, mapInstance]);
```

### 개선 효과
- 위치 권한 허용 시 지도가 사용자 실제 위치로 자동 이동
- 지도 재초기화 없이 panTo로 효율적 위치 변경

---

## 7. FindStorePage 반경 입력 디바운스 없음 — 키 입력마다 API 호출

### 문제
반경(km) 입력 필드에서 값을 변경할 때마다 즉시 검색 API가 호출되었다.
예를 들어 "30"을 "50"으로 변경하면: "5" 입력 시 1회, "0" 입력 시 1회로
총 2회의 불필요한 API 호출이 발생한다.

### 원인
radius state 변경이 useEffect 의존성 배열에 직접 연결되어
debounce 없이 모든 변경이 즉시 검색을 트리거했다.

### Before
```javascript
const [radius, setRadius] = useState(30);

const handleSearch = useCallback(() => {
    search({ ..., radius });
}, [selectedProduct, selectedSize, userLocation, radius, search]);

useEffect(() => {
    if (selectedProduct && selectedSize) handleSearch();
}, [selectedProduct, selectedSize, radius, handleSearch]);
```

### After
```javascript
const [radius, setRadius] = useState(30);
const [debouncedRadius, setDebouncedRadius] = useState(30);

useEffect(() => {
    const timer = setTimeout(() => setDebouncedRadius(radius), 500);
    return () => clearTimeout(timer);
}, [radius]);

const handleSearch = useCallback(() => {
    search({ ..., radius: debouncedRadius });
}, [selectedProduct, selectedSize, userLocation, debouncedRadius, search]);

useEffect(() => {
    if (selectedProduct && selectedSize) handleSearch();
}, [selectedProduct, selectedSize, debouncedRadius, handleSearch]);
```

### 개선 효과
- 500ms 디바운스로 불필요한 중간 API 호출 제거
- "30" → "50" 변경 시 API 호출 2회 → 1회로 감소
- 서버 부하 및 네트워크 트래픽 절감

---

## 8. PostGIS 도입 — Docker 이미지 변경 및 네이티브 쿼리 전환

### 배경
재고 기반 매장 검색 기능(`GET /api/stores/search`)을 추가하면서
기존 Haversine JPQL 방식 대신 PostGIS의 공간 함수를 도입했다.

### 변경 내용

#### 8-1. Docker 이미지 교체

기존 `postgres:15` 이미지는 PostGIS 확장이 포함되어 있지 않다.
`postgis/postgis:15-3.4` 이미지로 교체하여 PostGIS를 사용할 수 있도록 했다.

```yaml
# Before — docker-compose.yml
services:
  db:
    image: postgres:15

# After
services:
  db:
    image: postgis/postgis:15-3.4
```

이 이미지는 PostgreSQL 15 위에 PostGIS 3.4가 사전 설치된 공식 이미지다.
기존 데이터와 호환되지만, **볼륨을 새로 생성해야** PostGIS 라이브러리가 로드된다.
따라서 전환 시 반드시 `docker-compose down -v` 후 재기동해야 한다.

#### 8-2. PostGIS Extension 활성화

`postgis/postgis` 이미지에 PostGIS가 설치되어 있어도
데이터베이스별로 extension을 명시적으로 활성화해야 공간 함수를 사용할 수 있다.
Spring Boot가 실행하는 `data.sql` 최상단에 추가했다.

```sql
-- data.sql 최상단
CREATE EXTENSION IF NOT EXISTS postgis;
```

`IF NOT EXISTS`를 사용하여 재기동 시 중복 생성 에러를 방지한다.
활성화 확인:
```bash
docker exec bizmap-db psql -U bizmap -d bizmap -c "SELECT PostGIS_version();"
# 결과: 3.4 USE_GEOS=1 USE_PROJ=1 USE_STATS=1
```

#### 8-3. Hibernate Spatial 의존성 추가

Spring Data JPA에서 PostGIS 타입과 함수를 인식하려면
`hibernate-spatial` 모듈이 필요하다.

```gradle
// build.gradle
dependencies {
    implementation 'org.hibernate.orm:hibernate-spatial'
}
```

Spring Boot 3.2 + Hibernate 6 환경에서는 별도 버전 지정 없이
Spring의 dependency management가 호환 버전을 자동 결정한다.

#### 8-4. 네이티브 쿼리에서 PostGIS 공간 함수 사용

기존 매장 반경 검색(`/api/stores/nearby`)은 JPQL에서 Haversine 공식을 직접 작성했다.
새로운 재고 기반 매장 검색(`/api/stores/search`)은 PostGIS 네이티브 함수를 사용한다.

```java
// StoreInventoryRepository.java
@Query(value = """
        SELECT s.id AS store_id, s.name AS store_name, s.address,
               ST_Distance(
                   CAST(ST_MakePoint(s.longitude, s.latitude) AS geography),
                   CAST(ST_MakePoint(:lng, :lat) AS geography)
               ) / 1000.0 AS distance,
               si.quantity,
               s.latitude, s.longitude
        FROM store_inventory si
        JOIN stores s ON s.id = si.store_id
        WHERE si.product_id = :productId
          AND si.size = :size
          AND si.quantity > 0
          AND s.is_active = true
          AND ST_DWithin(
              CAST(ST_MakePoint(s.longitude, s.latitude) AS geography),
              CAST(ST_MakePoint(:lng, :lat) AS geography),
              :radiusMeters
          )
        ORDER BY distance
        """, nativeQuery = true)
List<Object[]> findStoresWithInventory(...);
```

**사용한 PostGIS 함수:**

| 함수 | 역할 |
|------|------|
| `ST_MakePoint(lng, lat)` | 경도/위도로 Point geometry 생성 (경도가 x, 위도가 y) |
| `CAST(... AS geography)` | geometry → geography 캐스팅 (미터 단위 계산 활성화) |
| `ST_DWithin(geogA, geogB, meters)` | 두 지점이 지정 미터 이내인지 boolean 반환 (공간 인덱스 활용 가능) |
| `ST_Distance(geogA, geogB)` | 두 지점 간 측지선 거리를 미터 단위로 반환 |

**geography 캐스팅이 필요한 이유:**
- `geometry` 타입은 평면(Cartesian) 좌표계로 계산하여 위경도 데이터에서 거리가 부정확하다
- `geography` 타입은 WGS84 타원체 위에서 측지선 거리를 계산하여 실제 지표면 거리(미터)를 반환한다
- `ST_DWithin`은 geography 타입에서 미터 단위 반경을 받으므로, 서비스 레이어에서 `radius(km) * 1000`으로 변환한다

#### 8-5. `::geography` 캐스팅 문법 이슈

최초 구현 시 PostgreSQL 표준 캐스팅 문법 `::geography`를 사용했으나 500 에러가 발생했다.

```java
// 에러 발생 — Spring Data JPA가 ::를 named parameter 접두사로 오인식
ST_MakePoint(s.longitude, s.latitude)::geography
// `:geography`를 `:geography`라는 바인딩 파라미터로 해석 → 바인딩 실패 → 500

// 해결 — ANSI SQL 표준 CAST 문법 사용
CAST(ST_MakePoint(s.longitude, s.latitude) AS geography)
```

Spring Data JPA의 네이티브 쿼리 파서는 `:`를 named parameter 접두사로 인식한다.
PostgreSQL의 `::` 캐스팅 문법에서 두 번째 `:`가 파라미터 시작으로 오인식되어
`geography`라는 이름의 바인딩 파라미터를 찾으려 하고 실패한다.
`CAST(... AS ...)` 문법은 `:`를 포함하지 않아 이 문제를 회피한다.

#### 8-6. Haversine vs PostGIS 비교

| 항목 | Haversine (JPQL) | PostGIS (네이티브) |
|------|-----------------|-------------------|
| 쿼리 위치 | JPQL (DB 독립적) | 네이티브 SQL (PostgreSQL 전용) |
| 거리 계산 | 구면 삼각법 근사 | WGS84 타원체 측지선 (더 정확) |
| 인덱스 | 불가능 (WHERE절 함수) | GiST 공간 인덱스 활용 가능 |
| 성능 (대규모) | 전체 테이블 스캔 | 공간 인덱스로 후보군 사전 필터링 |
| 이식성 | 모든 RDBMS | PostgreSQL + PostGIS 전용 |
| 사용 위치 | `/api/stores/nearby` (기존) | `/api/stores/search` (재고 검색) |

현재 프로젝트에서는 두 방식이 공존한다.
기존 `/api/stores/nearby`는 Haversine JPQL을 유지하고,
신규 `/api/stores/search`는 PostGIS를 사용한다.
매장 수가 수만 건 이상으로 증가하면 기존 nearby도 PostGIS로 전환하고
`stores` 테이블에 GiST 공간 인덱스를 추가하는 것을 권장한다.

```sql
-- 향후 성능 최적화 시 추가할 공간 인덱스
CREATE INDEX idx_stores_location ON stores
USING GIST (ST_MakePoint(longitude, latitude)::geography);
```

---

## 9. Places Autocomplete 도입 — 주소 입력 UX 개선 및 API 비용 최적화

### 배경
매장 등록 시 주소를 직접 입력하고 "좌표변환" 버튼으로 Geocoding API를 호출하는 방식이었다.
사용자가 정확한 주소를 알아야 하고, Geocoding 결과가 의도와 다를 수 있는 문제가 있었다.

### 변경 내용

#### 9-1. useAddressSearch 커스텀 훅

`src/hooks/useAddressSearch.js` — Google Places Autocomplete API를 래핑하는 훅.

**세션 토큰 기반 비용 최적화:**
- `AutocompleteSessionToken`을 검색 시작 시 생성
- 동일 세션 내 모든 Autocomplete 호출에 동일 토큰 사용
- Place Details 호출 시 토큰을 함께 전달 후 폐기, 새 검색 시 재생성
- 이렇게 하면 Autocomplete 요청 N회 + Place Details 1회가 하나의 세션으로 과금되어 비용 절감

**디바운싱:**
- 입력 후 300ms 동안 추가 입력이 없을 때만 API 호출
- `clearTimeout`/`setTimeout`으로 구현

```javascript
// 반환값
{ suggestions, loading, search, selectPlace, clearSuggestions }

// 주요 옵션
componentRestrictions: { country: 'kr' }  // 한국 주소만
language: 'ko'                            // 한국어 결과
```

#### 9-2. AddressSearchInput 컴포넌트

`src/components/AddressSearchInput.jsx` — 자동완성 드롭다운이 포함된 주소 입력 컴포넌트.

**Props:**
| Prop | 타입 | 설명 |
|------|------|------|
| `onSelect` | `({ address, lat, lng }) => void` | 장소 선택 시 콜백 |
| `placeholder` | `string` | 입력 placeholder |
| `initialValue` | `string` | 초기 주소값 (수정 모드용) |

**동작:**
- 2글자 이상 입력 시 드롭다운에 후보 목록 표시
- 후보 클릭 시 Place Details API로 좌표 획득 → `onSelect` 콜백 호출
- 외부 클릭 시 드롭다운 닫힘 (`mousedown` 이벤트 리스너)
- 로딩 중 "검색 중..." 표시, 결과 없으면 "검색 결과 없음" 표시

#### 9-3. StoreFormPage 수정

기존 주소 텍스트 입력 + "좌표변환" 버튼 + 위도/경도 수동 입력 필드를
`AddressSearchInput` 컴포넌트 하나로 교체.

```javascript
// Before — Geocoding API 호출 + 수동 좌표 입력
<input name="address" ... />
<button onClick={handleGeocode}>좌표변환</button>
<input name="latitude" ... />
<input name="longitude" ... />

// After — Autocomplete로 주소 선택 시 lat/lng 자동 세팅
<AddressSearchInput
  onSelect={handleAddressSelect}
  placeholder="주소를 검색하세요"
  initialValue={form.address}
/>
```

- `handleAddressSelect`에서 `address`, `latitude`, `longitude`를 한번에 form state 업데이트
- 지도 미리보기 마커도 자동 이동
- Geocoding API import 및 `handleGeocode` 함수 제거, 위도/경도 입력 필드 제거

### 개선 효과
- 사용자가 주소를 검색하면서 선택할 수 있어 UX 개선
- Geocoding API 대신 Autocomplete + Place Details 세션으로 비용 최적화
- 위도/경도 수동 입력 오류 가능성 제거

---

## 10. 경로 안내 — Google Maps 딥링크 방식으로 전환

### 배경
/find 페이지에서 매장 클릭 시 경로를 표시하기 위해 여러 API를 시도했으나 모두 실패:
1. **Routes API v2 (REST fetch)** — 빈 응답 `{}` 반환. FieldMask 조합, `routingPreference` 추가 등 시도했으나 해결 불가
2. **Maps JS API DirectionsService** — `ZERO_RESULTS` 반환. 서울 시청→강남 같은 정상 경로도 결과 없음

### 원인
2025년 이후 신규 Google Cloud 프로젝트는 Directions API, Routes API 모두
경로 데이터 반환에 추가 계정 인증(결제 프로필 검증 등)이 필요한 정책으로 변경됨.
API 키 활성화만으로는 경로 계산 결과를 받을 수 없는 상태.

### 해결: Google Maps 딥링크 방식

API로 경로를 직접 계산하는 대신, Google Maps 웹/앱으로 사용자를 보내는 딥링크 방식 채택.

#### useRoute.js — 딥링크 URL 생성 훅

```javascript
// Google Maps Directions 딥링크 URL 형식
// origin 생략 → 구글맵이 사용자 현재 위치를 자동 사용
// destination에 주소 문자열 사용 → 구글맵에서 주소로 표시
https://www.google.com/maps/dir/?api=1
  &destination={encodeURIComponent(storeAddress)}
  &travelmode=driving|walking

// 반환값
{ selectedStore, urls, selectStore, clearRoute }
```

- `selectStore(storeName, address)` — URL 생성 + 패널 표시 (구글맵 열지 않음)
- `urls.driving` / `urls.walking` — 각 이동수단 딥링크 URL 캐싱
- 버튼 클릭 시에만 `window.open(urls.driving)` 으로 구글맵 새 탭 오픈
- API 호출 없이 URL만 생성하므로 에러 발생 불가

#### FindStorePage 수정

```javascript
// Before — DirectionsService로 경로 계산 시도 (ZERO_RESULTS)
drawRoute(mapInstance.current, origin, destination, 'DRIVING');

// After — 지도 이동 + 경로 안내 패널만 표시 (구글맵 열지 않음)
mapInstance.current.panTo({ lat: store.latitude, lng: store.longitude });
mapInstance.current.setZoom(15);
selectStore(store.storeName, store.address);
// 버튼 클릭 시에만 window.open()
```

**경로 안내 패널 UI:**
- 출발지: "내 현재 위치"
- 도착지: 매장명 + 주소
- "자동차로 길찾기" 버튼 (파란색) → 클릭 시 Google Maps 새 탭 (travelmode=driving)
- "도보로 길찾기" 버튼 (초록색) → 클릭 시 Google Maps 새 탭 (travelmode=walking)
- "선택 해제" 버튼

### 시도한 방법과 실패 원인

| 시도 | 방식 | 결과 | 실패 원인 |
|------|------|------|----------|
| 1차 | Routes API v2 REST fetch | 빈 객체 `{}` | FieldMask/정책 문제 |
| 2차 | FieldMask `*` + languageCode/units | 빈 객체 `{}` | 동일 |
| 3차 | Maps JS DirectionsService | ZERO_RESULTS | 신규 프로젝트 정책 제한 |
| 4차 | LatLng 객체 명시적 생성 | ZERO_RESULTS | 동일 |
| 최종 | Google Maps 딥링크 | 성공 | API 의존성 제거 |

### 개선 효과
- API 의존성 제거로 안정성 향상 (API 정책 변경에 영향받지 않음)
- API 호출 비용 완전 제거
- 실제 서비스에서도 많이 사용하는 검증된 패턴 (카카오맵, 네이버맵 등도 동일 방식)
- 사용자가 Google Maps 앱의 실시간 내비게이션 기능 직접 활용 가능

---

## 11. Google Maps API 키 — 결제 계정 연결 문제

### 문제
Google Maps JavaScript API 호출 시 API 키가 거부되거나
일반 GCP API(Routes API 등)를 사용할 수 없었다.

### 원인
최초 생성한 `bizmap` GCP 프로젝트가 Google Maps Platform 전용 결제 계정에 연결되어 있었다.
Maps Platform 전용 결제 계정은 Maps/Places/Routes 등 Maps 관련 API만 사용 가능하며,
일반 GCP API나 일부 신규 API 기능에 제한이 있다.

### Before
```
프로젝트: bizmap
결제 계정: Google Maps Platform 전용
→ 일부 API에서 REQUEST_DENIED 또는 기능 제한
```

### After
```
프로젝트: caumap
결제 계정: 일반 GCP 결제 계정
→ Maps JavaScript API, Places API (New) 등 정상 동작
```

### 교훈
- GCP 프로젝트 생성 시 결제 계정 유형 확인 필요
- Google Maps Platform 전용 계정과 일반 GCP 계정은 사용 가능한 API 범위가 다름
- Maps Platform Console(https://console.cloud.google.com/google/maps-apis)에서 생성한 프로젝트는
  전용 결제 계정에 자동 연결될 수 있음

---

## 12. Places AutocompleteService deprecated — 신규 API 마이그레이션

### 문제
매장 등록 시 주소 자동완성에 `google.maps.places.AutocompleteService`를 사용했으나
콘솔에 아래 에러 발생:
```
google.maps.places.AutocompleteService is not available to new customers.
Please use google.maps.places.AutocompleteSuggestion instead.
```

### 원인
2025년 3월부터 Google이 Places API를 개편하면서,
신규 GCP 프로젝트에서는 기존 `AutocompleteService`가 비활성화되었다.
새 API인 `AutocompleteSuggestion`으로 교체해야 한다.

### Before
```javascript
// 기존 AutocompleteService 방식 (콜백 패턴)
const { AutocompleteService, AutocompleteSessionToken } = await importLibrary('places');
const service = new AutocompleteService();
const token = new AutocompleteSessionToken();

service.getPlacePredictions({
  input: keyword,
  sessionToken: token,
  componentRestrictions: { country: 'kr' },
  language: 'ko',
}, (predictions, status) => {
  // 콜백으로 결과 처리
});

// Place Details 조회
const { PlacesService } = await importLibrary('places');
const placesService = new PlacesService(document.createElement('div'));
placesService.getDetails({ placeId, fields: ['formatted_address', 'geometry'] }, callback);
```

### After
```javascript
// 새 AutocompleteSuggestion 방식 (Promise 패턴)
const { AutocompleteSuggestion } = await importLibrary('places');

const { suggestions } = await AutocompleteSuggestion.fetchAutocompleteSuggestions({
  input: keyword,
  language: 'ko',
  region: 'kr',
});

const results = suggestions.map(s => ({
  placeId: s.placePrediction.placeId,
  description: s.placePrediction.text.toString(),
}));

// Place 좌표 조회
const { Place } = await importLibrary('places');
const place = new Place({ id: placeId });
await place.fetchFields({ fields: ['formattedAddress', 'location'] });
const address = place.formattedAddress;
const lat = place.location.lat();
const lng = place.location.lng();
```

### 교훈
- 새 API는 Promise 기반으로 콜백 패턴 불필요
- `componentRestrictions: { country: 'kr' }` → `region: 'kr'`로 변경
- 세션 토큰 관리가 API 내부로 이동하여 명시적 토큰 생성/폐기 불필요
- `PlacesService.getDetails()` → `new Place({ id }).fetchFields()`로 변경
- 필드명 변경: `formatted_address` → `formattedAddress`, `geometry` → `location`

---

## 13. Places API (New) 403 Forbidden — 별도 활성화 필요

### 문제
`AutocompleteSuggestion`으로 마이그레이션 후에도 403 Forbidden 에러 발생.
기존 Places API는 활성화되어 있었다.

### 원인
기존 **Places API**와 **Places API (New)**는 GCP에서 별개의 API로 취급된다.
`AutocompleteSuggestion`, `Place.fetchFields()` 등 새 클래스는
Places API (New)에 속하므로 별도로 활성화해야 한다.

### Before
```
GCP Console > APIs & Services:
✅ Places API (활성화됨)
❌ Places API (New) (비활성화)
→ AutocompleteSuggestion 호출 시 403 Forbidden
```

### After
```
GCP Console > APIs & Services:
✅ Places API (활성화됨)
✅ Places API (New) (활성화)
→ AutocompleteSuggestion 정상 동작
```

### 교훈
- Google이 API를 개편할 때 기존 API와 신규 API를 별개 서비스로 제공하는 경우가 있음
- "Places API"와 "Places API (New)"는 이름은 비슷하지만 GCP에서 독립된 서비스
- 신규 API 클래스를 사용할 때는 GCP Console에서 해당 API가 활성화되어 있는지 반드시 확인

---

## 14. 구글맵 탭 중복 열림 — 이벤트 버블링

### 문제
매장 카드를 클릭하면 구글맵 새 탭이 의도치 않게 열리거나,
"자동차로 길찾기" / "도보로 길찾기" 버튼 클릭 시 탭이 2개 열렸다.

### 원인
1. 초기 구현에서 매장 클릭(`handleStoreClick`) 시 `window.open()`을 즉시 호출했음
2. 경로 안내 패널의 버튼이 매장 카드 내부에 있어 버튼 클릭 이벤트가 부모 카드로 버블링됨

### Before
```javascript
// 매장 클릭 시 바로 구글맵 열림 (의도하지 않은 동작)
const handleStoreClick = (store) => {
    openRoute(destination, store.storeName, store.address); // window.open() 포함
};
```

### After
```javascript
// 매장 클릭 → 패널만 표시 (구글맵 열지 않음)
const handleStoreClick = (store) => {
    mapInstance.current.panTo({ lat: store.latitude, lng: store.longitude });
    mapInstance.current.setZoom(15);
    selectStore(store.storeName, store.address); // URL 생성만, 열지 않음
};

// 버튼 클릭 시에만 구글맵 열기
const handleOpenMap = (travelMode) => {
    if (urls) {
        window.open(travelMode === 'walking' ? urls.walking : urls.driving, '_blank');
    }
};
```

### 교훈
- 외부 서비스로 리다이렉트하는 동작은 명시적인 사용자 액션(버튼 클릭)에만 연결
- 매장 선택(정보 표시)과 길찾기(외부 이동)는 별개 동작으로 분리해야 UX가 자연스러움
- 패널 내부 버튼과 부모 카드의 클릭 이벤트 버블링 주의

---

## 15. 위젯 임베드 시스템 — 외부 도메인 CORS와 정적 리소스 인증 우회

### 배경
외부 사이트에서 `<script>` 태그 한 줄로 BizMap 매장 지도를 임베드할 수 있도록
위젯 시스템을 구현했다. 위젯 JS와 공개 API는 인증 없이 임의 도메인에서 호출되어야 하며,
이 과정에서 SecurityConfig, CORS, 동적 JS 생성 관련 이슈가 발생했다.

### 15-1. /widget/** 경로 인증 우회

#### 문제
JWT 기반 SecurityConfig는 `.anyRequest().authenticated()`로 모든 요청에
인증을 강제한다. 그러나 위젯 JS 파일과 공개 API는 외부 사이트(인증 토큰 없음)에서
호출되므로 401이 반환된다.

#### 해결
SecurityConfig에 `permitAll` 경로를 추가했다.

```java
.requestMatchers("/widget/**").permitAll()
.requestMatchers("/widget-test.html").permitAll()
```

위젯 키(`api_key`)가 사실상 인증 토큰 역할을 하며, Spring Security 대신
Service 레이어에서 `WidgetKeyRepository.findByApiKey()`로 키 유효성을 검증한다.

### 15-2. 외부 도메인 CORS 처리

#### 문제
기존 CORS 설정은 `http://localhost:5173`(프론트엔드 dev 서버)만 허용했다.
위젯은 임의 외부 도메인에서 호출되므로 모든 origin을 허용해야 한다.
그러나 인증 API의 CORS를 와일드카드로 풀면 보안이 약해진다.

#### 해결
`UrlBasedCorsConfigurationSource`에 경로별로 다른 CORS 정책을 등록했다.

```java
CorsConfiguration widgetCors = new CorsConfiguration();
widgetCors.setAllowedOriginPatterns(List.of("*"));
widgetCors.setAllowedMethods(List.of("GET", "OPTIONS"));
widgetCors.setAllowCredentials(false);

UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
source.registerCorsConfiguration("/widget/**", widgetCors);  // 와일드카드
source.registerCorsConfiguration("/**", configuration);      // 기존 origin 제한
```

`allowCredentials=true`와 `allowedOrigins=*`는 동시에 사용할 수 없다는
Spring 제약 때문에, 위젯용 CORS는 credentials를 끄고 origin pattern을 사용한다.

### 15-3. JS 동적 생성 시 위젯 키 인라인 주입

#### 결정
위젯 JS는 정적 파일이 아니라 컨트롤러에서 동적으로 생성한다.
`?key=` 쿼리 파라미터로 받은 위젯 키를 JS 본문에 인라인으로 삽입하기 때문이다.

```java
@GetMapping(value = "/bizmap-widget.js", produces = "application/javascript")
public ResponseEntity<String> getWidgetScript(@RequestParam("key") String key) {
    widgetKeyService.findByApiKey(key);  // 키 유효성 사전 검증
    return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType("application/javascript"))
            .body(buildWidgetJs(key));
}
```

이렇게 하면 임베드하는 쪽은 별도 설정 코드 없이 `<script src="...?key=KEY">` 한 줄만
삽입하면 되고, 위젯 JS는 자기 자신에 박힌 키로 API를 호출한다.

### 15-4. API_BASE 자동 감지

#### 문제
위젯 JS는 BizMap 백엔드(`http://localhost:8080`)에서 서빙되지만,
외부 사이트(`http://example.com`)에 임베드된다. 위젯 JS 내부에서 호출하는
`/widget/api/stores`는 외부 사이트의 도메인이 아니라 BizMap 백엔드 도메인을 가리켜야 한다.

#### 해결
`document.currentScript.src`로 자기 자신이 로드된 URL을 읽어 origin을 추출한다.

```javascript
var API_BASE = (function () {
  try {
    var script = document.currentScript;
    if (script && script.src) {
      return new URL(script.src).origin;
    }
  } catch (e) {}
  return '';
})();
```

이 방식은 임베드하는 쪽이 백엔드 URL을 알 필요 없이 작동하며,
도메인이 바뀌어도 위젯 JS만 새로 받으면 된다.

### 15-5. Google Maps 미로드 시 폴백 UI

위젯이 임베드되는 외부 사이트에 Google Maps JS가 로드되지 않은 경우,
지도를 그릴 수 없어 위젯이 빈 화면이 된다. 이를 방지하기 위해 폴백 UI를 추가했다.

```javascript
function renderMap(container, stores) {
  if (!window.google || !window.google.maps) {
    renderFallback(container, stores);  // 매장 목록을 텍스트 리스트로 표시
    return;
  }
  // ... 지도 렌더링
}
```

### 교훈
- 외부 임베드용 엔드포인트는 SecurityConfig `permitAll`과 별도 CORS 정책이 필수
- API 키가 사실상 인증 토큰이 되는 경우 Service 레이어에서 키 검증 책임을 가진다
- 위젯 JS는 `document.currentScript`로 자기 origin을 알아내면 임베드 쪽 설정을 줄일 수 있다
- 외부 환경 의존성(Google Maps 등)은 항상 폴백 UI를 준비해야 한다
