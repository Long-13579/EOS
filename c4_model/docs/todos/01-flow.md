# Todos (Task Management) Flow

## Sequence Diagram

```mermaid
sequenceDiagram
    participant U as User
    participant W as Web Application
    participant B as Backend API
    participant DB as PostgreSQL

    Note over U,DB: Page Load - Fetch Todos

    U->>W: Navigate to /todos
    W->>W: Read active team from Zustand
    W->>B: GET /api/v1/teams/{teamId}/users
    B->>DB: Query team members
    DB->>B: User list
    B->>W: UserBaseResponse[] (for assignee filter/selector)

    W->>B: GET /api/v1/todos?teamId=X&isArchived=false&page=1&pageSize=10
    B->>B: Validate team membership (@PreAuthorize)
    B->>DB: Query todo IDs by team, archive status, and optional status filter
    DB->>B: Matching todo IDs (paginated)
    B->>DB: Fetch full todos by IDs (with joins: assignees, team)
    DB->>B: Todo entities with assignees
    B->>W: PagedEntityResponse<TodoResponse>
    W->>U: Display todos table with status badges

    Note over U,DB: Filter by Status

    U->>W: Select "In Progress" status filter
    W->>B: GET /api/v1/todos?teamId=X&status=IN_PROGRESS&isArchived=false&page=1
    B->>DB: Query todos filtered by status
    DB->>B: Filtered todos
    B->>W: Paginated todo list
    W->>U: Display filtered todos

    Note over U,DB: Create Todo

    U->>W: Click "Add Todo", fill dialog form
    W->>B: POST /api/v1/todos {teamId, title, description, status, dueDate, assigneeIds}
    B->>B: Validate team membership
    B->>B: Validate all assignees belong to team
    B->>DB: Insert todo entity
    B->>DB: Insert todo_assignees join records
    DB->>B: Saved todo with assignees
    B->>W: TodoResponse
    W->>W: Invalidate todos query cache
    W->>U: Show updated todos list

    Note over U,DB: Update Todo (Status Change)

    U->>W: Change todo status checkbox/dropdown
    W->>B: PUT /api/v1/todos/{todoId} {title, description, status, dueDate, assigneeIds}
    B->>B: Validate team membership via todo
    B->>B: Validate assignees belong to team
    B->>DB: Update todo fields
    B->>DB: Sync todo_assignees (delete old, insert new)
    DB->>B: Updated todo
    B->>W: TodoResponse
    W->>W: Invalidate todos query cache
    W->>U: Show updated todo status

    Note over U,DB: Archive Todo

    U->>W: Click archive action
    W->>B: PATCH /api/v1/todos/{todoId} {isArchived: true}
    B->>B: Validate team membership
    B->>DB: Update isArchived flag
    DB->>B: Updated todo
    B->>W: TodoResponse
    W->>U: Todo moved to archived view

    Note over U,DB: Delete Todo

    U->>W: Click delete, confirm
    W->>B: DELETE /api/v1/todos/{todoId}
    B->>B: Validate team membership
    B->>DB: Delete todo and assignee records (cascade)
    DB->>B: Confirmation
    B->>W: 204 No Content
    W->>W: Invalidate query cache
    W->>U: Todo removed from list

    Note over U,DB: My Todos (Cross-Team Dashboard Widget)

    U->>W: View dashboard
    W->>B: GET /api/v1/todos/me
    B->>B: Extract userId from JWT
    B->>DB: Query active todos where user is assignee (all teams)
    DB->>B: User's todos across teams
    B->>W: PagedEntityResponse<TodoResponse>
    W->>U: Display personal todos widget
```

## Flow Description

1. **Page Initialization** - When the user navigates to `/todos`, the frontend loads team members (for the assignee selector/filter) and the initial todos list for the active team.

2. **Status Filtering** - Todos can be filtered by status: NOT_STARTED, IN_PROGRESS, or COMPLETED. The filter is applied server-side for accurate pagination.

3. **Two-Step Query Pattern** - Like issues, todos use an optimized query pattern: first fetching matching IDs with filters, then loading full entities with eager joins to avoid N+1 problems.

4. **Multi-Assignee Support** - Todos support multiple assignees via a many-to-many join table (`todo_assignees`). The assignee selector allows picking multiple team members.

5. **Todo Creation** - Users create todos with title, optional description, status (defaults to NOT_STARTED), optional due date, and optional assignee list. All assignees must belong to the same team.

6. **Status Workflow** - Todos follow a simple status workflow: NOT_STARTED → IN_PROGRESS → COMPLETED. Status can be changed via the table inline controls or the edit dialog.

7. **Assignee Synchronization** - When updating a todo's assignees, the backend performs a full sync: deletes all existing assignee records and inserts the new set. This ensures the assignee list is always consistent.

8. **Archiving vs Deletion** - Todos support both soft-delete (archive) and hard-delete. Archiving preserves the todo for historical reference, while deletion permanently removes it.

9. **Cross-Team Personal View** - The dashboard widget shows todos assigned to the current user across all their teams, providing a unified personal task list.
