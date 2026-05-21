package com.ces.eos.mapper;

import com.ces.eos.dto.response.WeekResponse;
import com.ces.eos.entity.Week;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface WeekMapper {
  WeekResponse toWeekResponse(Week week);
}
