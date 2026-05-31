package com.ces.eos.mapper;

import com.ces.eos.dto.request.CreateTodoRequest;
import com.ces.eos.dto.response.TodoResponse;
import com.ces.eos.entity.Todo;
import com.ces.eos.enums.TodoStatus;
import com.ces.eos.util.EnumParserUtil;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {EntityMapper.class})
public interface TodoMapper {
  @Mapping(target = "issueId", source = "issue.id")
  TodoResponse toTodoResponse(Todo entity);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "updatedBy", ignore = true)
  @Mapping(target = "assignees", ignore = true)
  @Mapping(target = "isArchived", ignore = true)
  @Mapping(target = "team", ignore = true)
  @Mapping(target = "issue", ignore = true)
  Todo toEntity(CreateTodoRequest dto);

  default TodoStatus mapStatus(String status) {
    return EnumParserUtil.parseEnum(TodoStatus.class, status, "status");
  }
}
