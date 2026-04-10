# Skill: create-api

## 용도
Controller + Service + Dto 세트를 한번에 생성할 때 사용.

## 사용법
"create-api 스킬을 사용해서 {도메인명} API 만들어줘"

## 생성 규칙

### Controller 규칙
- @RestController, @RequestMapping("/api/{도메인}")
- @RequiredArgsConstructor
- 메서드당 하나의 책임만
- 반환 타입: ResponseEntity<ApiResponse<T>> 항상
- 비즈니스 로직 금지, Service 호출만

### Service 규칙
- @Service, @RequiredArgsConstructor, @Transactional
- 첫 줄: Long companyId = SecurityUtils.getCurrentCompanyId();
- 타사 데이터 접근 감지 시: throw new BizMapException(ErrorCode.FORBIDDEN)
- 조회 메서드: @Transactional(readOnly = true)

### Dto 규칙
- Request: @Valid 어노테이션, @NotBlank/@NotNull 등 검증
- Response: 불변 객체, @Builder 사용
- Entity → Response 변환은 Response 클래스의 static from() 메서드

## 출력 예시
요청: "create-api 스킬로 Store API 만들어줘"
생성: StoreController.java + StoreService.java + StoreDto.java
