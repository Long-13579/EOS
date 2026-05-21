import type { PaginationParams } from '@/types/pagination';

export const userKeys = {
    all: ['users'] as const,

    lists: () => [...userKeys.all, 'list'] as const,
    list: (params: PaginationParams) => [...userKeys.lists(), params] as const,

    details: () => [...userKeys.all, 'detail'] as const,
    detail: (id: string) => [...userKeys.details(), id] as const,

    byTeam: (teamId: string) => [...userKeys.all, 'team', teamId] as const,
};
