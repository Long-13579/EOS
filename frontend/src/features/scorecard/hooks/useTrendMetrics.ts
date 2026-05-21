import { useQuery } from '@tanstack/react-query';
import { getTrendMetrics } from '../services/metricService';
import { trendKeys } from '../types/trendKeys';

export const useTrendMetrics = (teamId: string) => {
    return useQuery({
        queryKey: trendKeys.list(teamId),
        queryFn: () => getTrendMetrics(teamId),
        enabled: !!teamId,
    });
};
