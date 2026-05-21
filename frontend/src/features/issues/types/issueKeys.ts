import type { GetIssuesParams } from './issue';

export const issueKeys = {
    all: ['issues'] as const,

    lists: () => [...issueKeys.all, 'list'] as const,
    list: (params: GetIssuesParams) => [...issueKeys.lists(), params] as const,

    details: () => [...issueKeys.all, 'detail'] as const,
    detail: (id: string) => [...issueKeys.details(), id] as const,

    issueTypes: () => [...issueKeys.all, 'types'] as const,

    deleteIssue: () => [...issueKeys.all, 'deleteIssue'] as const,
};
