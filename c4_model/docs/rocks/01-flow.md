# Rocks (Quarterly Goals) Flow

## Sequence Diagram

```mermaid
sequenceDiagram
    participant U as User
    participant W as Web Application
    participant B as Backend API
    participant DB as PostgreSQL

    Note over U,DB: Page Load - Fetch Rocks

    U->>W: Navigate to /rocks
    W->>W: Read active team from Zustand
    W->>B: GET /api/v1/years
    B->>DB: Query years (ordered ASC)
    DB->>B: Year list
    B->>W: YearResponse[]

    W->>B: GET /api/v1/quarters
    B->>DB: Query quarters (ordered ASC)
    DB->>B: Quarter list
    B->>W: QuarterResponse[]

    W->>W: Determine current year/quarter defaults
    W->>B: GET /api/v1/rocks?teamId=X&yearId=Y&quarterId=Z
    B->>B: Validate team membership (@PreAuthorize)
    B->>DB: Query rocks with joins (team, owner, year, quarter)
    DB->>B: Rock entities
    B->>W: RockListResponse (company, department, individual)
    W->>U: Display rocks grouped by category

    Note over U,DB: Create Rock

    U->>W: Click "Add Rock", fill form
    W->>B: POST /api/v1/rocks {teamId, yearId, quarterId, title, description, status, category, dueDate, ownerId}
    B->>B: Validate team membership
    B->>B: Validate owner belongs to team
    B->>B: Validate year and quarter exist
    B->>DB: Insert rock entity
    DB->>B: Saved rock with ID
    B->>W: RockResponse
    W->>W: Invalidate rocks query cache
    W->>U: Show updated rocks list

    Note over U,DB: Update Rock Status

    U->>W: Change rock status dropdown
    W->>B: PATCH /api/v1/rocks/{rockId}/status {status}
    B->>B: Validate team membership via rock
    B->>DB: Update rock status
    DB->>B: Updated rock
    B->>W: RockResponse
    W->>W: Invalidate rocks query cache
    W->>U: Show updated status

    Note over U,DB: Archive Rock

    U->>W: Click archive action
    W->>B: PATCH /api/v1/rocks/{rockId} {isArchived: true}
    B->>B: Validate team membership
    B->>DB: Update isArchived flag
    DB->>B: Updated rock
    B->>W: RockResponse
    W->>W: Invalidate rocks query cache
    W->>U: Rock moved to archived view

    Note over U,DB: My Rocks (Dashboard Widget)

    U->>W: View dashboard
    W->>B: GET /api/v1/rocks/me?yearId=Y&quarterId=Z
    B->>B: Extract userId from JWT
    B->>DB: Query active rocks by owner, year, quarter
    DB->>B: User's rock list
    B->>W: UserRockListResponse
    W->>U: Display personal rocks widget
```

## Flow Description

1. **Page Initialization** - When the user navigates to `/rocks`, the frontend reads the active team from the Zustand store, then fetches years and quarters to populate the selector dropdowns.

2. **Year/Quarter Selection** - The `useYearQuarterDefaults` hook determines the current year and quarter. The user can switch to view historical data using the `YearQuarterSelector` component.

3. **Rocks Fetching** - Rocks are fetched filtered by team, year, and quarter. The backend validates that the requesting user belongs to the team via `@PreAuthorize` and `TeamSecurityService`. Results are returned grouped by category (Company, Department, Individual).

4. **Rock Creation** - The user fills out a dialog form with title, description, category, status, owner, due date, year, and quarter. The backend validates team membership for both the creator and the assigned owner before persisting.

5. **Status Updates** - Rock status can be updated independently (ON_TRACK, OFF_TRACK, COMPLETED, DEFERRED) via a dedicated PATCH endpoint for quick status changes without opening the full edit form.

6. **Full Rock Update** - The edit dialog allows modifying all rock fields. The backend re-validates ownership and team membership on each update.

7. **Archiving** - Rocks can be archived (soft delete) rather than permanently deleted. Archived rocks are hidden from the default view but accessible via an archive filter.

8. **Personal Rocks View** - The dashboard widget and "My Rocks" view show rocks assigned to the current user across all their teams, filtered by the current year and quarter.
