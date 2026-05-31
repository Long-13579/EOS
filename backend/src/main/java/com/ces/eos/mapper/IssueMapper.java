package com.ces.eos.mapper;

import com.ces.eos.dto.request.CreateIssueRequest;
import com.ces.eos.dto.response.IssueResponse;
import com.ces.eos.entity.Issue;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {EntityMapper.class})
public interface IssueMapper {
  @Mapping(target = "totalTodosCount", ignore = true)
  IssueResponse toIssueResponse(Issue entity);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "updatedBy", ignore = true)
  @Mapping(target = "issueType", ignore = true)
  @Mapping(target = "creator", ignore = true)
  @Mapping(target = "team", ignore = true)
  @Mapping(target = "isArchived", ignore = true)
  Issue toEntity(CreateIssueRequest dto);
}
