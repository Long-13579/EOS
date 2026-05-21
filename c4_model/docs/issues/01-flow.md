# Issues Tracking Flow

## Sequence Diagram

```mermaid
sequenceDiagram
    participant U as User
    participant W as Web Application
    participant B as Backend API
    participant DB as PostgreSQL

    Note over U,DB: Page Load - Fetch Issues

    U->>W: Navigate to /issues
    W->>W: Read active team from Zustand
    W->>B: GET /api/v1/issue-types
    B->>DB: Query issue types (ordered ASC)
    DB->>B: IssueType list
    B->>W: IssueTypeBaseResponse[]

    W->>B: GET /api/v1/issues?teamId=X&view=issues&page=1&pageSize=10
    B->>B: Validate team membership (@PreAuthorize)
    B->>B: Determine filtering by view (issues/long_term/archived)
    B->>DB: Query issue IDs by filter criteria
    DB->>B: Matching issue IDs (paginated)
    B->>DB: Fetch full issues by IDs (with joins: type, creator, team)
    DB->>B: Issue entities with relations
    B->>W: PagedEntityResponse<IssueResponse>
    W->>U: Display issues table with pagination

    Note over U,DB: View Filtering

    U->>W: Switch to "Long Term" view tab
    W->>B: GET /api/v1/issues?teamId=X&view=long_term&page=1
    B->>DB: Query issues where type = "Long Term Issue"
    DB->>B: Long term issues
    B->>W: Paginated issue list
    W->>U: Display filtered issues

    U->>W: Switch to "Archived" view tab
    W->>B: GET /api/v1/issues?teamId=X&isArchived=true&page=1
    B->>DB: Query archived issues
    DB->>B: Archived issues
    B->>W: Paginated issue list
    W->>U: Display archived issues

    Note over U,DB: Create Issue

    U->>W: Click "Add Issue", fill dialog form
    W->>B: POST /api/v1/issues {teamId, title, description, issueTypeId}
    B->>B: Validate team membership
    B->>B: Validate issue type exists (optional)
    B->>DB: Insert issue entity
    DB->>B: Saved issue with ID
    B->>W: IssueResponse
    W->>W: Invalidate issues query cache
    W->>U: Show updated issues list

    Note over U,DB: Update Issue

    U->>W: Edit issue via dialog
    W->>B: PUT /api/v1/issues/{issueId} {title, description, issueTypeId}
    B->>B: Validate team membership via issue
    B->>DB: Update issue fields
    DB->>B: Updated issue
    B->>W: IssueResponse
    W->>W: Invalidate issues query cache
    W->>U: Show updated issue

    Note over U,DB: Archive Issue

    U->>W: Click archive action
    W->>B: PATCH /api/v1/issues/{issueId} {isArchived: true}
    B->>B: Validate team membership
    B->>DB: Update isArchived flag
    DB->>B: Updated issue
    B->>W: IssueResponse
    W->>U: Issue moved to archived view

    Note over U,DB: Delete Issue

    U->>W: Click delete, confirm in dialog
    W->>B: DELETE /api/v1/issues/{issueId}
    B->>B: Validate team membership
    B->>DB: Delete issue record
    DB->>B: Confirmation
    B->>W: 204 No Content
    W->>W: Invalidate issues query cache
    W->>U: Issue removed from list
```

## Flow Description

1. **Page Initialization** - When the user navigates to `/issues`, the frontend fetches available issue types and the initial issues list for the active team. Issues are loaded with pagination support.

2. **Three-View Filtering** - Issues are organized into three views:
   - **Issues** - Active short-term issues (excluding "Long Term Issue" type)
   - **Long Term** - Issues specifically typed as "Long Term Issue"
   - **Archived** - All archived issues regardless of type

3. **Optimized Query Pattern** - The backend uses a two-step query: first fetching matching issue IDs with filters (lightweight query), then loading full issue entities by those IDs with eager joins. This avoids N+1 problems and keeps pagination efficient.

4. **Issue Creation** - Users create issues via a dialog form with title, optional description, and optional issue type. The backend validates team membership before persisting. The creator is automatically set from the JWT token.

5. **Issue Updates** - Issues can be edited to change title, description, or issue type. The backend verifies the requesting user belongs to the issue's team.

6. **Archiving** - Issues support soft-delete via archiving. Archived issues are hidden from the default view but remain accessible in the "Archived" tab.

7. **Hard Delete** - Issues can also be permanently deleted, which removes the record from the database entirely. A confirmation dialog is shown before deletion.

8. **Pagination** - All issue views support server-side pagination with configurable page size, sort field, and sort direction.
