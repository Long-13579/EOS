import { useQuery, keepPreviousData } from '@tanstack/react-query';
import { getRocks } from '../services/rockService';
import { rockKeys } from '../types/rockKeys';
import type { GetRocksParams } from '../types/rock';

export const useRocks = (params: GetRocksParams) => {
    return useQuery({
        queryKey: rockKeys.list(params),
        queryFn: () => getRocks(params),
        enabled: !!params.teamId && !!params.yearId && !!params.quarterId,
        placeholderData: keepPreviousData,
    });
};
