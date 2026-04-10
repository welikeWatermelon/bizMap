# Skill: create-entity

## 용도
JPA Entity + Repository 세트를 한번에 생성할 때 사용.

## 사용법
"create-entity 스킬을 사용해서 {EntityName} 만들어줘"

## 생성 규칙

### Entity 규칙
- @Entity, @Table(name = "테이블명_snake_case") 필수
- @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
- @CreationTimestamp, @UpdateTimestamp 자동 시간 필드
- Lombok: @Getter, @NoArgsConstructor(access = PROTECTED), @Builder
- 연관관계 없이 company_id는 Long 타입 FK로만 보유 (객체 참조 금지)

### Repository 규칙
- JpaRepository<Entity, Long> 상속
- 기본 제공 메서드 외 필요한 것만 추가
- 쿼리 복잡할 시 @Query JPQL 사용 (네이티브 쿼리 금지)
- 목록 조회는 반드시 Pageable 파라미터 포함

## 출력 예시
요청: "create-entity 스킬로 Store 만들어줘"
생성: Store.java + StoreRepository.java
