# Frontend Deployment (GitHub Actions + Vercel)

This guide explains how to set up and operate frontend production deployment to Vercel.

## Workflow File

- `.github/workflows/ci-frontend.yaml`
- `.github/workflows/deploy.yml`

## One-Time Setup

Complete these steps once per repository.

1. Create/link the Vercel project
   - In Vercel, create a project for this frontend.
   - From the repository, run commands from `frontend/` so project linkage matches the app directory.

2. Create a Vercel token
   - Vercel Dashboard -> Settings -> Tokens -> Create.
   - Store it as GitHub secret `VERCEL_TOKEN`.

3. Get project identifiers
   - Recommended from `frontend/`:

```bash
vercel pull --yes --environment=production
```

- Read `.vercel/project.json`:

```json
{
  "orgId": "...",
  "projectId": "..."
}
```

- Add these as GitHub secrets:
  - `VERCEL_ORG_ID`
  - `VERCEL_PROJECT_ID`

4. Verify GitHub secrets exist
   - Repository -> Settings -> Secrets and variables -> Actions.
   - Required:
     - `VERCEL_TOKEN`
     - `VERCEL_ORG_ID`
     - `VERCEL_PROJECT_ID`

If any required secret is missing, deployment fails.

## Trigger Behavior

The workflows run on:

- `CI Frontend` runs on pull requests targeting `main` with changes in:
  - `frontend/**`
  - `.github/workflows/ci-frontend.yaml`
- `Unified Deployment` runs only by manual trigger (`workflow_dispatch`).

Important behavior:

- Pull requests run frontend validation (lint + build).
- Production deployment does not run automatically on push/merge to `main`.
- Production deployment requires a manual workflow run.

## What the Workflows Do

Both workflows run on `ubuntu-latest` with `frontend/` as the working directory.

1. `ci-frontend` (quality checks)
   - Checkout repository.
   - Setup Node.js from `frontend/package.json` (`24.x`).
   - `npm ci`
   - `npm run lint`
   - `npm run build`

2. `deploy-frontend` (production deployment)

- Runs as part of `Unified Deployment` when `deploy_target=frontend` or `deploy_target=both`.
- Checkout repository.
- Setup Node.js from `frontend/package.json` (`24.x`).
- Install latest Vercel CLI.
- Pull production config: `vercel pull --yes --environment=production`.
- Build prebuilt output: `vercel build --prod`.
- Deploy prebuilt output: `vercel deploy --prebuilt --prod`.

Concurrency is enabled:

- Group: `unified-deploy-${{ github.ref }}`
- Effect: only one unified deployment run per branch at a time (`cancel-in-progress: false`).

## Run Manually

1. Open GitHub -> Actions.
2. Select `Unified Deployment`.
3. Click `Run workflow` and choose branch `main`.
4. Set `deploy_target=frontend`.
5. Leave `backend_commit_sha` empty for frontend-only deploy.

## Troubleshooting

- `Error: No existing credentials found`
  - Verify `VERCEL_TOKEN` is present and valid.

- `Project not found` / `Organization not found`
  - Verify `VERCEL_PROJECT_ID` and `VERCEL_ORG_ID` values.

- Deployment did not run after merge
  - This is expected. Deployment is manual-only via `workflow_dispatch`.
  - Run `Unified Deployment` with `deploy_target=frontend` from the Actions tab.

- CI did not run on a pull request
  - Confirm target branch is `main`.
  - Confirm changed files matched CI workflow paths:
    - `frontend/**`
    - `.github/workflows/ci-frontend.yaml`

- A run was cancelled unexpectedly
  - This is usually expected because `cancel-in-progress: true` cancels superseded runs.

## Notes

- Deploy target is production only (`--prod`).
- Pull requests do not trigger production deployment.
- Deployment is manually triggered and should be run after CI is green.
