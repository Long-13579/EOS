package com.ces.eos.util;

import com.ces.eos.enums.MetricOperator;
import com.ces.eos.enums.MetricUnit;
import lombok.experimental.UtilityClass;

@UtilityClass
public class MetricUtil {
  public void validateMetricCreation(MetricUnit unit, MetricOperator operator, String goal) {
    if (unit == null) {
      throw new IllegalArgumentException("Metric unit must not be null");
    }
    validateNotBlank(goal, "Metric goal must not be null or blank");
    if (unit.isPredefinedValueType()) {
      validatePredefinedValueUnit(unit, operator, goal);
    } else {
      validateNumericValueUnit(unit, operator, goal);
    }
  }

  public void validateMetricUpdate(MetricUnit unit, String value) {
    if (unit == null) {
      throw new IllegalArgumentException("Metric unit must not be null");
    }
    if (unit.isPredefinedValueType()) {
      validatePredefinedValue(unit, value);
    } else if (!NumberUtil.isFiniteNumeric(value)) {
      throw new IllegalArgumentException(
          "Value must be a valid numeric value for unit '%s'".formatted(unit));
    }
  }

  private void validatePredefinedValueUnit(MetricUnit unit, MetricOperator operator, String goal) {
    if (operator != null) {
      throw new IllegalArgumentException(
          "Operator must not be specified for unit '%s' (predefined-value unit)".formatted(unit));
    }
    validatePredefinedValue(unit, goal);
  }

  private void validateNumericValueUnit(MetricUnit unit, MetricOperator operator, String goal) {
    if (operator == null) {
      throw new IllegalArgumentException(
          "Operator must be specified for unit '%s'".formatted(unit));
    }
    if (!unit.isOperatorAllowed(operator)) {
      throw new IllegalArgumentException(
          "Operator '%s' is not supported for unit '%s'. Supported operators: %s"
              .formatted(operator, unit, unit.getSupportedOperators()));
    }
    validateNumericValue(unit, goal);
  }

  private void validatePredefinedValue(MetricUnit unit, String value) {
    if (!unit.isValueAllowed(value)) {
      throw new IllegalArgumentException(
          "Value '%s' is not valid for unit '%s'. Allowed values: %s"
              .formatted(value, unit, unit.getAllowedValues()));
    }
  }

  private void validateNumericValue(MetricUnit unit, String value) {
    if (!NumberUtil.isFiniteNumeric(value)) {
      throw new IllegalArgumentException(
          "Value must be a valid numeric value for unit '%s'".formatted(unit));
    }
  }

  private void validateNotBlank(String value, String message) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(message);
    }
  }
}
