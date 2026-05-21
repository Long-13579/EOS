import { Rocks } from '@/pages/Rocks';
import { createFileRoute } from '@tanstack/react-router';

export const Route = createFileRoute('/(app)/rocks/')({
    component: Rocks,
    staticData: {
        breadcrumb: 'Rocks',
    },
});
