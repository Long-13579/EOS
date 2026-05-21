package com.ces.eos.exception;

import com.ces.eos.enums.ErrorCode;

import java.util.List;
import java.util.Map;

public class ResourceAlreadyExistsException extends BusinessException {

  public ResourceAlreadyExistsException() {
    super(ErrorCode.RESOURCE_ALREADY_EXISTS);
  }

  public ResourceAlreadyExistsException(String message) {
    super(ErrorCode.RESOURCE_ALREADY_EXISTS, message);
  }

  public ResourceAlreadyExistsException(Map<String, List<String>> details) {
    super(ErrorCode.RESOURCE_ALREADY_EXISTS, details);
  }

  public ResourceAlreadyExistsException(String message, Map<String, List<String>> details) {
    super(ErrorCode.RESOURCE_ALREADY_EXISTS, message, details);
  }
}
