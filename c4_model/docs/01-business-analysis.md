# EOS Platform - Business Analysis

## 1. System Overview

The EOS Platform is a web-based team management application built on the Entrepreneurial Operating System (EOS/Traction) methodology. It enables organizations to align teams around quarterly goals, track key metrics, manage issues, and run productive Level 10 meetings.

```
┌─────────────────────────────────────────────────────────────────────┐
│                        EOS PLATFORM                                 │
│                                                                     │
│   ┌──────────┐    ┌──────────────────────────────────────────┐     │
│   │  Admin /  │    │           Web Application                │     │
│   │  Team     │───▶│  React 19 + TypeScript + Tailwind CSS    │     │
│   │  Member   │    │  (SPA served via Vercel)                 │     │
│   └──────────┘    └────────────────┬─────────────────────────┘     │
│                                    │ JSON/HTTPS                     │
│                                    ▼                                │
│                    ┌──────────────────────────────────────────┐     │
│                    │           Backend API                     │     │
│                    │  Spring Boot 4 + Java 21                 │     │
│                    │  (Deployed on Render)                     │     │
│                    └────────────────┬─────────────────────────┘     │
│                                    │ JDBC/SQL                       │
│                                    ▼                                │
│                    ┌──────────────────────────────────────────┐     │
│   ┌──────────┐    │           PostgreSQL 16                   │     │
│   │  Google   │    │  (Managed Database)                      │     │
│   │  OAuth    │◀───│                                          │     │
│   └──────────┘    └──────────────────────────────────────────┘     │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

## 2. Problem Being Solved

Organizations implementing the EOS/Traction methodology need a centralized digital tool to:

- **Replace spreadsheets and whiteboards** used to track quarterly Rocks (goals), weekly Scorecards (KPIs), and team Issues
- **Provide real-time visibility** across teams so leadership can see progress at a glance
- **Standardize the EOS process** with structured workflows for L10 meetings, issue resolution, and goal setting
- **Enable accountability** by assigning owners to rocks, todos, and metrics with clear deadlines and status tracking

Without a dedicated tool, teams revert to fragmented tracking across spreadsheets, sticky notes, and email, losing the discipline that makes EOS effective.

## 3. Core Capabilities

```
┌─────────────────────────────────────────────────────────────┐
│                    CORE CAPABILITIES                         │
├──────────────────────┬──────────────────────────────────────┤
│                      │                                      │
│  ┌────────────────┐  │  ┌────────────────────────────────┐  │
│  │  Rocks         │  │  │  Scorecard / Metrics            │  │
│  │  (Quarterly    │  │  │  (Weekly KPI tracking           │  │
│  │   Goals)       │  │  │   with trend analysis)          │  │
│  └────────────────┘  │  └────────────────────────────────┘  │
│                      │                                      │
│  ┌────────────────┐  │  ┌────────────────────────────────┐  │
│  │  Issues        │  │  │  Headlines                      │  │
│  │  (Problem      │  │  │  (Team announcements            │  │
│  │   Tracking)    │  │  │   and updates)                  │  │
│  └────────────────┘  │  └────────────────────────────────┘  │
│                      │                                      │
│  ┌────────────────┐  │  ┌────────────────────────────────┐  │
│  │  Todos         │  │  │  Team Management                │  │
│  │  (Action Items │  │  │  (Users, roles, and             │  │
│  │   & Tasks)     │  │  │   multi-team support)           │  │
│  └────────────────┘  │  └────────────────────────────────┘  │
│                      │                                      │
└──────────────────────┴──────────────────────────────────────┘
```

## 4. Overall Functional Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                      PRESENTATION LAYER                          │
│                                                                  │
│  ┌──────────┐ ┌──────┐ ┌──────┐ ┌──────┐ ┌─────────┐ ┌──────┐ │
│  │Dashboard │ │Rocks │ │Issues│ │Todos │ │Scorecard│ │Heads │ │
│  │  Page    │ │ Page │ │ Page │ │ Page │ │  Page   │ │ Page │ │
│  └──────────┘ └──────┘ └──────┘ └──────┘ └─────────┘ └──────┘ │
│  ┌──────────┐ ┌──────────────┐ ┌────────────────────────────┐  │
│  │ Settings │ │ Auth/Login   │ │ Shared: Sidebar, TeamSelect│  │
│  │  Page    │ │    Page      │ │ Pagination, Dialogs        │  │
│  └──────────┘ └──────────────┘ └────────────────────────────┘  │
├─────────────────────────────────────────────────────────────────┤
│                      STATE LAYER                                 │
│                                                                  │
│  ┌─────────────────────┐  ┌──────────────────────────────────┐  │
│  │ Zustand Stores      │  │ TanStack Query Cache             │  │
│  │ (User, Active Team) │  │ (Server state, 5-min stale time) │  │
│  └─────────────────────┘  └──────────────────────────────────┘  │
├─────────────────────────────────────────────────────────────────┤
│                      API / TRANSPORT LAYER                       │
│                                                                  │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │ Axios HTTP Client (JWT cookies, auto-refresh, CORS)      │   │
│  └──────────────────────────────────────────────────────────┘   │
├══════════════════════════════════════════════════════════════════╡
│                      BACKEND API LAYER                           │
│                                                                  │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │ Spring Security (JWT Filter, Role & Team Checks)        │    │
│  └─────────────────────────────────────────────────────────┘    │
│  ┌──────┐ ┌──────┐ ┌──────┐ ┌──────┐ ┌──────┐ ┌──────────┐   │
│  │Auth  │ │User  │ │Team  │ │Rock  │ │Issue │ │Todo      │   │
│  │Ctrl  │ │Ctrl  │ │Ctrl  │ │Ctrl  │ │Ctrl  │ │Ctrl      │   │
│  └──────┘ └──────┘ └──────┘ └──────┘ └──────┘ └──────────┘   │
│  ┌──────┐ ┌──────┐ ┌──────┐ ┌──────┐ ┌──────────────────┐    │
│  │Head  │ │Metric│ │Week  │ │Qtr/Yr│ │Schedulers        │    │
│  │Ctrl  │ │Ctrl  │ │Ctrl  │ │Ctrls │ │(Token,Week)      │    │
│  └──────┘ └──────┘ └──────┘ └──────┘ └──────────────────┘    │
├─────────────────────────────────────────────────────────────────┤
│                      SERVICE LAYER                               │
│                                                                  │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │ 17 Service Interfaces + Implementations                  │   │
│  │ (Auth, User, Team, Rock, Issue, Todo, Headline, Metric,  │   │
│  │  MetricValue, Week, Quarter, Year, Role, GoogleOAuth)    │   │
│  └──────────────────────────────────────────────────────────┘   │
├─────────────────────────────────────────────────────────────────┤
│                      DATA ACCESS LAYER                           │
│                                                                  │
│  ┌────────────────┐  ┌────────────────┐  ┌──────────────────┐  │
│  │ JPA Repos (15) │  │ MapStruct (13) │  │ Liquibase (47+)  │  │
│  └────────────────┘  └────────────────┘  └──────────────────┘  │
├─────────────────────────────────────────────────────────────────┤
│                      INFRASTRUCTURE                              │
│                                                                  │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │ PostgreSQL 16  │  Google OAuth 2.0  │  Docker / Render   │   │
│  └──────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
```

