import { useQuery, keepPreviousData } from '@tanstack/react-query';

import type { PaginationParams } from '@/types/pagination';

import { getUsers } from '@/features/settings/services/userService';

import { userKeys } from '../types/userKeys';

export const useUsers = (params: PaginationParams) => {
    return useQuery({
        queryKey: userKeys.list(params),
        queryFn: () => getUsers(params),

        placeholderData: keepPreviousData,
    });
};
