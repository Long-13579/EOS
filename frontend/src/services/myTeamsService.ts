import { apiGet } from '@/utils/apiRequest';
import type { Team } from '@/types/team';

export const getMyTeams = (): Promise<Team[]> => apiGet<Team[]>('/users/me/teams');
