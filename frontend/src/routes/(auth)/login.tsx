import { createFileRoute } from '@tanstack/react-router';

import { useErrorHandler } from '@/hooks/useErrorHandler';
import LoginPage from '@/pages/Login';
import { UserAuthActions } from '@/features/auth';

type LoginSearch = {
    redirect?: string;
    error?: string;
};

export const Route = createFileRoute('/(auth)/login')({
    beforeLoad: async () => {
        await UserAuthActions.redirectIfAuthenticated();
    },
    validateSearch: (search: Record<string, unknown>): LoginSearch => {
        return {
            redirect: search.redirect as string | undefined,
            error: search.error as string | undefined,
        };
    },
    component: LoginPageComponent,
});

function LoginPageComponent() {
    useErrorHandler('/(auth)/login');
    return <LoginPage />;
}
