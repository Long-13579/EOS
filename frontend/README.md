# Web Frontend

This directory contains the frontend application for the EOS project. It is built using [React](https://react.dev/) and initialized with [Vite](https://vitejs.dev/).

## 🛠️ Tech Stack

### Core

- **Framework**: React 19
- **Build Tool**: Vite 7 (with SWC)
- **Language**: TypeScript 5.9

### State Management & Data Fetching

- **Global State**: [Zustand](https://github.com/pmndrs/zustand)
- **Server State / Caching**: [TanStack Query](https://tanstack.com/query/latest)
- **Data Fetching**: [Axios](https://www.npmjs.com/package/axios)

### Routing & Navigation

- **Routing**: [TanStack Router](https://tanstack.com/router/latest)

### Styling & UI

- **Styling**: [Tailwind CSS 4](https://tailwindcss.com/)
- **Charts**: [Recharts](https://recharts.org/)
- **Tables**: [TanStack Table](https://tanstack.com/table/latest)

## UI Components (ShadCN)

- Add a new ShadCN component:

```bash
npx shadcn@latest add button
```

- Components are generated in src/components/ui and can be customized directly.

### Forms & Validation

- **Form Handling**: [React Hook Form](https://react-hook-form.com/)
- **Schema Validation**: [Zod](https://zod.dev/)

### Code Quality

- **Linting**: ESLint (v9)
- **Formatting**: Prettier

## 🚀 Getting Started

### Prerequisites

- Node.js (Latest LTS recommended)
- [npm](https://www.npmjs.com/) (Package Manager)

### Installation

1. Navigate to the frontend directory:

    ```bash
    cd frontend
    ```

2. Install dependencies:
    ```bash
    npm install
    ```

## 💻 Available Scripts

| Command            | Description                                            |
| ------------------ | ------------------------------------------------------ |
| `npm run dev`      | Starts the development server.                         |
| `npm run build`    | Type-checks and builds the project for production.     |
| `npm run preview`  | Preview the production build locally (after building). |
| `npm run lint`     | Runs ESLint to check for code quality issues.          |
| `npm run lint:fix` | Runs ESLint and automatically fixes fixable issues.    |
| `npm run format`   | Formats all files using Prettier.                      |

## 📂 Project Structure

```text
frontend/
├── public/          # Static assets
├── src/             # Source code
│   ├── assets/      # Images, fonts, etc.
│   ├── App.tsx      # Main application component
│   ├── main.tsx     # Application entry point
│   └── ...
├── eslint.config.js # ESLint configuration
├── package.json     # Project dependencies and scripts
└── vite.config.ts   # Vite configuration
```

## 🔧 Configuration

- **Vite**: Configured in `vite.config.ts`.
- **TypeScript**: Configured in `tsconfig.json`, `tsconfig.app.json`, and `tsconfig.node.json`.
- **Tailwind**: Configured via `@tailwindcss/vite` plugin.
- **ESLint/Prettier**: Configured in `eslint.config.js`, `.prettierrc`.

## 🚢 Deployment

- Production deployment is handled by GitHub Actions + Vercel.
- Workflow files:
    - `.github/workflows/ci-frontend.yaml` (pull request validation for `main`)
    - `.github/workflows/deploy.yml` (manual unified deployment; choose `deploy_target=frontend` for frontend-only)
- Setup and troubleshooting guide: `../docs/deployment/frontend-vercel.md`.
