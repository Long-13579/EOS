package com.ces.eos.exception;

import com.ces.eos.enums.ErrorCode;

import java.util.List;
import java.util.Map;

public class ConflictException extends BusinessException {

  public ConflictException() {
    super(ErrorCode.CONFLICT);
  }

  public ConflictException(String message) {
    super(ErrorCode.CONFLICT, message);
  }

  public ConflictException(Map<String, List<String>> details) {
    super(ErrorCode.CONFLICT, details);
  }

  public ConflictException(String message, Map<String, List<String>> details) {
    super(ErrorCode.CONFLICT, message, details);
  }
}
