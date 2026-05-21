import { useQuery } from '@tanstack/react-query';
import { metricKeys } from '../types/metricKeys';
import { getMyMetrics } from '../services/metricService';
import { mapMetric } from '../mappers/mapMetric';
import type { GetMyMetricsParams } from '../types/metric';

export const useMyMetrics = (params: GetMyMetricsParams) => {
    return useQuery({
        queryKey: metricKeys.myMetric(params),
        queryFn: () => getMyMetrics(params).then((data) => data.map(mapMetric)),
        enabled: !!params.weekId,
    });
};
