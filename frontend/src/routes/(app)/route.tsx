import { createFileRoute, Outlet } from '@tanstack/react-router';
import { DashboardLayout } from '@/components/layouts/DashboardLayout';
import { useErrorHandler } from '@/hooks/useErrorHandler';
import { getSafeInternalPath, UserAuthActions } from '@/features/auth';
import { TooltipProvider } from '@/components/ui/tooltip';

interface AppSearchParams {
    error?: string;
}

export const Route = createFileRoute('/(app)')({
    beforeLoad: ({ location }) => UserAuthActions.requireAuth(getSafeInternalPath(location.href)),
    validateSearch: (search): AppSearchParams => ({
        error: search.error as string | undefined,
    }),
    component: AppLayoutComponent,
});

function AppLayoutComponent() {
    useErrorHandler('/(app)');

    return (
        <TooltipProvider>
            <DashboardLayout>
                <Outlet />
            </DashboardLayout>
        </TooltipProvider>
    );
}
