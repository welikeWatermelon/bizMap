# Store Agent

## 담당 범위
매장 CRUD, 목록 검색/필터/페이징, 지도 핀 데이터, 반경 검색.
Auth Agent 구현 완료 후 작업 시작.

## 엔드포인트
GET    /api/stores           목록 조회 (keyword, category, page, size)
POST   /api/stores           매장 등록
GET    /api/stores/{id}      단건 조회
PUT    /api/stores/{id}      수정
DELETE /api/stores/{id}      soft delete
GET    /api/stores/map       지도용 핀 목록 [{ id, name, lat, lng }]
GET    /api/stores/nearby    반경검색 ?lat=&lng=&radius=(km)

## 핵심 규칙
1. 모든 쿼리: WHERE company_id = :companyId AND is_active = true
2. 단건 조회/수정/삭제: company_id 불일치 시 FORBIDDEN throw
3. 삭제: is_active = false (DB에서 지우지 않음)

## 생성할 파일 목록
### domain/
- Store.java           Entity, @Table name=stores
- StoreCategory.java   ENUM { RETAIL, FOOD, SERVICE, OTHER }

### repository/
- StoreRepository.java
  - findAllByCompanyIdAndIsActiveTrue(Long companyId, Pageable pageable)
  - findByIdAndIsActiveTrue(Long id)
  - Haversine 반경검색 @Query (JPQL)

### dto/
- CreateStoreRequest.java  { name, category, address, lat, lng, phone, openTime, closeTime } @Valid
- UpdateStoreRequest.java  (같은 필드, 전부 optional)
- StoreResponse.java       전체 필드 응답
- MapPinResponse.java      { id, name, latitude, longitude }
- NearbyStoreResponse.java { id, name, address, distance(km) }

### service/
- StoreService.java
  - 모든 메서드 첫 줄: SecurityUtils.getCurrentCompanyId()
  - 단건 조회/수정/삭제: store.getCompanyId() != companyId → FORBIDDEN

### controller/
- StoreController.java

## Haversine JPQL
SELECT s FROM Store s
WHERE s.companyId = :companyId
AND s.isActive = true
AND (6371 * acos(
  cos(radians(:lat)) * cos(radians(s.latitude))
  * cos(radians(s.longitude) - radians(:lng))
  + sin(radians(:lat)) * sin(radians(s.latitude))
)) <= :radius

## 구현 순서
1. StoreCategory.java
2. Store.java
3. StoreRepository.java
4. Dto 파일 전체
5. StoreService.java
6. StoreController.java