## 5. Key Lifecycle / Flow

The primary user journey through the EOS Platform follows the weekly L10 meeting rhythm:

```
┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐
│  1. LOGIN│───▶│ 2. SELECT│───▶│ 3. CHECK │───▶│ 4. UPDATE│
│  Google  │    │   TEAM   │    │ DASHBOARD│    │ SCORECARD│
│  OAuth   │    │          │    │          │    │ (weekly) │
└──────────┘    └──────────┘    └──────────┘    └──────────┘
                                                      │
     ┌────────────────────────────────────────────────┘
     ▼
┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐
│ 5. REVIEW│───▶│ 6. TRACK │───▶│ 7. MANAGE│───▶│ 8. SHARE │
│  ROCKS   │    │  ISSUES  │    │  TODOS   │    │ HEADLINES│
│ (quarter)│    │          │    │          │    │          │
└──────────┘    └──────────┘    └──────────┘    └──────────┘
                                                      │
     ┌────────────────────────────────────────────────┘
     ▼
┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐
│ 9. L10   │───▶│10. AGENDA│───▶│11. CONCL-│───▶│12. VIEW  │
│ MEETING  │    │  SESSIONS│    │ UDE +    │    │ HISTORY  │
│ (sched.) │    │  (Segue, │    │ RATINGS  │    │ (finished│
│          │    │  Score..) │    │          │    │ meetings)│
└──────────┘    └──────────┘    └──────────┘    └──────────┘
```

