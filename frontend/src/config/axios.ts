import axios, { HttpStatusCode, type AxiosRequestConfig } from 'axios';
import { env } from './env';

export interface AppRequestConfig extends AxiosRequestConfig {
    skipAuth?: boolean;
    _retry?: boolean;
}

export const instance = axios.create({
    withCredentials: true,
    baseURL: env.API_BASE_URL,
    headers: {
        'Content-Type': 'application/json',
    },
});

let refreshHandler: (() => Promise<void>) | null = null;
let onAuthFailure: (() => Promise<void>) | null = null;

export function registerAuthHandlers(handlers: { refresh: () => Promise<void>; onAuthFailure: () => Promise<void> }) {
    refreshHandler = handlers.refresh;
    onAuthFailure = handlers.onAuthFailure;
}

/**
 * Single-flight refresh state
 */
let refreshPromise: Promise<void> | null = null;

instance.interceptors.response.use(
    (response) => response,
    async (error) => {
        const originalRequest = error.config as AppRequestConfig | undefined;

        if (!error.response || !originalRequest) {
            return Promise.reject(error);
        }

        if (originalRequest.skipAuth) {
            return Promise.reject(error);
        }

        const isUnauthorized = error.response.status === HttpStatusCode.Unauthorized;

        if (isUnauthorized && !originalRequest._retry) {
            originalRequest._retry = true;

            try {
                if (!refreshPromise) {
                    if (!refreshHandler) {
                        return Promise.reject(error);
                    }

                    refreshPromise = refreshHandler()
                        .catch(async (refreshError) => {
                            await onAuthFailure?.();
                            return Promise.reject(refreshError);
                        })
                        .finally(() => {
                            refreshPromise = null;
                        });
                }

                await refreshPromise;

                return instance(originalRequest);
            } catch (error) {
                return Promise.reject(error);
            }
        }

        return Promise.reject(error);
    },
);
