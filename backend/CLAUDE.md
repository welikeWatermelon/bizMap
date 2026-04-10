# Backend Lead Agent

## 역할
백엔드 전체 구조 관리 및 도메인 Agent 작업 위임.

## 레이어 규칙
Controller → Service → Repository 순서 엄수
- Controller: 요청/응답 처리, 인증 확인, ApiResponse 래핑만
- Service: 비즈니스 로직, company_id 검증, 예외 throw
- Repository: 쿼리만, 로직 금지

## 공통 응답 형식
성공: ApiResponse.success(data)
실패: ApiResponse.error(code, message)
위치: common/response/ApiResponse.java

## 보안 규칙
- SecurityContextHolder → SecurityUtils.getCurrentCompanyId()로 company 추출
- 모든 Store 관련 Service에서 company_id 일치 검증 필수
- permitAll 경로: /api/auth/**, /actuator/health

## 패키지 구조
com.bizmap/
├── auth/        → auth/CLAUDE.md 참고
├── store/       → store/CLAUDE.md 참고
├── dashboard/   → dashboard/CLAUDE.md 참고
└── common/      → common/CLAUDE.md 참고

## 도메인별 작업 위임 기준
| 작업 내용                  | 담당 CLAUDE.md       |
|--------------------------|---------------------|
| 회원가입/로그인/JWT/Security | auth/CLAUDE.md      |
| 매장 CRUD/검색/반경검색      | store/CLAUDE.md     |
| 집계/차트 데이터             | dashboard/CLAUDE.md |
| 예외처리/응답래퍼/유틸        | common/CLAUDE.md    |

## 개발 순서
1. common (예외처리, 응답 래퍼)
2. auth (JWT, Security)
3. store (CRUD, 반경검색)
4. dashboard (집계)