**Step-by-step:**

1. **Login** - User authenticates via Google OAuth 2.0 (one-click sign-in)
2. **Select Team** - Choose active team from the global team selector
3. **Check Dashboard** - View overview widgets showing personal rocks and todos
4. **Update Scorecard** - Enter weekly metric values, review trends against goals
5. **Review Rocks** - Check quarterly rock status (On Track / Off Track / Completed / Deferred)
6. **Track Issues** - Log new issues, categorize by type, discuss in meetings
7. **Manage Todos** - Create action items, assign to team members, update statuses
8. **Share Headlines** - Post team announcements and good news
9. **Schedule L10 Meeting** - Create a weekly meeting with facilitator, scribe, date, and time (one meeting per team per week enforced)
10. **Agenda Sessions** - Start the meeting and navigate through fixed sessions: Segue, Scorecard Review, Rock Review, Headlines, To-dos, Issues, Conclude. Facilitator/scribe controls the flow.
11. **Conclude + Ratings** - Record key decisions and cascading messages. Each member submits a 1-10 rating (or Absent) before the meeting finishes.
12. **View History** - Review past finished meetings with read-only conclude notes and rating results.

## 6. User Roles & Permissions

```
┌────────────────┬───────────────────────────────────────────────────┐
│     ROLE       │                 PERMISSIONS                       │
├────────────────┼───────────────────────────────────────────────────┤
│                │                                                   │
│  ADMIN         │  - Create, update, and deactivate users          │
│                │  - Create and manage teams                        │
│                │  - Assign users to teams                          │
│                │  - View all teams' data                           │
│                │  - All Team Member permissions                    │
│                │                                                   │
├────────────────┼───────────────────────────────────────────────────┤
│                │                                                   │
│  TEAM MEMBER   │  - View dashboard with personal widgets          │
│  (USER role)   │  - Schedule and view L10 meetings                │
│                │  - Submit meeting ratings during Conclude phase   │
│                │  - Create/edit/archive rocks for their teams     │
│                │  - Create/edit/delete issues for their teams      │
│                │  - Create/edit/delete todos for their teams       │
│                │  - Create/edit/delete headlines for their teams   │
│                │  - Create/edit metrics and update weekly values   │
│                │  - View scorecard trends across weeks             │
│                │  - Switch between assigned teams                  │
│                │                                                   │
│  FACILITATOR / │  - All Team Member permissions                   │
│  SCRIBE        │  - Start and finish the meeting                  │
│  (per-meeting  │  - Navigate through agenda sessions              │
│   role)        │  - Update conclude fields (key decisions,        │
│                │    cascading message)                            │
│                │  - Edit meeting details                          │
│                │  - Cancel (delete) scheduled meetings            │
│                │                                                   │
└────────────────┴───────────────────────────────────────────────────┘

Access Control Model:
- Role-based: ADMIN vs USER at the application level
- Team-based: Users can only access data for teams they belong to
- Enforced via @PreAuthorize + TeamSecurityService SpEL checks
```

## 7. Technology Stack

| Layer | Technology | Purpose |
|-------|-----------|---------|
| **Frontend Framework** | React 19 + TypeScript | UI rendering and type safety |
| **Build Tool** | Vite 7 | Fast development server and bundling |
| **Styling** | Tailwind CSS 4 + ShadCN/Radix UI | Utility-first CSS with accessible components |
| **Routing** | TanStack Router | File-based routing with auth guards |
| **Server State** | TanStack Query | Caching, background refresh, optimistic updates |
| **Client State** | Zustand | Lightweight stores (user session, active team) |
| **Forms** | React Hook Form + Zod | Form management with schema validation |
| **Charts** | Recharts | Scorecard trend visualization |
| **HTTP Client** | Axios | API calls with interceptor-based token refresh |
| **Auth (Frontend)** | @react-oauth/google | Google OAuth 2.0 auth-code flow |
| **Backend Framework** | Spring Boot 4 + Java 21 | REST API and business logic |
| **Security** | Spring Security + JJWT | JWT authentication and authorization |
| **ORM** | Spring Data JPA / Hibernate | Database access and queries |
| **DTO Mapping** | MapStruct | Compile-time entity-to-DTO mapping |
| **Migrations** | Liquibase | Database schema versioning (47+ changesets) |
| **Database** | PostgreSQL 16 | Relational data storage |
| **Auth (Backend)** | Google API Client | OAuth code exchange and token verification |
| **Frontend Hosting** | Vercel | CDN-delivered SPA |
| **Backend Hosting** | Render | Docker-based API deployment |
| **Container Registry** | Docker Hub | Immutable image storage by commit SHA |
| **CI/CD** | GitHub Actions | Automated testing, building, and deployment |
| **Code Quality** | ESLint + Prettier + Husky | Linting and formatting on pre-commit |

