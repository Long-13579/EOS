package com.ces.eos.mapper;

import com.ces.eos.dto.response.L10MeetingRatingResponse;
import com.ces.eos.entity.L10MeetingRating;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {EntityMapper.class})
public interface L10MeetingRatingMapper {
  @Mapping(target = "meetingId", source = "meeting.id")
  @Mapping(target = "rating", source = "rating", qualifiedByName = "ratingToString")
  L10MeetingRatingResponse toL10MeetingRatingResponse(L10MeetingRating entity);

  @org.mapstruct.Named("ratingToString")
  default String ratingToString(com.ces.eos.enums.L10MeetingRatingValue rating) {
    return rating != null ? rating.name() : null;
  }
}
