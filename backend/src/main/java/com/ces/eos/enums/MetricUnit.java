package com.ces.eos.enums;

import java.util.Set;

public enum MetricUnit {
  PERCENTAGE(Set.of(MetricOperator.values())),
  NUMBER(Set.of(MetricOperator.values())),
  CURRENCY(Set.of(MetricOperator.values())),
  YES_NO(Set.of(), Set.of("YES", "NO")),
  RYG_STATUS(Set.of(), Set.of("RED", "YELLOW", "GREEN"));

  private final Set<MetricOperator> supportedOperators;
  private final Set<String> allowedValues;

  MetricUnit(Set<MetricOperator> supportedOperators) {
    this(supportedOperators, Set.of());
  }

  MetricUnit(Set<MetricOperator> supportedOperators, Set<String> allowedValues) {
    this.supportedOperators = supportedOperators;
    this.allowedValues = allowedValues;
  }

  public boolean isPredefinedValueType() {
    return !allowedValues.isEmpty();
  }

  public boolean isValueAllowed(String value) {
    if (value == null) {
      return false;
    }
    if (isPredefinedValueType()) {
      return allowedValues.contains(value.toUpperCase());
    }
    return true;
  }

  public Set<MetricOperator> getSupportedOperators() {
    return supportedOperators;
  }

  public Set<String> getAllowedValues() {
    return allowedValues;
  }

  public boolean isOperatorAllowed(MetricOperator operator) {
    return supportedOperators.contains(operator);
  }
}
