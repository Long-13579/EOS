package com.ces.eos.service.impl;

import com.ces.eos.constant.SortingConstants;
import com.ces.eos.dto.common.SortField;
import com.ces.eos.dto.request.CreateTodoRequest;
import com.ces.eos.dto.request.PaginationRequest;
import com.ces.eos.dto.request.UpdateTodoRequest;
import com.ces.eos.dto.response.PagedEntityResponse;
import com.ces.eos.dto.response.TodoResponse;
import com.ces.eos.entity.Team;
import com.ces.eos.entity.Todo;
import com.ces.eos.entity.User;
import com.ces.eos.enums.TodoStatus;
import com.ces.eos.exception.BadRequestException;
import com.ces.eos.exception.ConflictException;
import com.ces.eos.exception.ResourceNotFoundException;
import com.ces.eos.mapper.TodoMapper;
import com.ces.eos.repository.TeamRepository;
import com.ces.eos.repository.TodoRepository;
import com.ces.eos.repository.UserRepository;
import com.ces.eos.service.TeamService;
import com.ces.eos.service.TodoService;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TodoServiceImpl implements TodoService {

  private final TodoRepository todoRepository;
  private final TeamRepository teamRepository;
  private final UserRepository userRepository;
  private final TodoMapper todoMapper;
  private final TeamService teamService;

  @Override
  @Transactional
  public TodoResponse addTodo(CreateTodoRequest request) {
    log.info("action=addTodo.start");
    Todo todo = todoMapper.toEntity(request);

    log.debug("action=addTodo.service.getTeamById teamId={}", request.teamId());
    todo.setTeam(getTeamById(request.teamId()));
    assignUsersToTodo(todo, request.assigneeIds());

    log.debug("action=addTodo.repo.save teamId={}", request.teamId());
    Todo savedTodo = todoRepository.save(todo);
    log.info("action=addTodo.success todoId={}", savedTodo.getId());
    return todoMapper.toTodoResponse(savedTodo);
  }

  @Override
  public PagedEntityResponse<TodoResponse> getTodosByFilter(
      UUID teamId, PaginationRequest request, boolean isArchived, TodoStatus status) {
    log.info(
        "action=getTodosByFilter.start teamId={} page={} limit={}",
        teamId,
        request.page(),
        request.limit());
    log.debug("action=getTodosByFilter.service.validateTeamExists teamId={}", teamId);
    teamService.validateTeamExists(teamId);

    Pageable pageable;
    Page<UUID> todoIdsPage;
    if (status != null) {
      log.debug("action=getTodosByFilter.branch.withStatus teamId={} status={}", teamId, status);
      pageable =
          PageRequest.of(
              request.page() - 1,
              request.limit(),
              SortingConstants.customEntitiesSort(SortField.asc("dueDate")));
      log.debug(
          "action=getTodosByFilter.repo.findTodoIdsByTeamIdAndStatusAndArchiveStatus teamId={} status={}",
          teamId,
          status);
      todoIdsPage =
          todoRepository.findTodoIdsByTeamIdAndStatusAndArchiveStatus(
              teamId, status, isArchived, pageable);
    } else {
      log.debug("action=getTodosByFilter.branch.withoutStatus teamId={}", teamId);
      pageable = PageRequest.of(request.page() - 1, request.limit());
      log.debug(
          "action=getTodosByFilter.repo.findTodoIdsByTeamIdAndArchiveStatus teamId={}", teamId);
      todoIdsPage =
          todoRepository.findTodoIdsByTeamIdAndArchiveStatus(teamId, isArchived, pageable);
    }
    PagedEntityResponse<TodoResponse> response = mapToPagedTodoResponse(todoIdsPage, pageable);
    log.info("action=getTodosByFilter.success teamId={} count={}", teamId, response.data().size());
    return response;
  }

  @Override
  public PagedEntityResponse<TodoResponse> getActiveTodosByUserAcrossTeams(
      UUID userId, PaginationRequest request) {
    log.info(
        "action=getActiveTodosByUserAcrossTeams.start userId={} page={} limit={}",
        userId,
        request.page(),
        request.limit());
    Pageable pageable = PageRequest.of(request.page() - 1, request.limit());
    log.debug(
        "action=getActiveTodosByUserAcrossTeams.repo.findActiveTodoIdsByAssigneeId userId={}",
        userId);
    Page<UUID> todoIdsPage = todoRepository.findActiveTodoIdsByAssigneeId(userId, pageable);
    PagedEntityResponse<TodoResponse> response = mapToPagedTodoResponse(todoIdsPage, pageable);
    log.info(
        "action=getActiveTodosByUserAcrossTeams.success userId={} count={}",
        userId,
        response.data().size());
    return response;
  }

  @Override
  @Transactional
  public TodoResponse updateTodoById(UUID todoId, UpdateTodoRequest request) {
    log.info("action=updateTodoById.start todoId={}", todoId);
    Todo todo = getTodoById(todoId);
    if (Boolean.TRUE.equals(todo.getIsArchived())) {
      log.warn("action=updateTodoById.validationFailed reason=archived todoId={}", todoId);
      throw new ConflictException(
          Map.of("todoId", List.of("Cannot update an archived todo. Please unarchive it first.")));
    }
    todo.setTitle(request.title());
    todo.setDescription(request.description());
    todo.setStatus(todoMapper.mapStatus(request.status()));
    todo.setDueDate(request.dueDate());
    assignUsersToTodo(todo, request.assigneeIds());

    log.debug("action=updateTodoById.repo.save todoId={}", todoId);
    Todo updatedTodo = todoRepository.save(todo);
    log.info("action=updateTodoById.success todoId={}", updatedTodo.getId());
    return todoMapper.toTodoResponse(updatedTodo);
  }

  @Override
  @Transactional
  public void deleteTodoById(UUID todoId) {
    log.info("action=deleteTodoById.start todoId={}", todoId);
    Todo todo = getTodoById(todoId);
    log.debug("action=deleteTodoById.repo.delete todoId={}", todoId);
    todoRepository.delete(todo);
    log.info("action=deleteTodoById.success todoId={}", todoId);
  }

  @Override
  @Transactional
  public TodoResponse updateTodoArchiveStatus(UUID todoId, boolean isArchived) {
    log.info("action=updateTodoArchiveStatus.start todoId={} isArchived={}", todoId, isArchived);
    Todo todo = getTodoById(todoId);
    if (todo.getIsArchived() == isArchived) {
      log.debug("action=updateTodoArchiveStatus.branch.noChange todoId={}", todoId);
      log.info("action=updateTodoArchiveStatus.success todoId={}", todoId);
      return todoMapper.toTodoResponse(todo);
    }
    todo.setIsArchived(isArchived);
    log.debug("action=updateTodoArchiveStatus.repo.save todoId={}", todoId);
    Todo updatedTodo = todoRepository.save(todo);
    log.info("action=updateTodoArchiveStatus.success todoId={}", updatedTodo.getId());
    return todoMapper.toTodoResponse(updatedTodo);
  }

  private Team getTeamById(UUID teamId) {
    log.debug("action=getTeamById.repo.findById teamId={}", teamId);
    return teamRepository
        .findById(teamId)
        .orElseThrow(
            () -> {
              log.warn("action=getTeamById.validationFailed teamId={}", teamId);
              return new ResourceNotFoundException(
                  Map.of("teamId", List.of(String.format("Team not found with id: %s", teamId))));
            });
  }

  private void assignUsersToTodo(Todo todo, Set<UUID> assigneeIds) {
    log.debug("action=assignUsersToTodo.start todoId={}", todo.getId());

    Set<UUID> validIds =
        (assigneeIds == null)
            ? Collections.emptySet()
            : assigneeIds.stream().filter(Objects::nonNull).collect(Collectors.toSet());

    if (validIds.isEmpty()) {
      log.debug("action=assignUsersToTodo.branch.emptyAssignees todoId={}", todo.getId());
      todo.setAssignees(new HashSet<>());
      return;
    }

    log.debug(
        "action=assignUsersToTodo.repo.findAllByIdInAndTeamIdAndIsActiveTrue teamId={}",
        todo.getTeam().getId());
    Set<User> assignees =
        userRepository.findAllByIdInAndTeamIdAndIsActiveTrue(validIds, todo.getTeam().getId());
    log.debug(
        "action=assignUsersToTodo.branch.foundCount found={} requested={}",
        assignees.size(),
        validIds.size());

    if (assignees.size() != validIds.size()) {
      Set<UUID> foundIds = assignees.stream().map(User::getId).collect(Collectors.toSet());
      List<String> invalidMessages =
          validIds.stream()
              .filter(id -> !foundIds.contains(id))
              .map(id -> "Invalid assignee id: " + id)
              .toList();
      log.warn("action=assignUsersToTodo.validationFailed invalidCount={}", invalidMessages.size());
      throw new BadRequestException(Map.of("assigneeIds", invalidMessages));
    }

    todo.setAssignees(assignees);
  }

  private Todo getTodoById(UUID todoId) {
    log.debug("action=getTodoById.repo.findById todoId={}", todoId);
    return todoRepository
        .findById(todoId)
        .orElseThrow(
            () -> {
              log.warn("action=getTodoById.validationFailed todoId={}", todoId);
              return new ResourceNotFoundException(
                  Map.of("todoId", List.of(String.format("Todo not found with id: %s", todoId))));
            });
  }

  private PagedEntityResponse<TodoResponse> mapToPagedTodoResponse(
      Page<UUID> todoIdsPage, Pageable pageable) {
    if (todoIdsPage.isEmpty()) {
      log.debug("action=mapToPagedTodoResponse.branch.emptyPage");
      return PagedEntityResponse.from(Page.empty(pageable));
    }

    log.debug(
        "action=mapToPagedTodoResponse.repo.findAllByIdIn count={}",
        todoIdsPage.getContent().size());
    Map<UUID, Todo> todoMap =
        todoRepository.findAllByIdIn(todoIdsPage.getContent()).stream()
            .collect(Collectors.toMap(Todo::getId, Function.identity()));

    List<TodoResponse> todoResponses =
        todoIdsPage.getContent().stream()
            .map(todoMap::get)
            .filter(Objects::nonNull)
            .map(todoMapper::toTodoResponse)
            .toList();

    log.debug("action=mapToPagedTodoResponse.success count={}", todoResponses.size());
    return PagedEntityResponse.from(
        new PageImpl<>(todoResponses, pageable, todoIdsPage.getTotalElements()));
  }
}
