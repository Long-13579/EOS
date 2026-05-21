import { apiGet, apiPost, apiPut } from '@/utils/apiRequest';
import type { PaginatedResponse } from '@/types/pagination';
import type { CreateUser, User, TeamMember, UpdateUser } from '@/types/user';
import type { PaginationParams } from '@/types/pagination';

export const getUsers = (params: PaginationParams): Promise<PaginatedResponse<User>> => apiGet<PaginatedResponse<User>>('/users', { params });

export const createUser = ({ payload }: { payload: CreateUser }): Promise<User> => apiPost<User>('/users', payload);

export const updateUser = ({ id, payload }: { id: string; payload: UpdateUser }): Promise<User> => apiPut<User>(`/users/${id}`, payload);

export const getTeamMembers = (teamId: string): Promise<TeamMember[]> => apiGet<TeamMember[]>(`/teams/${teamId}/users`);
