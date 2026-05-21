import { L10Meetings } from '@/pages/L10Meetings';
import { createFileRoute } from '@tanstack/react-router';

export const Route = createFileRoute('/(app)/l10-meetings/')({
    component: L10Meetings,
    staticData: {
        breadcrumb: 'L10 Meetings',
    },
});
