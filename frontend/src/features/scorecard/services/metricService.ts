import { apiDelete, apiGet, apiPatch, apiPost, apiPut } from '@/utils/apiRequest';
import type {
    Metric,
    CreateMetric,
    GetMetricsParams,
    GetMyMetricsParams,
    MetricResponseItem,
    UpdateMetricValue,
    UpdateMetric,
} from '../types/metric';
import type { TrendsTabMetricListResponse } from '../types/trends';

export const getMetrics = (params: GetMetricsParams): Promise<MetricResponseItem[]> => apiGet('/metrics', { params });

export const getMyMetrics = (params: GetMyMetricsParams): Promise<MetricResponseItem[]> => apiGet('/metrics/me', { params });

export const createMetric = (payload: CreateMetric): Promise<MetricResponseItem> => apiPost<MetricResponseItem>('/metrics', payload);

export const updateMetricValue = (data: UpdateMetricValue): Promise<Metric> => apiPut<Metric>(`/metric-values`, data);

export const updateMetric = (id: string, payload: UpdateMetric): Promise<MetricResponseItem> => apiPut<MetricResponseItem>(`/metrics/${id}`, payload);

export const getTrendMetrics = (teamId: string): Promise<TrendsTabMetricListResponse> =>
    apiGet('/metrics/trends', {
        params: { teamId },
    });

export const deleteMetric = (id: string): Promise<void> => apiDelete<void>('/metrics/' + id);

export const archiveMetric = (id: string, isArchived: boolean): Promise<MetricResponseItem> =>
    apiPatch<MetricResponseItem>(`/metrics/${id}`, { isArchived });
