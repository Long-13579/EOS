package com.ces.eos.service;

import com.ces.eos.dto.request.CreateTodoRequest;
import com.ces.eos.dto.request.PaginationRequest;
import com.ces.eos.dto.request.UpdateTodoRequest;
import com.ces.eos.dto.response.PagedEntityResponse;
import com.ces.eos.dto.response.TodoResponse;
import com.ces.eos.enums.TodoStatus;
import java.util.UUID;

public interface TodoService {
  TodoResponse addTodo(CreateTodoRequest request);

  PagedEntityResponse<TodoResponse> getTodosByFilter(
      UUID teamId, PaginationRequest request, boolean isArchived, TodoStatus status, UUID issueId);

  PagedEntityResponse<TodoResponse> getActiveTodosByUserAcrossTeams(
      UUID userId, PaginationRequest request);

  TodoResponse updateTodoById(UUID todoId, UpdateTodoRequest request);

  void deleteTodoById(UUID todoId);

  TodoResponse updateTodoArchiveStatus(UUID todoId, boolean isArchived);

  TodoResponse updateTodoStatus(UUID todoId, TodoStatus status);
}
