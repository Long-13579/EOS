package com.ces.eos.mapper;

import com.ces.eos.dto.response.QuarterBaseResponse;
import com.ces.eos.dto.response.QuarterResponse;
import com.ces.eos.entity.Quarter;
import com.ces.eos.util.DateUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    imports = {DateUtils.class},
    uses = {EntityMapper.class})
public interface QuarterMapper {
  @Mapping(
      target = "startDate",
      expression = "java(DateUtils.fromMonthDayToString(quarter.getStartDate()))")
  @Mapping(
      target = "endDate",
      expression = "java(DateUtils.fromMonthDayToString(quarter.getEndDate()))")
  QuarterBaseResponse toQuarterBaseResponse(Quarter quarter);

  @Mapping(target = "isCurrent", expression = "java(DateUtils.isCurrentQuarter(quarter))")
  @Mapping(
      target = "startDate",
      expression = "java(DateUtils.fromMonthDayToString(quarter.getStartDate()))")
  @Mapping(
      target = "endDate",
      expression = "java(DateUtils.fromMonthDayToString(quarter.getEndDate()))")
  QuarterResponse toQuarterResponse(Quarter quarter);
}
