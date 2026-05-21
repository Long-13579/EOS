import { useQuery } from '@tanstack/react-query';
import { getMyTodos } from '../services/todoService';
import { todoKeys } from '../types/todoKeys';
import type { PaginationParams } from '@/types/pagination';

export const useMyTodos = (params: PaginationParams) => {
    return useQuery({
        queryKey: todoKeys.myTodo(params),
        queryFn: () => getMyTodos(params),
    });
};
