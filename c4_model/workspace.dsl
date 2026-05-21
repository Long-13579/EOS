workspace "EOS Platform - Team Management" "A full-stack web application for team management using the EOS/Traction methodology" {

    !docs "docs"
    !identifiers hierarchical

    model {
        # =============================================
        # PEOPLE / ACTORS
        # =============================================
        admin = person "Admin" "Manages users, teams, and system-wide configuration" "Admin"
        teamMember = person "Team Member" "Participates in EOS meetings, tracks rocks, todos, issues, and metrics" "TeamMember"

        # =============================================
        # EXTERNAL SYSTEMS
        # =============================================
        googleOAuth = softwareSystem "Google OAuth 2.0" "Provides user authentication via Google accounts" "ExternalSystem"

        # =============================================
        # MAIN SOFTWARE SYSTEM
        # =============================================
        eos = softwareSystem "EOS Platform" "Web application for team management following the EOS/Traction methodology" {

            # -----------------------------------------
            # CONTAINER: Frontend Application
            # -----------------------------------------
            webapp = container "Web Application" "Single-page application for EOS team management" "React 19, TypeScript, Vite, Tailwind CSS" "WebApp" {
                authModule = component "Auth Module" "Handles Google OAuth login, token refresh, and session management" "React, @react-oauth/google"
                dashboardPage = component "Dashboard" "Overview page with rocks and todos widgets" "React, TanStack Router"
                rocksModule = component "Rocks Module" "Manage quarterly goals by company, department, and individual categories" "React, TanStack Query"
                issuesModule = component "Issues Module" "Track and categorize team issues with type filtering" "React, TanStack Query"
                todosModule = component "Todos Module" "Manage action items with multi-assignee support and status tracking" "React, TanStack Query"
                headlinesModule = component "Headlines Module" "Share team announcements and updates" "React, TanStack Query"
                scorecardModule = component "Scorecard Module" "Track KPI metrics with weekly values and trend visualization" "React, TanStack Query, Recharts"
                settingsModule = component "Settings Module" "Admin-only user and team management" "React, TanStack Query"
                teamSelector = component "Team Selector" "Global team context switcher persisted to localStorage" "React, Zustand"
                stateManagement = component "State Management" "Client state via Zustand stores, server state via TanStack Query" "Zustand, TanStack Query"
                axiosClient = component "HTTP Client" "Axios instance with JWT cookie auth and single-flight token refresh" "Axios"
            }

            # -----------------------------------------
            # CONTAINER: Backend API
            # -----------------------------------------
            backendApi = container "Backend API" "RESTful API providing business logic and data access" "Spring Boot 4, Java 21" "API" {
                authController = component "Auth Controller" "POST /auth/google, /auth/refresh, /auth/logout" "Spring MVC" {
                    !docs "docs/authentication"
                }
                userController = component "User Controller" "CRUD /users with pagination and search" "Spring MVC"
                teamController = component "Team Controller" "CRUD /teams with member management" "Spring MVC"
                rockController = component "Rock Controller" "CRUD /rocks with status, category, and archive support" "Spring MVC" {
                    !docs "docs/rocks"
                }
                issueController = component "Issue Controller" "CRUD /issues with type filtering and pagination" "Spring MVC" {
                    !docs "docs/issues"
                }
                todoController = component "Todo Controller" "CRUD /todos with multi-assignee and status filtering" "Spring MVC" {
                    !docs "docs/todos"
                }
                headlineController = component "Headline Controller" "CRUD /headlines with archive support" "Spring MVC"
                metricController = component "Metric Controller" "CRUD /metrics, /metric-values, and /metrics/trends" "Spring MVC" {
                    !docs "docs/scorecard"
                }
                weekController = component "Week Controller" "GET /weeks - last 13 weeks for scorecard" "Spring MVC"
                quarterController = component "Quarter Controller" "GET /quarters for rock management" "Spring MVC"
                yearController = component "Year Controller" "GET /years for rock management" "Spring MVC"
                securityFilter = component "JWT Security Filter" "Validates JWT from HTTP-only cookies, enforces role-based access" "Spring Security, JJWT"
                googleOAuthService = component "Google OAuth Service" "Exchanges authorization codes for Google user info" "Google API Client"
                teamSecurityService = component "Team Security Service" "SpEL bean for @PreAuthorize team membership checks" "Spring Security"
                dataAccessLayer = component "Data Access Layer" "JPA repositories, MapStruct mappers, Liquibase migrations" "Spring Data JPA, MapStruct, Liquibase"
                schedulers = component "Background Schedulers" "Token cleanup (Sun 2AM, daily midnight) and week creation (Mon 00:01)" "Spring Scheduling"
            }

            # -----------------------------------------
            # CONTAINER: Database
            # -----------------------------------------
            database = container "Database" "Stores users, teams, rocks, issues, todos, headlines, metrics, and auth tokens" "PostgreSQL 16" "Database"
        }

        # =============================================
        # RELATIONSHIPS - System Context Level (L1)
        # =============================================
        admin -> eos "Manages users, teams, and views all EOS data"
        teamMember -> eos "Tracks rocks, issues, todos, headlines, and metrics"
        eos -> googleOAuth "Authenticates users via" "OAuth 2.0 / HTTPS"

        # =============================================
        # RELATIONSHIPS - Container Level (L2)
        # =============================================
        admin -> eos.webapp "Manages users and teams via browser" "HTTPS"
        teamMember -> eos.webapp "Interacts with EOS features via browser" "HTTPS"
        eos.webapp -> eos.backendApi "Makes API calls to" "JSON/HTTPS"
        eos.backendApi -> eos.database "Reads from and writes to" "JDBC/SQL"
        eos.backendApi -> googleOAuth "Exchanges auth codes and verifies tokens" "HTTPS"

        # =============================================
        # RELATIONSHIPS - Component Level (L3) - Frontend
        # =============================================
        teamMember -> eos.webapp.authModule "Logs in via Google"
        teamMember -> eos.webapp.dashboardPage "Views overview dashboard"
        teamMember -> eos.webapp.rocksModule "Manages quarterly goals"
        teamMember -> eos.webapp.issuesModule "Tracks team issues"
        teamMember -> eos.webapp.todosModule "Manages action items"
        teamMember -> eos.webapp.headlinesModule "Shares announcements"
        teamMember -> eos.webapp.scorecardModule "Tracks KPI metrics"
        admin -> eos.webapp.settingsModule "Manages users and teams"

        eos.webapp.authModule -> eos.webapp.stateManagement "Stores user session"
        eos.webapp.teamSelector -> eos.webapp.stateManagement "Reads/writes active team"
        eos.webapp.dashboardPage -> eos.webapp.stateManagement "Reads active team and user"
        eos.webapp.rocksModule -> eos.webapp.stateManagement "Reads active team context"
        eos.webapp.issuesModule -> eos.webapp.stateManagement "Reads active team context"
        eos.webapp.todosModule -> eos.webapp.stateManagement "Reads active team context"
        eos.webapp.headlinesModule -> eos.webapp.stateManagement "Reads active team context"
        eos.webapp.scorecardModule -> eos.webapp.stateManagement "Reads active team context"

        eos.webapp.authModule -> eos.webapp.axiosClient "Makes auth API calls"
        eos.webapp.rocksModule -> eos.webapp.axiosClient "Makes rocks API calls"
        eos.webapp.issuesModule -> eos.webapp.axiosClient "Makes issues API calls"
        eos.webapp.todosModule -> eos.webapp.axiosClient "Makes todos API calls"
        eos.webapp.headlinesModule -> eos.webapp.axiosClient "Makes headlines API calls"
        eos.webapp.scorecardModule -> eos.webapp.axiosClient "Makes metrics API calls"
        eos.webapp.settingsModule -> eos.webapp.axiosClient "Makes users/teams API calls"

        eos.webapp.axiosClient -> eos.backendApi.securityFilter "Sends requests with JWT cookies" "JSON/HTTPS"

        # =============================================
        # RELATIONSHIPS - Component Level (L3) - Backend
        # =============================================
        eos.backendApi.securityFilter -> eos.backendApi.authController "Routes auth requests"
        eos.backendApi.securityFilter -> eos.backendApi.userController "Routes user requests"
        eos.backendApi.securityFilter -> eos.backendApi.teamController "Routes team requests"
        eos.backendApi.securityFilter -> eos.backendApi.rockController "Routes rock requests"
        eos.backendApi.securityFilter -> eos.backendApi.issueController "Routes issue requests"
        eos.backendApi.securityFilter -> eos.backendApi.todoController "Routes todo requests"
        eos.backendApi.securityFilter -> eos.backendApi.headlineController "Routes headline requests"
        eos.backendApi.securityFilter -> eos.backendApi.metricController "Routes metric requests"
        eos.backendApi.securityFilter -> eos.backendApi.weekController "Routes week requests"
        eos.backendApi.securityFilter -> eos.backendApi.quarterController "Routes quarter requests"
        eos.backendApi.securityFilter -> eos.backendApi.yearController "Routes year requests"

        eos.backendApi.authController -> eos.backendApi.googleOAuthService "Delegates OAuth code exchange"
        eos.backendApi.googleOAuthService -> googleOAuth "Exchanges auth code for user info" "HTTPS"

        eos.backendApi.rockController -> eos.backendApi.teamSecurityService "Validates team membership"
        eos.backendApi.issueController -> eos.backendApi.teamSecurityService "Validates team membership"
        eos.backendApi.todoController -> eos.backendApi.teamSecurityService "Validates team membership"
        eos.backendApi.headlineController -> eos.backendApi.teamSecurityService "Validates team membership"
        eos.backendApi.metricController -> eos.backendApi.teamSecurityService "Validates team membership"

        eos.backendApi.authController -> eos.backendApi.dataAccessLayer "CRUD users, refresh tokens, invalidated tokens"
        eos.backendApi.userController -> eos.backendApi.dataAccessLayer "CRUD users with pagination"
        eos.backendApi.teamController -> eos.backendApi.dataAccessLayer "CRUD teams and members"
        eos.backendApi.rockController -> eos.backendApi.dataAccessLayer "CRUD rocks with year/quarter filtering"
        eos.backendApi.issueController -> eos.backendApi.dataAccessLayer "CRUD issues with type filtering"
        eos.backendApi.todoController -> eos.backendApi.dataAccessLayer "CRUD todos with assignee management"
        eos.backendApi.headlineController -> eos.backendApi.dataAccessLayer "CRUD headlines"
        eos.backendApi.metricController -> eos.backendApi.dataAccessLayer "CRUD metrics and metric values"
        eos.backendApi.weekController -> eos.backendApi.dataAccessLayer "Reads week calendar data"
        eos.backendApi.quarterController -> eos.backendApi.dataAccessLayer "Reads quarter definitions"
        eos.backendApi.yearController -> eos.backendApi.dataAccessLayer "Reads year entries"
        eos.backendApi.schedulers -> eos.backendApi.dataAccessLayer "Cleans expired tokens, creates weekly entries"

        eos.backendApi.dataAccessLayer -> eos.database "Executes queries and persists data" "JDBC/SQL"
    }

    views {
        properties {
            "plantuml.url" "https://plantuml.com/plantuml"
            "plantuml.format" "svg"
            "generatr.site.externalTag" "ExternalSystem"
            "generatr.style.customStylesheet" "site/custom.css"
            "generatr.style.colors.primary" "#2563eb"
            "generatr.style.colors.secondary" "#ffffff"
            "generatr.site.nestGroups" "true"
            "generatr.site.theme" "light"
            "generatr.markdown.flexmark.extensions" "Tables,Admonition"
        }

        # L1: System Context
        systemContext eos "SystemContext" "System context - all actors and external systems" {
            include *
            autoLayout
        }

        # L2: Container Diagram
        container eos "Containers" "Container architecture of the EOS Platform" {
            include *
            autoLayout
        }

        # L3: Frontend Components
        component eos.webapp "WebappComponents" "Frontend application components" {
            include *
            autoLayout
        }

        # L3: Backend Components
        component eos.backendApi "BackendComponents" "Backend API components" {
            include *
            autoLayout lr 400 100
        }

        # L4: PlantUML Flow Diagrams
        image eos.backendApi.authController "AuthenticationFlow" {
            plantuml docs/authentication/flow.puml
            title "Authentication - Google OAuth Login Flow"
        }

        image eos.backendApi.rockController "RocksFlow" {
            plantuml docs/rocks/flow.puml
            title "Rocks - Quarterly Goal Management Flow"
        }

        image eos.backendApi.issueController "IssuesFlow" {
            plantuml docs/issues/flow.puml
            title "Issues - Issue Tracking Flow"
        }

        image eos.backendApi.todoController "TodosFlow" {
            plantuml docs/todos/flow.puml
            title "Todos - Task Management Flow"
        }

        image eos.backendApi.metricController "ScorecardFlow" {
            plantuml docs/scorecard/flow.puml
            title "Scorecard - Metrics and Trends Flow"
        }

        # -----------------------------------------
        # STYLES
        # -----------------------------------------
        styles {
            element "Software System" {
                background #2563eb
                color #ffffff
                shape RoundedBox
            }
            element "ExternalSystem" {
                background #999999
                color #ffffff
                shape RoundedBox
            }
            element "Person" {
                background #3b82f6
                color #ffffff
                shape Person
            }
            element "Admin" {
                background #7c3aed
                color #ffffff
                shape Person
            }
            element "TeamMember" {
                background #3b82f6
                color #ffffff
                shape Person
            }
            element "Container" {
                background #3b82f6
                color #ffffff
            }
            element "WebApp" {
                background #2563eb
                color #ffffff
                shape WebBrowser
            }
            element "API" {
                background #0f172a
                color #ffffff
                shape Hexagon
            }
            element "Database" {
                background #92400e
                color #ffffff
                shape Cylinder
            }
            element "Component" {
                background #60a5fa
                color #ffffff
            }
            relationship "Relationship" {
                thickness 2
                color #555555
            }
        }

        theme default
    }
}
