import { Scorecards } from '@/pages/Scorecards';
import { createFileRoute } from '@tanstack/react-router';

export const Route = createFileRoute('/(app)/scorecards/')({
    component: Scorecards,
    staticData: {
        breadcrumb: 'Scorecards',
    },
});
