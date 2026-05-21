package com.ces.eos.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class NumberUtil {
  private static final double DOUBLE_EQUALITY_THRESHOLD = 1e-9;

  public double parseDouble(String value) {
    if (value == null) {
      throw new IllegalArgumentException("Value must not be null");
    }
    try {
      return Double.parseDouble(value.trim());
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Invalid numeric value: '%s'".formatted(value), e);
    }
  }

  public boolean isFiniteNumeric(String value) {
    if (value == null) {
      return false;
    }
    try {
      double parsed = Double.parseDouble(value.trim());
      return !Double.isInfinite(parsed) && !Double.isNaN(parsed);
    } catch (NumberFormatException e) {
      return false;
    }
  }

  public boolean areNumericEqual(double a, double b) {
    return Math.abs(a - b) < DOUBLE_EQUALITY_THRESHOLD;
  }
}
