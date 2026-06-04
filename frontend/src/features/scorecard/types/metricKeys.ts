import type { GetMetricsParams, GetMyMetricsParams } from './metric';

export const metricKeys = {
    all: ['metrics'] as const,

    lists: () => [...metricKeys.all, 'list'] as const,
    list: (params: GetMetricsParams) => [...metricKeys.lists(), params] as const,

    myMetrics: () => [...metricKeys.all, 'my-metrics'] as const,
    myMetric: (params: GetMyMetricsParams) => [...metricKeys.myMetrics(), params] as const,

    details: (ids: string[]) => [...metricKeys.all, 'details', ids] as const,
    detail: (id: string) => [...metricKeys.all, 'detail', id] as const,

    deleteMetric: () => [...metricKeys.all, 'delete'] as const,
};
