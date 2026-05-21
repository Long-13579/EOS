package com.ces.eos.mapper;

import com.ces.eos.dto.response.TeamBaseResponse;
import com.ces.eos.dto.response.UserBaseResponse;
import com.ces.eos.dto.response.YearBaseResponse;
import com.ces.eos.entity.CustomYear;
import com.ces.eos.entity.Team;
import com.ces.eos.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EntityMapper {
  UserBaseResponse toUserBaseResponse(User entity);

  TeamBaseResponse toTeamBaseResponse(Team entity);

  YearBaseResponse toYearBaseResponse(CustomYear entity);
}
