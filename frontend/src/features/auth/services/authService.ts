import { apiPost } from '@/utils/apiRequest';
import type { LoginGoogleRequest } from '../types/auth';

export const loginWithGoogle = (payload: LoginGoogleRequest): Promise<void> => apiPost<void>('/auth/google', payload, { skipAuth: true });

export const refreshToken = (): Promise<void> => apiPost<void>('/auth/refresh', null, { skipAuth: true });

export const logout = (): Promise<void> => apiPost<void>('/auth/logout', null, { skipAuth: true });
