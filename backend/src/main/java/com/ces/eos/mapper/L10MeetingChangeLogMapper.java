package com.ces.eos.mapper;

import com.ces.eos.dto.response.L10MeetingChangeLogResponse;
import com.ces.eos.entity.L10MeetingChangeLog;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface L10MeetingChangeLogMapper {
  @Mapping(target = "meetingId", source = "meeting.id")
  L10MeetingChangeLogResponse toL10MeetingChangeLogResponse(L10MeetingChangeLog entity);
}
