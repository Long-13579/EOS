import { apiGet } from '@/utils/apiRequest';
import type { CurrentUser } from '@/types/user';

export const getCurrentUserInfo = (): Promise<CurrentUser> => apiGet<CurrentUser>('/users/me');
