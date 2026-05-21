import type { GetPersonalRocksParams, GetRocksParams } from './rock';

export const rockKeys = {
    all: ['rocks'] as const,

    years: () => [...rockKeys.all, 'years'] as const,

    quarters: () => [...rockKeys.all, 'quarters'] as const,

    lists: () => [...rockKeys.all, 'list'] as const,
    list: (params: GetRocksParams) => [...rockKeys.lists(), params] as const,

    details: () => [...rockKeys.all, 'detail'] as const,
    detail: (id: string) => [...rockKeys.details(), id] as const,

    myRocks: () => [...rockKeys.all, 'my-rocks'] as const,
    myRock: (params: GetPersonalRocksParams) => [...rockKeys.myRocks(), params] as const,
};
