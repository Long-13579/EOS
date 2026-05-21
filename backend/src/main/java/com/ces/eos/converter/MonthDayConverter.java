package com.ces.eos.converter;

import com.ces.eos.util.DateUtils;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.time.MonthDay;

@Converter(autoApply = true)
public class MonthDayConverter implements AttributeConverter<MonthDay, String> {
  @Override
  public String convertToDatabaseColumn(MonthDay attribute) {
    return DateUtils.fromMonthDayToString(attribute);
  }

  @Override
  public MonthDay convertToEntityAttribute(String dbData) {
    return DateUtils.fromStringToMonthDay(dbData);
  }
}
