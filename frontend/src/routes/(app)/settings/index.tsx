import { createFileRoute } from '@tanstack/react-router';
import { Settings } from '@/pages/Settings';
import { getSafeInternalPath, UserAuthActions } from '@/features/auth';

export const Route = createFileRoute('/(app)/settings/')({
    component: Settings,
    staticData: {
        breadcrumb: 'Settings',
    },
    beforeLoad: ({ location }) => UserAuthActions.requireAdmin(getSafeInternalPath(location.href)),
});
