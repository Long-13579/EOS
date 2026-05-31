package com.ces.eos.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ces.eos.dto.request.CreateTodoRequest;
import com.ces.eos.dto.request.PaginationRequest;
import com.ces.eos.dto.request.UpdateTodoRequest;
import com.ces.eos.dto.response.PagedEntityResponse;
import com.ces.eos.dto.response.TodoResponse;
import com.ces.eos.entity.Team;
import com.ces.eos.entity.Todo;
import com.ces.eos.entity.User;
import com.ces.eos.enums.ErrorCode;
import com.ces.eos.enums.TodoStatus;
import com.ces.eos.exception.BadRequestException;
import com.ces.eos.exception.ConflictException;
import com.ces.eos.exception.ResourceNotFoundException;
import com.ces.eos.mapper.TodoMapper;
import com.ces.eos.repository.IssueRepository;
import com.ces.eos.repository.TeamRepository;
import com.ces.eos.repository.TodoRepository;
import com.ces.eos.repository.UserRepository;
import com.ces.eos.service.TeamService;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class TodoServiceImplTest {

  @Mock private TodoRepository todoRepository;
  @Mock private IssueRepository issueRepository;
  @Mock private TeamRepository teamRepository;
  @Mock private UserRepository userRepository;
  @Mock private TodoMapper todoMapper;
  @Mock private TeamService teamService;

  @InjectMocks private TodoServiceImpl todoService;

  @Nested
  class AddTodo {

    @Test
    void addTodo_validRequest_savesAndReturnsResponse() {
      UUID teamId = UUID.randomUUID();
      UUID assigneeId = UUID.randomUUID();
      CreateTodoRequest request =
          new CreateTodoRequest(
              "Task", "desc", "IN_PROGRESS", Instant.now(), teamId, Set.of(assigneeId), null);

      Todo todo = org.mockito.Mockito.mock(Todo.class);
      Team team = Team.builder().id(teamId).build();
      User assignee = org.mockito.Mockito.mock(User.class);
      Todo saved = org.mockito.Mockito.mock(Todo.class);
      TodoResponse response =
          new TodoResponse(
              UUID.randomUUID(),
              "Task",
              "desc",
              TodoStatus.IN_PROGRESS,
              null,
              false,
              null,
              null,
              null,
              null,
              List.of(),
              null,
              null);

      when(todoMapper.toEntity(request)).thenReturn(todo);
      when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
      when(todo.getTeam()).thenReturn(team);
      when(userRepository.findAllByIdInAndTeamIdAndIsActiveTrue(Set.of(assigneeId), teamId))
          .thenReturn(Set.of(assignee));
      when(todoRepository.save(todo)).thenReturn(saved);
      when(todoMapper.toTodoResponse(saved)).thenReturn(response);

      TodoResponse result = todoService.addTodo(request);

      assertThat(result).isEqualTo(response);
      verify(todoRepository).save(todo);
    }

    @Test
    void addTodo_invalidAssignee_throwsBadRequestException() {
      UUID teamId = UUID.randomUUID();
      UUID assigneeId = UUID.randomUUID();
      CreateTodoRequest request =
          new CreateTodoRequest(
              "Task", "desc", "IN_PROGRESS", Instant.now(), teamId, Set.of(assigneeId), null);

      Todo todo = org.mockito.Mockito.mock(Todo.class);
      Team team = Team.builder().id(teamId).build();

      when(todoMapper.toEntity(request)).thenReturn(todo);
      when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
      when(todo.getTeam()).thenReturn(team);
      when(userRepository.findAllByIdInAndTeamIdAndIsActiveTrue(Set.of(assigneeId), teamId))
          .thenReturn(Set.of());

      assertThatThrownBy(() -> todoService.addTodo(request))
          .isInstanceOf(BadRequestException.class)
          .satisfies(
              ex ->
                  assertThat(((BadRequestException) ex).getErrorCode())
                      .isEqualTo(ErrorCode.BAD_REQUEST));

      verify(todoRepository, never()).save(any(Todo.class));
    }

    @Test
    void addTodo_missingTeam_throwsResourceNotFoundException() {
      UUID teamId = UUID.randomUUID();
      CreateTodoRequest request =
          new CreateTodoRequest("Task", "desc", "IN_PROGRESS", Instant.now(), teamId, Set.of(), null);

      Todo todo = org.mockito.Mockito.mock(Todo.class);
      when(todoMapper.toEntity(request)).thenReturn(todo);
      when(teamRepository.findById(teamId)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> todoService.addTodo(request))
          .isInstanceOf(ResourceNotFoundException.class)
          .satisfies(
              ex ->
                  assertThat(((ResourceNotFoundException) ex).getErrorCode())
                      .isEqualTo(ErrorCode.RESOURCE_NOT_FOUND));

      verify(todoRepository, never()).save(any(Todo.class));
    }
  }

  @Nested
  class GetTodos {

    @Test
    void getTodosByFilter_withStatusAndEmptyPage_returnsEmptyResponse() {
      UUID teamId = UUID.randomUUID();
      when(todoRepository.findTodoIdsByTeamIdAndStatusAndArchiveStatus(
              eq(teamId), eq(TodoStatus.IN_PROGRESS), eq(false), any()))
          .thenReturn(Page.empty(PageRequest.of(0, 10)));

      PagedEntityResponse<TodoResponse> result =
          todoService.getTodosByFilter(
              teamId, new PaginationRequest(1, 10), false, TodoStatus.IN_PROGRESS, null);

      assertThat(result.data()).isEmpty();
      verify(teamService).validateTeamExists(teamId);
    }

    @Test
    void getActiveTodosByUserAcrossTeams_idsFound_returnsMappedItems() {
      UUID userId = UUID.randomUUID();
      UUID todoId = UUID.randomUUID();
      Page<UUID> idsPage = new PageImpl<>(List.of(todoId), PageRequest.of(0, 10), 1);
      Todo todo = org.mockito.Mockito.mock(Todo.class);
      TodoResponse response =
          new TodoResponse(
              todoId,
              "Task",
              null,
              TodoStatus.NOT_STARTED,
              null,
              false,
              null,
              null,
              null,
              null,
              List.of(),
              null,
              null);

      when(todoRepository.findActiveTodoIdsByAssigneeId(any(), any())).thenReturn(idsPage);
      when(todoRepository.findAllByIdIn(List.of(todoId))).thenReturn(List.of(todo));
      when(todo.getId()).thenReturn(todoId);
      when(todoMapper.toTodoResponse(todo)).thenReturn(response);

      PagedEntityResponse<TodoResponse> result =
          todoService.getActiveTodosByUserAcrossTeams(userId, new PaginationRequest(1, 10));

      assertThat(result.data()).containsExactly(response);
    }
  }

  @Nested
  class UpdateAndDelete {

    @Test
    void updateTodoById_archivedTodo_throwsConflictException() {
      UUID todoId = UUID.randomUUID();
      Todo todo = org.mockito.Mockito.mock(Todo.class);
      when(todoRepository.findById(todoId)).thenReturn(Optional.of(todo));
      when(todo.getIsArchived()).thenReturn(true);

      assertThatThrownBy(
              () ->
                  todoService.updateTodoById(
                      todoId,
                      new UpdateTodoRequest(
                          "Title", "Desc", "IN_PROGRESS", Instant.now(), Set.of(), null)))
          .isInstanceOf(ConflictException.class)
          .satisfies(
              ex ->
                  assertThat(((ConflictException) ex).getErrorCode())
                      .isEqualTo(ErrorCode.CONFLICT));
    }

    @Test
    void updateTodoById_missingTodo_throwsResourceNotFoundException() {
      UUID todoId = UUID.randomUUID();
      when(todoRepository.findById(todoId)).thenReturn(Optional.empty());

      assertThatThrownBy(
              () ->
                  todoService.updateTodoById(
                      todoId,
                      new UpdateTodoRequest("Title", "Desc", "IN_PROGRESS", Instant.now(), Set.of(), null)))
          .isInstanceOf(ResourceNotFoundException.class)
          .satisfies(
              ex ->
                  assertThat(((ResourceNotFoundException) ex).getErrorCode())
                      .isEqualTo(ErrorCode.RESOURCE_NOT_FOUND));
    }

    @Test
    void updateTodoById_invalidAssigneeSet_throwsBadRequestException() {
      UUID todoId = UUID.randomUUID();
      UUID teamId = UUID.randomUUID();
      UUID validAssigneeId = UUID.randomUUID();
      UUID missingAssigneeId = UUID.randomUUID();

      Todo todo = org.mockito.Mockito.mock(Todo.class);
      Team team = Team.builder().id(teamId).build();
      User validAssignee = org.mockito.Mockito.mock(User.class);

      when(todoRepository.findById(todoId)).thenReturn(Optional.of(todo));
      when(todo.getIsArchived()).thenReturn(false);
      when(todo.getTeam()).thenReturn(team);
      when(userRepository.findAllByIdInAndTeamIdAndIsActiveTrue(
              Set.of(validAssigneeId, missingAssigneeId), teamId))
          .thenReturn(Set.of(validAssignee));
      when(validAssignee.getId()).thenReturn(validAssigneeId);

      assertThatThrownBy(
              () ->
                  todoService.updateTodoById(
                      todoId,
                      new UpdateTodoRequest(
                          "Title",
                          "Desc",
                          "IN_PROGRESS",
                          Instant.now(),
                          Set.of(validAssigneeId, missingAssigneeId),
                          null)))
          .isInstanceOf(BadRequestException.class)
          .satisfies(
              ex -> assertThat(((BadRequestException) ex).getErrorCode()).isEqualTo(ErrorCode.BAD_REQUEST));

      verify(todoRepository, never()).save(any(Todo.class));
    }

    @Test
    void updateTodoArchiveStatus_sameStatus_returnsWithoutSave() {
      UUID todoId = UUID.randomUUID();
      Todo todo = org.mockito.Mockito.mock(Todo.class);
      TodoResponse response =
          new TodoResponse(
              todoId,
              "Task",
              null,
              TodoStatus.NOT_STARTED,
              null,
              false,
              null,
              null,
              null,
              null,
              List.of(),
              null,
              null);

      when(todoRepository.findById(todoId)).thenReturn(Optional.of(todo));
      when(todo.getIsArchived()).thenReturn(false);
      when(todoMapper.toTodoResponse(todo)).thenReturn(response);

      TodoResponse result = todoService.updateTodoArchiveStatus(todoId, false);

      assertThat(result).isEqualTo(response);
      verify(todoRepository, never()).save(any(Todo.class));
    }

    @Test
    void deleteTodoById_missingTodo_throwsResourceNotFoundException() {
      UUID todoId = UUID.randomUUID();
      when(todoRepository.findById(todoId)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> todoService.deleteTodoById(todoId))
          .isInstanceOf(ResourceNotFoundException.class)
          .satisfies(
              ex ->
                  assertThat(((ResourceNotFoundException) ex).getErrorCode())
                      .isEqualTo(ErrorCode.RESOURCE_NOT_FOUND));
    }
  }
}
