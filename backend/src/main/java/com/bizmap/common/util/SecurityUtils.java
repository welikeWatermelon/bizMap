package com.bizmap.common.util;

import com.bizmap.common.exception.BizMapException;
import com.bizmap.common.exception.ErrorCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    private SecurityUtils() {}

    public static Long getCurrentCompanyId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new BizMapException(ErrorCode.INVALID_TOKEN);
        }
        return (Long) authentication.getPrincipal();
    }
}
