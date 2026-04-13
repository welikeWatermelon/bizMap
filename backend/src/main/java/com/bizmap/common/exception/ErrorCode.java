package com.bizmap.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    COMPANY_NOT_FOUND(404, "COMPANY_NOT_FOUND", "회사를 찾을 수 없습니다."),
    COMPANY_ALREADY_EXISTS(409, "COMPANY_ALREADY_EXISTS", "이미 존재하는 회사입니다."),
    STORE_NOT_FOUND(404, "STORE_NOT_FOUND", "매장을 찾을 수 없습니다."),
    FORBIDDEN(403, "FORBIDDEN", "접근 권한이 없습니다."),
    INVALID_TOKEN(401, "INVALID_TOKEN", "유효하지 않은 토큰입니다."),
    INVALID_INPUT(400, "INVALID_INPUT", "잘못된 입력입니다."),
    PRODUCT_NOT_FOUND(404, "PRODUCT_NOT_FOUND", "상품을 찾을 수 없습니다."),
    WIDGET_KEY_NOT_FOUND(404, "WIDGET_KEY_NOT_FOUND", "위젯 키를 찾을 수 없습니다."),
    INVALID_ADDRESS(400, "INVALID_ADDRESS", "유효하지 않은 주소입니다.");

    private final int status;
    private final String code;
    private final String message;
}
