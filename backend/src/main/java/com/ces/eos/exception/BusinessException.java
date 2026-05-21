package com.ces.eos.exception;

import com.ces.eos.enums.ErrorCode;
import java.util.List;
import java.util.Map;

public abstract class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;
    private final Map<String, List<String>> details;

    protected BusinessException(ErrorCode errorCode) {
        this(errorCode, null, null);
    }

    protected BusinessException(
            ErrorCode errorCode,
            String overrideMessage) {
        this(errorCode, overrideMessage, null);
    }

    protected BusinessException(
            ErrorCode errorCode,
            Map<String, List<String>> details) {
        this(errorCode, null, details);
    }

    protected BusinessException(
            ErrorCode errorCode,
            String overrideMessage,
            Map<String, List<String>> details) {

        super(overrideMessage != null
                ? overrideMessage
                : errorCode.getMessage());

        this.errorCode = errorCode;
        this.details = details;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public Map<String, List<String>> getDetails() {
        return details;
    }
}
