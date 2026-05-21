import { createFileRoute } from '@tanstack/react-router';
import { Headlines } from '@/pages/Headlines';

export const Route = createFileRoute('/(app)/headlines/')({
    component: Headlines,
    staticData: {
        breadcrumb: 'Headlines',
    },
});
