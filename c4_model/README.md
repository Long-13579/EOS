# EOS Platform - C4 Architecture Documentation

C4 architecture documentation for the EOS Platform, built with [Structurizr DSL](https://structurizr.com/dsl) and served via [structurizr-site-generatr](https://github.com/avisi-cloud/structurizr-site-generatr).

## Prerequisites

- [Docker](https://docs.docker.com/get-docker/) installed and running

## Quick Start

Serve the documentation locally on port 8082:

```bash
docker run -it --rm \
  -v $(pwd):/var/model \
  -p 8082:8080 \
  ghcr.io/avisi-cloud/structurizr-site-generatr serve \
  --workspace-file workspace.dsl
```

Open [http://localhost:8082](http://localhost:8082) in your browser.

## Generate Static Site

Build the static HTML site into the `build/` directory:

```bash
docker run -it --rm \
  -v $(pwd):/var/model \
  ghcr.io/avisi-cloud/structurizr-site-generatr generate-site \
  --workspace-file workspace.dsl \
  --output-dir build
```

## Run in Docker Container (with Basic Auth)

Build and run the static site behind Nginx with basic authentication:

```bash
# Generate static site first
docker run -it --rm \
  -v $(pwd):/var/model \
  ghcr.io/avisi-cloud/structurizr-site-generatr generate-site \
  --workspace-file workspace.dsl \
  --output-dir build

# Build the Nginx container
docker build -t eos-c4-docs .

# Run on port 8082
docker run -d --name eos-c4-docs -p 8082:80 eos-c4-docs
```

Default credentials: `eos` / `changeme123` (change in Dockerfile before deploying).

## Share via ngrok (optional)

```bash
ngrok http 8082
```

## Project Structure

```
c4_model/
├── workspace.dsl                  # Structurizr DSL C4 model (L1-L4)
├── docs/
│   ├── 01-business-analysis.md    # Business requirements overview
│   ├── authentication/
│   │   ├── 01-flow.md             # Auth flow (Mermaid sequence diagram)
│   │   └── flow.puml              # Auth flow (PlantUML)
│   ├── rocks/
│   │   ├── 01-flow.md             # Rocks flow (Mermaid)
│   │   └── flow.puml              # Rocks flow (PlantUML)
│   ├── issues/
│   │   ├── 01-flow.md             # Issues flow (Mermaid)
│   │   └── flow.puml              # Issues flow (PlantUML)
│   ├── todos/
│   │   ├── 01-flow.md             # Todos flow (Mermaid)
│   │   └── flow.puml              # Todos flow (PlantUML)
│   └── scorecard/
│       ├── 01-flow.md             # Scorecard flow (Mermaid)
│       └── flow.puml              # Scorecard flow (PlantUML)
├── site/
│   └── custom.css                 # Docusaurus-inspired theme
├── Dockerfile                     # Nginx with basic auth
└── README.md                      # This file
```

## C4 Model Views

| Level | View | Description |
|-------|------|-------------|
| L1 | System Context | EOS Platform with Admin, Team Member, and Google OAuth |
| L2 | Containers | Web Application (React), Backend API (Spring Boot), PostgreSQL |
| L3 | Frontend Components | Auth, Dashboard, Rocks, Issues, Todos, Headlines, Scorecard, Settings modules |
| L3 | Backend Components | Controllers, Security Filter, OAuth Service, Data Access Layer, Schedulers |

## Level 4 Flows

| Flow | Description | Files |
|------|-------------|-------|
| Authentication | Google OAuth login, token refresh, logout | `docs/authentication/` |
| Rocks | Quarterly goal CRUD with year/quarter filtering | `docs/rocks/` |
| Issues | Issue tracking with type classification and views | `docs/issues/` |
| Todos | Task management with multi-assignee support | `docs/todos/` |
| Scorecard | Weekly metrics, value entry, and trend analysis | `docs/scorecard/` |

## Workspace Properties

| Property | Value | Purpose |
|----------|-------|---------|
| `plantuml.url` | `https://plantuml.com/plantuml` | PlantUML rendering server |
| `plantuml.format` | `svg` | Diagram output format |
| `generatr.site.externalTag` | `ExternalSystem` | Hide external systems from sidebar |
| `generatr.style.customStylesheet` | `site/custom.css` | Custom CSS theme |
| `generatr.style.colors.primary` | `#2563eb` | Brand primary color |
| `generatr.site.theme` | `light` | Light theme |
| `generatr.markdown.flexmark.extensions` | `Tables,Admonition` | Markdown extensions |

## Deploying to Production

| Option | Pros | Cons |
|--------|------|------|
| **A: GitHub Pages** | Free, auto-deploy on push | No auth, public only |
| **B: Docker + Caddy** | Auto-HTTPS, basic auth | Requires server |
| **C: Nginx Container** | Lightweight, full control | Manual SSL setup |

### Option A: GitHub Pages

Add to `.github/workflows/c4-docs.yml`:

```yaml
name: Deploy C4 Docs
on:
  push:
    branches: [main]
    paths: ['c4_model/**']
jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Generate site
        run: |
          cd c4_model
          docker run --rm -v $(pwd):/var/model \
            ghcr.io/avisi-cloud/structurizr-site-generatr generate-site \
            --workspace-file workspace.dsl --output-dir build
      - uses: peaceiris/actions-gh-pages@v3
        with:
          github_token: ${{ secrets.GITHUB_TOKEN }}
          publish_dir: ./c4_model/build
```

### Option B: Docker Compose + Caddy

```yaml
services:
  c4-docs:
    build: ./c4_model
    restart: unless-stopped
  caddy:
    image: caddy:alpine
    ports: ["80:80", "443:443"]
    volumes:
      - ./Caddyfile:/etc/caddy/Caddyfile
    restart: unless-stopped
```

### Option C: Standalone Nginx Container

```bash
scp -r c4_model/ user@server:/opt/eos-c4/
ssh user@server "cd /opt/eos-c4 && docker build -t eos-c4-docs . && docker run -d -p 8082:80 eos-c4-docs"
```

## Keeping Documentation Up to Date

Update the C4 documentation when:

- **New features/modules** are added (add components to workspace.dsl)
- **External integrations** change (update system context)
- **Architecture decisions** are made (update business analysis)
- **API endpoints** are added or modified (update flow diagrams)
- **Deployment topology** changes (update container diagram)

Run `docker run ... serve` locally to verify changes before committing.
