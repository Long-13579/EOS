import type { PaginationParams } from '@/types/pagination';

export const teamKeys = {
    all: ['teams'] as const,

    lists: () => [...teamKeys.all, 'list'] as const,
    list: (params: PaginationParams) => [...teamKeys.lists(), params] as const,

    details: () => [...teamKeys.all, 'detail'] as const,
    detail: (id: string) => [...teamKeys.details(), id] as const,

    myTeams: () => [...teamKeys.all, 'myTeams'] as const,
};
