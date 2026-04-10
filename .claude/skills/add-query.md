# Skill: add-query

## 용도
Repository에 JPQL 쿼리 메서드를 추가할 때 사용.

## 사용법
"add-query 스킬로 {Repository명}에 {기능} 쿼리 추가해줘"

## 생성 규칙

### 기본 원칙
- 네이티브 쿼리(@Query nativeQuery=true) 금지, JPQL만 사용
- 모든 쿼리에 company_id 조건 포함 필수
- 목록 조회: Pageable 파라미터 추가
- is_active = true 조건 항상 포함

### 네이밍 규칙
- 단건: findBy{조건}
- 목록: findAllBy{조건}
- 존재확인: existsBy{조건}
- 삭제(soft): @Modifying + @Query로 is_active = false

### 성능 규칙
- 페이징 쿼리는 countQuery 분리
- FETCH JOIN 남용 금지

## 출력 예시
요청: "add-query 스킬로 StoreRepository에 카테고리 필터 쿼리 추가해줘"
생성: StoreRepository.java에 findAllByCompanyIdAndCategoryAndIsActiveTrue() 추가
