import { createFileRoute } from '@tanstack/react-router';
import { NotFoundPage } from '@/pages/NotFound';
import { UserAuthActions } from '@/features/auth';

export const Route = createFileRoute('/$')({
    beforeLoad: () => UserAuthActions.requireAuthWithoutRedirect(),
    component: NotFoundPage,
});
