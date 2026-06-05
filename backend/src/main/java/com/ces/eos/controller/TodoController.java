package com.ces.eos.controller;

import com.ces.eos.dto.request.CreateTodoRequest;
import com.ces.eos.dto.request.PaginationRequest;
import com.ces.eos.dto.request.UpdateTodoArchiveRequest;
import com.ces.eos.dto.request.UpdateTodoRequest;
import com.ces.eos.dto.request.UpdateTodoStatusRequest;
import com.ces.eos.dto.response.PagedEntityResponse;
import com.ces.eos.dto.response.TodoResponse;
import com.ces.eos.enums.TodoStatus;
import com.ces.eos.security.CustomUserDetails;
import com.ces.eos.service.TodoService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/todos")
@RequiredArgsConstructor
public class TodoController {

  private final TodoService todoService;

  @PostMapping
  @PreAuthorize(
      "hasRole('ADMIN') or @teamSecurityService.isCurrentUserMemberOfTeam(#request.teamId)")
  public ResponseEntity<TodoResponse> addTodo(@Valid @RequestBody CreateTodoRequest request) {
    TodoResponse response = todoService.addTodo(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping
  @PreAuthorize("hasRole('ADMIN') or @teamSecurityService.isCurrentUserMemberOfTeam(#teamId)")
  public ResponseEntity<PagedEntityResponse<TodoResponse>> getTodosByTeam(
      @Valid @ModelAttribute PaginationRequest request,
      @RequestParam UUID teamId,
      @RequestParam(required = false) TodoStatus status,
      @RequestParam(defaultValue = "false") Boolean isArchived,
      @RequestParam(required = false) UUID issueId) {
    PagedEntityResponse<TodoResponse> response =
        todoService.getTodosByFilter(teamId, request, isArchived, status, issueId);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/me")
  public ResponseEntity<PagedEntityResponse<TodoResponse>> getTodosAssignedToCurrentUser(
      @AuthenticationPrincipal CustomUserDetails currentUser,
      @Valid @ModelAttribute PaginationRequest request) {
    PagedEntityResponse<TodoResponse> response =
        todoService.getActiveTodosByUserAcrossTeams(currentUser.getId(), request);
    return ResponseEntity.ok(response);
  }

  @PutMapping("/{todoId}")
  @PreAuthorize(
      "hasRole('ADMIN') or @teamSecurityService.isCurrentUserMemberOfTeamByTodoId(#todoId)")
  public ResponseEntity<TodoResponse> updateTodoById(
      @PathVariable UUID todoId, @Valid @RequestBody UpdateTodoRequest request) {
    TodoResponse response = todoService.updateTodoById(todoId, request);
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/{todoId}")
  @PreAuthorize(
      "hasRole('ADMIN') or @teamSecurityService.isCurrentUserMemberOfTeamByTodoId(#todoId)")
  public ResponseEntity<Void> deleteTodoById(@PathVariable UUID todoId) {
    todoService.deleteTodoById(todoId);
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/{todoId}")
  @PreAuthorize(
      "hasRole('ADMIN') or @teamSecurityService.isCurrentUserMemberOfTeamByTodoId(#todoId)")
  public ResponseEntity<TodoResponse> updateTodoArchiveStatus(
      @PathVariable UUID todoId, @Valid @RequestBody UpdateTodoArchiveRequest request) {
    TodoResponse response = todoService.updateTodoArchiveStatus(todoId, request.isArchived());
    return ResponseEntity.ok(response);
  }

  @PatchMapping("/{todoId}/status")
  @PreAuthorize(
      "hasRole('ADMIN') or @teamSecurityService.isCurrentUserMemberOfTeamByTodoId(#todoId)")
  public ResponseEntity<TodoResponse> updateTodoStatus(
      @PathVariable UUID todoId, @Valid @RequestBody UpdateTodoStatusRequest request) {
    TodoResponse response =
        todoService.updateTodoStatus(todoId, TodoStatus.valueOf(request.status()));
    return ResponseEntity.ok(response);
  }
}
