package com.ces.eos.service;

import com.ces.eos.dto.request.CreateUserRequest;
import com.ces.eos.dto.request.GetEntitiesRequest;
import com.ces.eos.dto.request.UpdateUserRequest;
import com.ces.eos.dto.response.PagedEntityResponse;
import com.ces.eos.dto.response.UserResponse;
import com.ces.eos.entity.User;
import java.util.UUID;

public interface UserService {
  PagedEntityResponse<UserResponse> getUsersWithPagination(GetEntitiesRequest request);

  User getUserById(UUID userId);

  UserResponse addUser(CreateUserRequest user);

  UserResponse updateUser(UUID currentUserId, UUID userId, UpdateUserRequest user);

  User getUserByIdAndTeamId(UUID userId, UUID teamId);
}
