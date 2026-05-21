package com.ces.eos.exception;

import com.ces.eos.enums.ErrorCode;
import java.util.List;
import java.util.Map;

public class AuthException extends BusinessException {

    public AuthException(ErrorCode errorCode) {
        super(errorCode);
    }

    public AuthException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public AuthException(ErrorCode errorCode, Map<String, List<String>> details) {
        super(errorCode, details);
    }

    public AuthException(ErrorCode errorCode, String message, Map<String, List<String>> details) {
        super(errorCode, message, details);
    }

    public static AuthException forbidden() {
        return new AuthException(ErrorCode.FORBIDDEN);
    }

    public static AuthException forbidden(String message) {
        return new AuthException(ErrorCode.FORBIDDEN, message);
    }

    public static AuthException oauthError() {
        return new AuthException(ErrorCode.OAUTH_ERROR);
    }

    public static AuthException oauthError(String message) {
        return new AuthException(ErrorCode.OAUTH_ERROR, message);
    }

    public static AuthException unauthorized() {
        return new AuthException(ErrorCode.UNAUTHORIZED);
    }

    public static AuthException unauthorized(String message) {
        return new AuthException(ErrorCode.UNAUTHORIZED, message);
    }

    public static AuthException invalidToken() {
        return new AuthException(ErrorCode.INVALID_TOKEN);
    }

    public static AuthException invalidToken(String message) {
        return new AuthException(ErrorCode.INVALID_TOKEN, message);
    }

    public static AuthException tokenExpired() {
        return new AuthException(ErrorCode.TOKEN_EXPIRED);
    }

    public static AuthException tokenExpired(String message) {
        return new AuthException(ErrorCode.TOKEN_EXPIRED, message);
    }

    public static AuthException userInactive() {
        return new AuthException(ErrorCode.USER_INACTIVE);
    }
}
