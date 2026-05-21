import { useQuery, keepPreviousData } from '@tanstack/react-query';
import type { PaginationParams } from '@/types/pagination';
import { getTeams } from '../services/teamService';
import { teamKeys } from '@/utils/teamKeys';

export const useTeams = (params?: PaginationParams) => {
    const queryKey = params ? teamKeys.list(params) : teamKeys.lists();

    return useQuery({
        queryKey,
        queryFn: () => getTeams(params),

        placeholderData: keepPreviousData,
    });
};
