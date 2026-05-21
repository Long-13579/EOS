package com.ces.eos.exception;

import com.ces.eos.enums.ErrorCode;
import java.util.List;
import java.util.Map;

public class BadRequestException extends BusinessException {

    public BadRequestException() {
        super(ErrorCode.BAD_REQUEST);
    }

    public BadRequestException(String message) {
        super(ErrorCode.BAD_REQUEST, message);
    }

    public BadRequestException(Map<String, List<String>> details) {
        super(ErrorCode.BAD_REQUEST, details);
    }

    public BadRequestException(String message, Map<String, List<String>> details) {
        super(ErrorCode.BAD_REQUEST, message, details);
    }
}
