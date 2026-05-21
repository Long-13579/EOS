import { useQuery } from '@tanstack/react-query';
import { metricKeys } from '../types/metricKeys';
import { getMetrics } from '../services/metricService';
import { mapMetric } from '../mappers/mapMetric';
import type { GetMetricsParams } from '../types/metric';

export const useMetrics = (params: GetMetricsParams) => {
    return useQuery({
        queryKey: metricKeys.list(params),
        queryFn: () => getMetrics(params).then((data) => data.map(mapMetric)),
        enabled: !!params.teamId && !!params.weekId,
    });
};
