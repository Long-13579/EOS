import { useSearch, useNavigate } from '@tanstack/react-router';

import { TODOS_ROUTE_PATH } from '@/constants/routes';
import { ALL_TODO_STATUSES, type TodoStatus } from '../types/todo';

export function useFilterTodo(onSuccess?: () => void) {
    const navigate = useNavigate();
    const { status } = useSearch({ from: TODOS_ROUTE_PATH });
    const filterStatus: TodoStatus | typeof ALL_TODO_STATUSES = status ?? ALL_TODO_STATUSES;

    const handleFilterChange = (status: TodoStatus | typeof ALL_TODO_STATUSES) => {
        navigate({
            to: '.',
            search: (prev: Record<string, unknown>) => ({
                ...prev,
                status: status === ALL_TODO_STATUSES ? undefined : status,
            }),
        });
        onSuccess?.();
    };
    return { filterStatus, handleFilterChange };
}
