package com.ces.eos.mapper;

import com.ces.eos.dto.request.CreateTeamRequest;
import com.ces.eos.dto.response.TeamBaseResponse;
import com.ces.eos.dto.response.TeamResponse;
import com.ces.eos.entity.Team;
import org.mapstruct.*;

@Mapper(
    componentModel = "spring",
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {EntityMapper.class})
public interface TeamMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "updatedBy", ignore = true)
  @Mapping(target = "users", ignore = true)
  Team toEntity(CreateTeamRequest dto);

  TeamResponse toTeamResponse(Team entity);

  @Mapping(target = "users", ignore = true)
  TeamResponse toTeamResponseWithoutUsers(Team entity);

  TeamBaseResponse toTeamBaseResponse(Team entity);
}
