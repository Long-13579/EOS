import { useQuery, keepPreviousData } from '@tanstack/react-query';
import { todoKeys } from '../types/todoKeys';
import { getTodos } from '../services/todoService';
import type { GetTodosParams } from '../types/todo';

export const useTodos = (params: GetTodosParams) => {
    return useQuery({
        queryKey: todoKeys.list(params),
        queryFn: () => getTodos(params),
        enabled: !!params.teamId,
        placeholderData: keepPreviousData,
    });
};
