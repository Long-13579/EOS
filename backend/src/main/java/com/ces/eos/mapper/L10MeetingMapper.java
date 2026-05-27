package com.ces.eos.mapper;

import com.ces.eos.dto.request.CreateL10MeetingRequest;
import com.ces.eos.dto.response.L10MeetingResponse;
import com.ces.eos.entity.L10Meeting;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {EntityMapper.class, L10MeetingRatingMapper.class})
public interface L10MeetingMapper {
  L10MeetingResponse toL10MeetingResponse(L10Meeting entity);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "team", ignore = true)
  @Mapping(target = "weekStartDate", ignore = true)
  @Mapping(target = "facilitator", ignore = true)
  @Mapping(target = "scribe", ignore = true)
  @Mapping(target = "status", ignore = true)
  @Mapping(target = "concludeKeyDecisions", ignore = true)
  @Mapping(target = "concludeCascadingMessage", ignore = true)
  @Mapping(target = "ratings", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "createdById", ignore = true)
  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "updatedById", ignore = true)
  @Mapping(target = "updatedBy", ignore = true)
  L10Meeting toEntity(CreateL10MeetingRequest dto);
}
