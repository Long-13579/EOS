package com.ces.eos.mapper;

import com.ces.eos.dto.request.CreateMetricRequest;
import com.ces.eos.dto.response.MetricResponse;
import com.ces.eos.dto.response.TrendDataPointResponse;
import com.ces.eos.dto.response.TrendsTabMetricResponse;
import com.ces.eos.entity.Metric;
import com.ces.eos.entity.MetricValue;
import com.ces.eos.util.MetricValueUtil;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    imports = {MetricValueUtil.class},
    uses = {EntityMapper.class, MetricValueMapper.class})
public interface MetricMapper {
  @Mapping(target = "id", source = "entity.id")
  @Mapping(target = "createdAt", source = "entity.createdAt")
  @Mapping(target = "updatedAt", source = "entity.updatedAt")
  @Mapping(target = "createdBy", source = "entity.createdBy")
  @Mapping(target = "updatedBy", source = "entity.updatedBy")
  @Mapping(target = "lastValue", source = "lastValue.value")
  MetricResponse toMetricResponse(Metric entity, MetricValue currentValue, MetricValue lastValue);

  @Mapping(target = "id", source = "entity.id")
  @Mapping(target = "createdAt", source = "entity.createdAt")
  @Mapping(target = "updatedAt", source = "entity.updatedAt")
  @Mapping(target = "createdBy", source = "entity.createdBy")
  @Mapping(target = "updatedBy", source = "entity.updatedBy")
  @Mapping(target = "values", source = "values")
  TrendsTabMetricResponse toTrendsTabMetricResponse(
      Metric entity, List<TrendDataPointResponse> values);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "createdById", ignore = true)
  @Mapping(target = "updatedById", ignore = true)
  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "updatedBy", ignore = true)
  @Mapping(target = "team", ignore = true)
  @Mapping(target = "owner", ignore = true)
  Metric toEntity(CreateMetricRequest dto);
}
