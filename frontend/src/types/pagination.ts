export interface Pagination {
    page: number;
    limit: number;
    totalItems: number;
    totalPages: number;
    hasNext: boolean;
    hasPrev: boolean;
}

export interface PaginatedResponse<T> {
    data: T[];
    pagination: Pagination;
}

export type PaginationParams = Pick<Pagination, 'page' | 'limit'>;

export const DEFAULT_LIMIT = 10;
