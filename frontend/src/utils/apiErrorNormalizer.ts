import axios from 'axios';
import type { ApiErrorResponse, ApiFieldErrors } from '@/types/api';
import { ERROR_MESSAGES } from '@/constants/messages';

export type ErrorType = 'validation' | 'conflict' | 'unauthorized' | 'forbidden' | 'not_found' | 'server' | 'network' | 'unknown';

export type NormalizedApiError = {
    type: ErrorType;
    message: string;
    fieldErrors?: Record<string, string>;
    status?: number;
    originalError: unknown;
};

const parseDetails = (details: unknown): Record<string, string> | undefined => {
    if (!details || typeof details !== 'object') {
        return undefined;
    }

    const result: Record<string, string> = {};

    for (const [key, value] of Object.entries(details as Record<string, unknown>)) {
        if (Array.isArray(value)) {
            const first = value.find((item): item is string => typeof item === 'string');

            if (first) {
                result[key] = first;
            }
        } else if (typeof value === 'string') {
            result[key] = value;
        }
    }

    return Object.keys(result).length ? result : undefined;
};

export const normalizeApiError = (error: unknown): NormalizedApiError => {
    if (!axios.isAxiosError(error)) {
        return {
            type: 'unknown',
            message: (error as Error)?.message || ERROR_MESSAGES.COMMON.GENERIC_UNKNOWN,
            originalError: error,
        };
    }

    if (!error.response) {
        return {
            type: 'network',
            message: ERROR_MESSAGES.COMMON.NETWORK,
            originalError: error,
        };
    }

    const status = error.response.status;
    const data = error.response.data as ApiErrorResponse<ApiFieldErrors> | undefined;

    if (status === 409) {
        return {
            type: 'conflict',
            message: data?.message || ERROR_MESSAGES.COMMON.CONFLICT,
            fieldErrors: parseDetails(data?.details),
            status,
            originalError: error,
        };
    }

    if (status === 422 || (status === 400 && data?.details)) {
        return {
            type: 'validation',
            message: data?.message || ERROR_MESSAGES.COMMON.VALIDATION,
            fieldErrors: parseDetails(data?.details),
            status,
            originalError: error,
        };
    }

    if (status === 401) {
        return {
            type: 'unauthorized',
            message: data?.message || ERROR_MESSAGES.COMMON.UNAUTHORIZED,
            status,
            originalError: error,
        };
    }

    if (status === 403) {
        return {
            type: 'forbidden',
            message: data?.message || ERROR_MESSAGES.COMMON.FORBIDDEN,
            status,
            originalError: error,
        };
    }

    if (status === 404) {
        return {
            type: 'not_found',
            message: data?.message || ERROR_MESSAGES.COMMON.NOT_FOUND,
            status,
            originalError: error,
        };
    }

    if (status >= 500) {
        return {
            type: 'server',
            message: data?.message || ERROR_MESSAGES.COMMON.SERVER,
            status,
            originalError: error,
        };
    }

    return {
        type: 'unknown',
        message: data?.message || error.message || ERROR_MESSAGES.COMMON.UNKNOWN,
        status,
        originalError: error,
    };
};
