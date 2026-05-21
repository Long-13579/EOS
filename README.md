# EOS

Official repository for the EOS project. This workspace currently contains the web frontend (Vite + React) and shared tooling at the repository root.

## Prerequisites

- Node.js (latest LTS recommended)
- npm (comes with Node.js)

## Install

From the repository root:

```bash
npm run install:all
```

## Run the frontend

```bash
npm run frontend:dev
```

The app will start on the Vite dev server (see the terminal output for the URL).

## Build the frontend

```bash
npm run frontend:build
```

## Lint and format

```bash
npm run frontend:lint
npm run frontend:lint:fix
npm run frontend:format
```

## Useful scripts (root)

| Script                      | Description                         |
| --------------------------- | ----------------------------------- |
| `npm run install:all`       | Install necessary dependencies      |
| `npm run frontend:dev`      | Start the frontend dev server       |
| `npm run frontend:build`    | Type-check and build the frontend   |
| `npm run frontend:lint`     | Run ESLint in the frontend          |
| `npm run frontend:lint:fix` | Fix lint issues in the frontend     |
| `npm run frontend:format`   | Format frontend files with Prettier |

## Notes

- Frontend sources live under `frontend/`. See the frontend README for more details about the UI stack and structure.
- Frontend production deployment workflow docs: `docs/deployment/frontend-vercel.md`.
