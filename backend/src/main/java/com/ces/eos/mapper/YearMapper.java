package com.ces.eos.mapper;

import com.ces.eos.dto.response.YearResponse;
import com.ces.eos.entity.CustomYear;
import com.ces.eos.util.DateUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    imports = {DateUtils.class})
public interface YearMapper {
  @Mapping(target = "isCurrent", expression = "java(DateUtils.isCurrentYear(year))")
  YearResponse toYearResponse(CustomYear year);
}
