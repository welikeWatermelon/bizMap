# Skill: create-hook

## 용도
React 커스텀 훅을 생성할 때 사용.

## 사용법
"create-hook 스킬로 {훅 이름} 만들어줘"

## 생성 규칙

### 훅 구조
- 파일명: use{Name}.js
- 위치: src/hooks/
- 반환: { data, loading, error, 액션함수들 }

### 상태 관리
- useState로 data, loading, error 관리
- loading: API 호출 시작 시 true, 완료/실패 시 false
- error: null 또는 에러 메시지 문자열

### API 호출 규칙
- api/ 폴더의 함수 사용 (직접 axios 호출 금지)
- try/catch 필수
- finally에서 loading = false

### 의존성
- useEffect 의존성 배열 명시 필수
- 불필요한 리렌더링 방지

## 출력 예시
요청: "create-hook 스킬로 useStores 만들어줘"
생성: src/hooks/useStores.js
반환값: { stores, loading, error, fetchStores, totalPages }
