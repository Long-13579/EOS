package com.ces.eos.util;

import com.ces.eos.entity.Metric;
import com.ces.eos.entity.MetricValue;
import com.ces.eos.enums.MetricOperator;
import com.ces.eos.enums.MetricUnit;
import lombok.experimental.UtilityClass;

@UtilityClass
public class MetricValueUtil {
  public Boolean evaluateMetricValue(MetricValue metricValue) {
    if (metricValue == null || metricValue.getValue() == null) {
      return null;
    }
    Metric metric = metricValue.getMetric();
    if (metric == null) {
      throw new IllegalArgumentException("MetricValue must be associated with a Metric");
    }
    return evaluate(
        metricValue.getValue(), metric.getGoal(), metric.getUnit(), metric.getOperator());
  }

  private boolean evaluate(String actual, String goal, MetricUnit unit, MetricOperator operator) {
    if (actual == null) {
      throw new IllegalArgumentException("Actual value must not be null");
    }
    if (goal == null) {
      throw new IllegalArgumentException("Goal value must not be null");
    }
    if (unit == null) {
      throw new IllegalArgumentException("Metric unit must not be null");
    }

    if (unit.isPredefinedValueType()) {
      return comparePredefinedValues(actual, goal);
    }

    if (operator == null) {
      throw new IllegalArgumentException("Operator must not be null for numeric unit: " + unit);
    }
    return compareNumericValues(actual, goal, operator);
  }

  private boolean compareNumericValues(String actual, String goal, MetricOperator operator) {
    double actualValue;
    double goalValue;
    try {
      actualValue = NumberUtil.parseDouble(actual);
      goalValue = NumberUtil.parseDouble(goal);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException(
          "Failed to parse numeric values: actual='" + actual + "', goal='" + goal + "'", e);
    }
    return switch (operator) {
      case LESS_THAN -> actualValue < goalValue;
      case LESS_THAN_OR_EQUAL_TO -> actualValue <= goalValue;
      case GREATER_THAN -> actualValue > goalValue;
      case GREATER_THAN_OR_EQUAL_TO -> actualValue >= goalValue;
      case EQUAL_TO -> NumberUtil.areNumericEqual(actualValue, goalValue);
    };
  }

  private boolean comparePredefinedValues(String actual, String goal) {
    if (actual == null || goal == null) {
      throw new IllegalArgumentException("Predefined values must not be null");
    }
    return actual.trim().equalsIgnoreCase(goal.trim());
  }
}
