package com.ces.eos.service;

import com.ces.eos.dto.request.CreateTeamRequest;
import com.ces.eos.dto.request.GetTeamsRequest;
import com.ces.eos.dto.request.UpdateTeamRequest;
import com.ces.eos.dto.response.PagedEntityResponse;
import com.ces.eos.dto.response.TeamResponse;
import com.ces.eos.dto.response.UserBaseResponse;
import com.ces.eos.entity.Team;
import com.ces.eos.entity.User;
import com.ces.eos.enums.UserRole;
import java.util.List;
import java.util.UUID;

public interface TeamService {
  PagedEntityResponse<TeamResponse> getTeamsWithPagination(GetTeamsRequest request);

  TeamResponse addTeam(CreateTeamRequest request);

  TeamResponse updateTeam(UUID teamId, UpdateTeamRequest request);

  List<UserBaseResponse> getUsersByTeamId(UUID teamId);

  List<User> getActiveUsersByTeamId(UUID teamId);

  void validateTeamExists(UUID teamId);

  List<TeamResponse> getTeamsByUserId(UUID userId, UserRole userRole);

  Team getTeamById(UUID teamId);

  void deleteTeam(UUID teamId);
}
