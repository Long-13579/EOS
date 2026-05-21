import { createFileRoute } from '@tanstack/react-router';
import { Vto } from '@/pages/Vto';

export const Route = createFileRoute('/(app)/vto/')({
    component: Vto,
    staticData: {
        breadcrumb: 'VTO',
    },
});
