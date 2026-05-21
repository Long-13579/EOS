package com.ces.eos.util;

import com.ces.eos.exception.BadRequestException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class EnumParserUtil {

  private EnumParserUtil() {}

  public static <T extends Enum<T>> T parseEnum(
      Class<T> enumClass, String value, String fieldName) {

    if (value == null || value.isBlank()) {
      throw new BadRequestException(
          Map.of(fieldName, List.of(String.format("%s cannot be blank", fieldName))));
    }

    try {
      return Enum.valueOf(enumClass, value.toUpperCase());
    } catch (IllegalArgumentException ex) {

      String allowedValues =
          Arrays.stream(enumClass.getEnumConstants())
              .map(Enum::name)
              .collect(Collectors.joining(", "));

      throw new BadRequestException(
          Map.of(
              fieldName,
              List.of(
                  String.format("Invalid value '%s'. Allowed values: %s", value, allowedValues))));
    }
  }
}
