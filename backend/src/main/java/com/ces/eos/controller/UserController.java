package com.ces.eos.controller;

import com.ces.eos.dto.request.CreateUserRequest;
import com.ces.eos.dto.request.GetEntitiesRequest;
import com.ces.eos.dto.request.UpdateUserRequest;
import com.ces.eos.dto.response.CurrentUserResponse;
import com.ces.eos.dto.response.PagedEntityResponse;
import com.ces.eos.dto.response.TeamResponse;
import com.ces.eos.dto.response.UserResponse;
import com.ces.eos.enums.UserRole;
import com.ces.eos.security.CustomUserDetails;
import com.ces.eos.service.TeamService;
import com.ces.eos.service.UserService;
import com.ces.eos.util.EnumParserUtil;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;
  private final TeamService teamService;

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<UserResponse> addUser(@Valid @RequestBody CreateUserRequest userDto) {
    UserResponse response = userService.addUser(userDto);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<PagedEntityResponse<UserResponse>> getUsers(
      @Valid @ModelAttribute GetEntitiesRequest request) {
    PagedEntityResponse<UserResponse> response = userService.getUsersWithPagination(request);
    return ResponseEntity.ok(response);
  }

  @PutMapping("/{userId}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<UserResponse> updateUser(
      @AuthenticationPrincipal CustomUserDetails currentUser,
      @PathVariable UUID userId,
      @Valid @RequestBody UpdateUserRequest userDto) {
    UserResponse response = userService.updateUser(currentUser.getId(), userId, userDto);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/me/teams")
  public ResponseEntity<List<TeamResponse>> getCurrentUserTeams(
      @AuthenticationPrincipal CustomUserDetails currentUser) {
    UserRole userRole = EnumParserUtil.parseEnum(UserRole.class, currentUser.getRole(), "role");
    List<TeamResponse> response = teamService.getTeamsByUserId(currentUser.getId(), userRole);
    return ResponseEntity.ok(response);
  }

  @PatchMapping("/{userId}/deactivate")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<UserResponse> deactivateUser(
      @AuthenticationPrincipal CustomUserDetails currentUser,
      @PathVariable UUID userId) {
    UserResponse response = userService.deactivateUser(currentUser.getId(), userId);
    return ResponseEntity.ok(response);
  }

  @PatchMapping("/{userId}/activate")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<UserResponse> activateUser(
      @AuthenticationPrincipal CustomUserDetails currentUser,
      @PathVariable UUID userId) {
    UserResponse response = userService.activateUser(currentUser.getId(), userId);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/me")
  public ResponseEntity<CurrentUserResponse> getCurrentUser(
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(
        new CurrentUserResponse(
            userDetails.getId(),
            userDetails.getFirstName(),
            userDetails.getLastName(),
            userDetails.getEmail(),
            userDetails.getRole()));
  }
}
