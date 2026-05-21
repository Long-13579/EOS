package com.ces.eos.mapper;

import com.ces.eos.dto.request.CreateRockRequest;
import com.ces.eos.dto.request.UpdateRockRequest;
import com.ces.eos.dto.response.RockResponse;
import com.ces.eos.entity.CustomYear;
import com.ces.eos.entity.Quarter;
import com.ces.eos.entity.Rock;
import com.ces.eos.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {QuarterMapper.class, EntityMapper.class})
public interface RockMapper {

  RockResponse toRockResponse(Rock entity);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "team", ignore = true)
  @Mapping(target = "year", ignore = true)
  @Mapping(target = "quarter", ignore = true)
  @Mapping(target = "owner", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "createdById", ignore = true)
  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "updatedById", ignore = true)
  @Mapping(target = "updatedBy", ignore = true)
  @Mapping(target = "isArchived", ignore = true)
  Rock toEntity(CreateRockRequest dto);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "team", ignore = true)
  @Mapping(target = "owner", source = "owner")
  @Mapping(target = "quarter", source = "quarter")
  @Mapping(target = "year", source = "year")
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "createdById", ignore = true)
  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "updatedById", ignore = true)
  @Mapping(target = "updatedBy", source = "updater")
  @Mapping(target = "isArchived", ignore = true)
  void updateRockFromRequest(
      @MappingTarget Rock target,
      UpdateRockRequest request,
      User owner,
      CustomYear year,
      Quarter quarter,
      User updater);
}
