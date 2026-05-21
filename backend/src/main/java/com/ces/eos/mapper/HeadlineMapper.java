package com.ces.eos.mapper;

import com.ces.eos.dto.request.CreateHeadlineRequest;
import com.ces.eos.dto.response.HeadlineResponse;
import com.ces.eos.entity.Headline;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {EntityMapper.class})
public interface HeadlineMapper {
  HeadlineResponse toHeadlineResponse(Headline entity);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "createdById", ignore = true)
  @Mapping(target = "updatedById", ignore = true)
  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "updatedBy", ignore = true)
  @Mapping(target = "team", ignore = true)
  @Mapping(target = "isArchived", ignore = true)
  Headline toEntity(CreateHeadlineRequest dto);
}
