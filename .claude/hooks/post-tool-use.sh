#!/bin/bash
# Post Tool Use Hook - 파일 저장 후 자동 검증

FILE_PATH="$1"

if [[ -z "$FILE_PATH" ]] || [[ ! -f "$FILE_PATH" ]]; then
  exit 0
fi

if [[ "$FILE_PATH" == *.java ]]; then
  if grep -q "class " "$FILE_PATH"; then
    PACKAGE_LINE=$(head -1 "$FILE_PATH")
    if [[ ! "$PACKAGE_LINE" == package* ]]; then
      echo "[HOOK 경고] $FILE_PATH : package 선언이 없습니다."
    fi
  fi
fi

if [[ "$FILE_PATH" == *"Service.java" ]]; then
  if grep -q "company" "$FILE_PATH" || grep -q "Company" "$FILE_PATH"; then
    if ! grep -q "SecurityUtils" "$FILE_PATH"; then
      echo "[HOOK 경고] $FILE_PATH : SecurityUtils.getCurrentCompanyId() 호출이 없습니다."
    fi
  fi
fi

if [[ "$FILE_PATH" == *"Controller.java" ]]; then
  if ! grep -q "ApiResponse" "$FILE_PATH"; then
    echo "[HOOK 경고] $FILE_PATH : ApiResponse 래퍼를 사용하지 않는 메서드가 있을 수 있습니다."
  fi
fi

if [[ "$FILE_PATH" == *"Repository.java" ]]; then
  if grep -q "isActive\|is_active" "$FILE_PATH"; then
    if ! grep -q "isActiveTrue\|is_active = true" "$FILE_PATH"; then
      echo "[HOOK 경고] $FILE_PATH : is_active 필터 조건을 확인하세요."
    fi
  fi
fi

echo "[HOOK 완료] 파일 검증 완료: $FILE_PATH"