## 8. Key Features

```
┌──────────────────────────────┬──────────────────────────────────┐
│       CORE FEATURES          │      SUPPORTING FEATURES         │
├──────────────────────────────┼──────────────────────────────────┤
│                              │                                  │
│  L10 Meetings                │  Google OAuth Authentication     │
│  - Weekly scheduling         │  - One-click Google sign-in      │
│  - One meeting per team/week │  - JWT access + refresh tokens   │
│  - Facilitator/scribe roles  │  - HTTP-only cookie security     │
│  - Start/finish lifecycle    │                                  │
│  - Agenda sessions (Segue,   │  Team Management                 │
│    Scorecard, Rocks,         │  - Multi-team support            │
│    Headlines, To-dos,        │  - Leadership team flag          │
│    Issues, Conclude)         │  - Member assignment             │
│  - Conclude key decisions    │  - Global team switcher          │
│  - Per-member ratings 1-10   │                                  │
│  - Read-only post-meeting    │  User Management (Admin)         │
│                              │  - Create/update users           │
│  Rocks (Quarterly Goals)     │  - Role assignment               │
│  - Company / Dept / Indiv.   │  - Pagination and search         │
│  - Status tracking           │                                  │
│  - Year/quarter filtering    │  Dashboard                       │
│  - Archive support           │  - Personal rocks widget         │
│                              │  - Personal todos widget         │
│  Issues Tracking             │  - At-a-glance overview          │
│  - Type classification       │                                  │
│  - Short-term / Long-term    │  Background Schedulers           │
│  - Archive support           │  - Token cleanup (weekly/daily)  │
│                              │  - Week calendar auto-creation   │
│  Todos (Action Items)        │                                  │
│  - Multi-assignee support    │  Audit Trail                     │
│  - Status workflow           │  - Created/updated timestamps    │
│  - Due date tracking         │  - Created/updated by user       │
│  - Archive support           │                                  │
│                              │                                  │
│  Scorecard / Metrics         │                                  │
│  - Weekly value entry        │                                  │
│  - Goal comparison           │                                  │
│  - Trend analysis            │                                  │
│  - Multiple unit types       │                                  │
│  - Operator-based goals      │                                  │
│                              │                                  │
│  Headlines                   │                                  │
│  - Team announcements        │                                  │
│  - Archive support           │                                  │
│  - Company / Dept / Indiv.   │  - One-click Google sign-in      │
│  - Status tracking           │  - JWT access + refresh tokens   │
│  - Year/quarter filtering    │  - HTTP-only cookie security     │
│  - Archive support           │                                  │
│                              │  Team Management                 │
│  Issues Tracking             │  - Multi-team support            │
│  - Type classification       │  - Leadership team flag          │
│  - Short-term / Long-term    │  - Member assignment             │
│  - Archive support           │  - Global team switcher          │
│                              │                                  │
│  Todos (Action Items)        │  User Management (Admin)         │
│  - Multi-assignee support    │  - Create/update users           │
│  - Status workflow           │  - Role assignment               │
│  - Due date tracking         │  - Pagination and search         │
│  - Archive support           │                                  │
│                              │  Dashboard                       │
│  Scorecard / Metrics         │  - Personal rocks widget         │
│  - Weekly value entry        │  - Personal todos widget         │
│  - Goal comparison           │  - At-a-glance overview          │
│  - Trend analysis            │                                  │
│  - Multiple unit types       │  Background Schedulers           │
│  - Operator-based goals      │  - Token cleanup (weekly/daily)  │
│                              │  - Week calendar auto-creation   │
│  Headlines                   │                                  │
│  - Team announcements        │  Audit Trail                     │
│  - Archive support           │  - Created/updated timestamps    │
│                              │  - Created/updated by user       │
│                              │                                  │
└──────────────────────────────┴──────────────────────────────────┘
```
