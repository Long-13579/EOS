import { apiDelete, apiGet, apiPatch, apiPost, apiPut } from '@/utils/apiRequest';
import type { Year, Quarter, UpdateRockStatus, Rock, UpdateRock, GetPersonalRocksParams, PersonalRockListResponse } from '../types/rock';

import type { GetRocksParams, GetRocksResponse, CreateRock } from '../types/rock';

export const getRocks = (params: GetRocksParams) => apiGet<GetRocksResponse>('/rocks', { params });

export const getYears = (): Promise<Year[]> => apiGet<Year[]>('/years');

export const getQuarters = (): Promise<Quarter[]> => apiGet<Quarter[]>('/quarters');

export const createRock = (payload: CreateRock): Promise<Rock> => apiPost<Rock>('/rocks', payload);

export const updateRockStatus = (id: string, data: UpdateRockStatus) => apiPatch(`/rocks/${id}/status`, data);

export const archiveRock = (id: string, isArchived: boolean): Promise<Rock> => apiPatch<Rock>(`/rocks/${id}`, { isArchived });

export const updateRock = (id: string, payload: UpdateRock): Promise<Rock> => apiPut<Rock>(`/rocks/${id}`, payload);

export const deleteRock = (id: string): Promise<void> => apiDelete<void>(`/rocks/${id}`);

export const getMyRocks = (params: GetPersonalRocksParams): Promise<PersonalRockListResponse> =>
    apiGet<PersonalRockListResponse>('/rocks/me', { params });
