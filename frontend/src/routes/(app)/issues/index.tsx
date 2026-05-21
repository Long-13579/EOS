import { useActiveTeamId } from '@/hooks/useActiveTeamId';
import { Issues } from '@/pages/Issues';
import { createFileRoute } from '@tanstack/react-router';

function IssuesRoute() {
    const activeTeamId = useActiveTeamId();

    return <Issues key={activeTeamId} />;
}

export const Route = createFileRoute('/(app)/issues/')({
    component: IssuesRoute,
    staticData: {
        breadcrumb: 'Issues',
    },
});
