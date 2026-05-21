import { apiGet, apiPut, apiPost } from '@/utils/apiRequest';
import type { PaginatedResponse, PaginationParams } from '@/types/pagination';
import type { Team, CreateTeam, UpdateTeam } from '@/types/team';

export const getTeams = (params?: PaginationParams): Promise<PaginatedResponse<Team>> => apiGet<PaginatedResponse<Team>>('/teams', { params });

export const createTeam = (payload: CreateTeam): Promise<Team> => apiPost<Team>('/teams', payload);

export const updateTeam = (id: string, data: UpdateTeam): Promise<Team> => apiPut<Team>(`/teams/${id}`, data);
