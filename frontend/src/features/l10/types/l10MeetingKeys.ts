import type { GetL10MeetingsParams } from './l10Meeting';

export const l10MeetingKeys = {
    all: ['l10-meetings'] as const,

    lists: () => [...l10MeetingKeys.all, 'list'] as const,
    list: (params: GetL10MeetingsParams) => [...l10MeetingKeys.lists(), params] as const,

    details: () => [...l10MeetingKeys.all, 'detail'] as const,
    detail: (id: string) => [...l10MeetingKeys.details(), id] as const,
};
