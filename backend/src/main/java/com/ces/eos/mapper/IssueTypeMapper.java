package com.ces.eos.mapper;

import com.ces.eos.dto.response.IssueTypeBaseResponse;
import com.ces.eos.entity.IssueType;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {EntityMapper.class})
public interface IssueTypeMapper {
  IssueTypeBaseResponse toIssueTypeBaseResponse(IssueType entity);
}
