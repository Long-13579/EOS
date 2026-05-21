package com.ces.eos.mapper;

import com.ces.eos.dto.response.MetricValueResponse;
import com.ces.eos.dto.response.MetricValueBaseResponse;
import com.ces.eos.entity.MetricValue;
import com.ces.eos.util.MetricValueUtil;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    imports = {MetricValueUtil.class},
    uses = {EntityMapper.class})
public interface MetricValueMapper {
  @Mapping(target = "isGoalMet", expression = "java(MetricValueUtil.evaluateMetricValue(entity))")
  MetricValueResponse toMetricValueResponse(MetricValue entity);

  @Mapping(target = "isGoalMet", expression = "java(MetricValueUtil.evaluateMetricValue(entity))")
  MetricValueBaseResponse toMetricValueBaseResponse(MetricValue entity);
}
