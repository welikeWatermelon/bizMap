package com.bizmap.common.exception;

import lombok.Getter;

@Getter
public class BizMapException extends RuntimeException {

    private final ErrorCode errorCode;

    public BizMapException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public BizMapException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getCode() {
        return errorCode.getCode();
    }

    public int getStatus() {
        return errorCode.getStatus();
    }
}
