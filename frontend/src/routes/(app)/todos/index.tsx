import { useEffect, useRef } from 'react';
import { createFileRoute, useNavigate } from '@tanstack/react-router';

import { TODOS_ROUTE_PATH } from '@/constants/routes';
import { useActiveTeamId } from '@/hooks/useActiveTeamId';
import { Todos } from '@/pages/Todos';
import { type TodoStatus } from '@/features/todos';

type TodosSearch = {
    status?: TodoStatus;
};

const TODO_STATUS_VALUES: ReadonlySet<TodoStatus> = new Set(['NOT_STARTED', 'IN_PROGRESS', 'COMPLETED']);

export const Route = createFileRoute(TODOS_ROUTE_PATH)({
    validateSearch: (search: Record<string, unknown>): TodosSearch => {
        const status = search.status;
        return {
            status: typeof status === 'string' && TODO_STATUS_VALUES.has(status as TodoStatus) ? (status as TodoStatus) : undefined,
        };
    },
    component: TodosWrapper,
    staticData: {
        breadcrumb: 'To-dos',
    },
});

function TodosWrapper() {
    const activeTeamId = useActiveTeamId();
    const navigate = useNavigate();
    const prevTeamId = useRef<string | undefined>(activeTeamId);

    useEffect(() => {
        if (prevTeamId.current !== undefined && prevTeamId.current !== activeTeamId) {
            navigate({
                to: '.',
                search: (prev: Record<string, unknown>) => ({ ...prev, status: undefined }),
                replace: true,
            });
        }
        prevTeamId.current = activeTeamId;
    }, [activeTeamId, navigate]);

    return <Todos key={activeTeamId} />;
}
