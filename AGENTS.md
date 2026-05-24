# Codebase Guide

## Stack
- **Backend**: Java 21, Spring Boot 3, Maven, PostgreSQL, Liquibase, MapStruct, JPA/Hibernate
- **Frontend**: React 19, TypeScript, Vite, TanStack Router, TanStack Query, Zustand, shadcn/ui, react-hook-form + zod

## Backend Patterns

### File Structure
```
controller/ → service/ → repository/ (JPA)
dto/request/ + dto/response/ → mapper/ (MapStruct) → entity/
```

### Pagination Pattern (two-step ID fetch)
Every list endpoint uses this flow to avoid N+1 and support JOIN FETCH:
1. `Repository` — `Page<UUID> findXxxIdsByYyy(...)` (lightweight ID-only query)
2. `Repository` — `List<Xxx> findAllByIdIn(List<UUID> ids)` (with JOIN FETCH)
3. `Service` — Collects IDs into `Map<UUID, Entity>`, stream order-preserving, maps via `toResponse()`
4. Returns `PagedEntityResponse<T>` (wraps `data: T[]` + `pagination`)

Request: `PaginationRequest(page, limit)` with defaults (page=1, limit=10, max 20).  
Response: `PagedEntityResponse<T>` with `PaginationResponse(page, limit, totalItems, totalPages, hasNext, hasPrev)`.

### DTO Conventions
- Request records: `@NotNull`, `@NotBlank`, `@Min`/`@Max` validation annotations
- Response records: `@JsonInclude(Include.NON_NULL)` on the record, `TeamBaseResponse(id, name)`, `UserBaseResponse(id, firstName, lastName, email)`
- `CreateXxxRequest` → `XxxResponse`

### Security
- `@PreAuthorize("hasRole('ADMIN') or @teamSecurityService.isCurrentUserMemberOfTeam(#teamId)")`
- `@AuthenticationPrincipal CustomUserDetails`
- `CustomUserDetails` wraps UUID `id`

### Error Handling
- `ResourceNotFoundException` — 404, field-level error map
- `ConflictException` — 409, field-level error map
- `AuthException.forbidden()` — 403
- Frontend maps via `apiErrorNormalizer` → `useFormError` (reads `details` map, sets field errors on matching form fields)

### Seeder
- `@Profile("dev")` + `CommandLineRunner` + `@Order`
- Check `repository.count() > 0` to skip (idempotent)
- Use repositories directly for entity creation

## Frontend Patterns

### Feature Structure
```
features/<name>/
├── components/    # XxxDialog, XxxCard, XxxForm, XxxTable
├── hooks/         # useXxx (query), useCreateXxx, useUpdateXxx (mutation)
├── services/      # API calls via apiGet/apiPost/apiPut/apiPatch/apiDelete
├── schemas/       # Zod schemas
├── types/         # TypeScript interfaces + XxxKeys (React Query key factory)
└── index.ts       # Barrel exports
```

### Query Keys Pattern
```ts
export const xxxKeys = {
    all: ['xxx'] as const,
    lists: () => [...xxxKeys.all, 'list'] as const,
    list: (params: Params) => [...xxxKeys.lists(), params] as const,
    detail: (id: string) => [...xxxKeys.all, 'detail', id] as const,
};
```
Invalidate lists on create/update: `queryClient.invalidateQueries({ queryKey: xxxKeys.lists() })`.

### Dialog + Form Pattern
- `XxxDialog` wraps `Dialog` from shadcn, takes `isOpen`, `onOpenChange`, optional `editingItem`
- `XxxForm` uses `useForm` + `zodResolver`, `Controller` for each field
- Team select: `TeamMemberSelect` (single) or `MultipleTeamMembersSelect` (multi)
- Date picker: `DatePicker` from `@/components/shared/DatePicker` (date-fns)
- Error handling: `catch(error) → normalizeApiError → useFormError(normalized) → toast.error()`
- Active team: `useActiveTeamId()` from `@/hooks/useActiveTeamId` (returns `string | undefined`)

### Key Imports
```ts
import { apiGet, apiPost } from '@/utils/apiRequest';
import { normalizeApiError } from '@/utils/apiErrorNormalizer';
import { useFormError } from '@/hooks/useFormError';
import { useActiveTeamId } from '@/hooks/useActiveTeamId';
import { useTeamStore } from '@/stores/useTeamStore';
import { PaginatedResponse, PaginationParams, DEFAULT_LIMIT } from '@/types/pagination';
```

### Pagination Component
`CustomPagination(currentPage, totalPages, onPageChange)` from `@/components/shared/CustomPagination`.

## L10 Meeting Specifics
- Entity: `L10Meeting(id, team, meetingDate, meetingTime, weekStartDate, facilitator, scribe, status, concludeKeyDecisions, concludeCascadingMessage, ratings)`
- Status: `SCHEDULED`, `STARTED`, `FINISHED`
- Constraint: one meeting per team per week
- Backend sorts: SCHEDULED asc, FINISHED desc by meetingDate/meetingTime/id
