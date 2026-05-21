import type { GetHeadlinesParams } from './headline';

export const headlineKeys = {
    all: ['headlines'] as const,

    lists: () => [...headlineKeys.all, 'list'] as const,
    list: (params: GetHeadlinesParams) => [...headlineKeys.lists(), params] as const,

    details: () => [...headlineKeys.all, 'detail'] as const,
    detail: (id: string) => [...headlineKeys.details(), id] as const,

    deleteHeadline: () => [...headlineKeys.all, 'delete'] as const,
    archiveHeadline: () => [...headlineKeys.all, 'archive'] as const,
};
