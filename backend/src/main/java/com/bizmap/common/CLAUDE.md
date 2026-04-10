# Common Agent

## 담당 범위
예외처리, 공통 응답 래퍼, 유틸 클래스. 가장 먼저 구현한다.

## 생성할 파일 및 역할

### common/response/ApiResponse.java
```java
// 모든 응답에 사용하는 제네릭 래퍼
{ "data": T, "message": String, "code": String }
success(T data) → 200
error(String code, String message) → 에러용
```

### common/exception/BizMapException.java
RuntimeException 상속. code(String), message 필드 보유.

### common/exception/ErrorCode.java
에러 코드 enum:
COMPANY_NOT_FOUND(404), COMPANY_ALREADY_EXISTS(409),
STORE_NOT_FOUND(404), FORBIDDEN(403),
INVALID_TOKEN(401), INVALID_INPUT(400)

### common/exception/GlobalExceptionHandler.java
@RestControllerAdvice
| 잡는 예외                          | HTTP | 반환 code            |
|----------------------------------|------|---------------------|
| BizMapException                  | 각 코드 | exception.getCode() |
| MethodArgumentNotValidException  | 400  | INVALID_INPUT       |
| AccessDeniedException            | 403  | FORBIDDEN           |
| Exception                        | 500  | INTERNAL_ERROR      |

### common/util/SecurityUtils.java
SecurityContextHolder에서 현재 로그인한 companyId(Long) 추출.
인증 없을 시 BizMapException(INVALID_TOKEN) throw.

## 구현 순서
1. ErrorCode.java
2. BizMapException.java
3. ApiResponse.java
4. GlobalExceptionHandler.java
5. SecurityUtils.java
