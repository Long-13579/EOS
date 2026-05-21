export const trendKeys = {
    all: ['trend-metrics'] as const,

    lists: () => [...trendKeys.all, 'list'] as const,
    list: (teamId: string) => [...trendKeys.lists(), teamId] as const,
};
