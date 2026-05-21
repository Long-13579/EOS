import type { PaginationParams } from '@/types/pagination';
import type { GetTodosParams } from '../types/todo';

export const todoKeys = {
    all: ['todos'] as const,

    lists: () => [...todoKeys.all, 'list'] as const,
    list: (params: GetTodosParams) => [...todoKeys.lists(), params] as const,

    details: () => [...todoKeys.all, 'detail'] as const,
    detail: (id: string) => [...todoKeys.details(), id] as const,

    deleteTodo: () => [...todoKeys.all, 'delete'] as const,
    archiveTodo: () => [...todoKeys.all, 'archive'] as const,

    myTodos: () => [...todoKeys.all, 'my-todos'] as const,
    myTodo: (params: PaginationParams) => [...todoKeys.myTodos(), params] as const,
};
