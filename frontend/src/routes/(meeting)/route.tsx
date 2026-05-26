import { createFileRoute, Outlet } from '@tanstack/react-router';
import { TooltipProvider } from '@/components/ui/tooltip';
import { getSafeInternalPath, UserAuthActions } from '@/features/auth';

export const Route = createFileRoute('/(meeting)')({
    beforeLoad: ({ location }) => UserAuthActions.requireAuth(getSafeInternalPath(location.href)),
    component: () => (
        <TooltipProvider>
            <Outlet />
        </TooltipProvider>
    ),
});
