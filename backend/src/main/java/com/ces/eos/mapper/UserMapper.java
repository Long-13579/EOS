package com.ces.eos.mapper;

import com.ces.eos.dto.request.CreateUserRequest;
import com.ces.eos.dto.response.UserBaseResponse;
import com.ces.eos.dto.response.UserResponse;
import com.ces.eos.entity.User;
import org.mapstruct.*;

@Mapper(
    componentModel = "spring",
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {EntityMapper.class})
public interface UserMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "updatedBy", ignore = true)
  @Mapping(target = "role", ignore = true)
  @Mapping(target = "isActive", constant = "true")
  @Mapping(target = "teams", ignore = true)
  User toEntity(CreateUserRequest dto);

  @Mapping(target = "role", source = "role.name")
  UserResponse toUserResponse(User entity);

  UserBaseResponse toUserBaseResponse(User entity);
}
