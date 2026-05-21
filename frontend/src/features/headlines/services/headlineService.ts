import { apiGet, apiPost, apiPut, apiDelete, apiPatch } from '@/utils/apiRequest';
import type { GetHeadlinesParams, Headline, CreateHeadline, UpdateHeadline, ArchiveHeadline } from '../types/headline';
import type { PaginatedResponse } from '@/types/pagination';

export const getHeadlines = (params: GetHeadlinesParams): Promise<PaginatedResponse<Headline>> =>
    apiGet<PaginatedResponse<Headline>>('/headlines', { params });

export const createHeadline = (payload: CreateHeadline): Promise<Headline> => apiPost<Headline>('/headlines', payload);

export const updateHeadline = (id: string, payload: UpdateHeadline): Promise<Headline> => apiPut<Headline>(`/headlines/${id}`, payload);

export const archiveHeadline = (id: string, payload: ArchiveHeadline): Promise<Headline> => apiPatch<Headline>(`/headlines/${id}`, payload);

export const deleteHeadline = (id: string): Promise<void> => apiDelete<void>(`/headlines/${id}`);
