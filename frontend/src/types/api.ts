export interface ApiErrorResponse<T = unknown> {
    code?: string;
    message: string;
    details?: T;
}

export type ApiFieldErrors = Record<string, string | string[]>;
