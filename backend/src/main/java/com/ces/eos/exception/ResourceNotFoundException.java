package com.ces.eos.exception;

import com.ces.eos.enums.ErrorCode;
import java.util.List;
import java.util.Map;

public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException() {
        super(ErrorCode.RESOURCE_NOT_FOUND);
    }

    public ResourceNotFoundException(String message) {
        super(ErrorCode.RESOURCE_NOT_FOUND, message);
    }

    public ResourceNotFoundException(Map<String, List<String>> details) {
        super(ErrorCode.RESOURCE_NOT_FOUND, details);
    }

    public ResourceNotFoundException(String message, Map<String, List<String>> details) {
        super(ErrorCode.RESOURCE_NOT_FOUND, message, details);
    }
}
