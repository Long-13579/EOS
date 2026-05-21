import { useQuery } from '@tanstack/react-query';
import { getMyRocks } from '../services/rockService';
import { rockKeys } from '../types/rockKeys';
import type { GetPersonalRocksParams } from '../types/rock';

export const useMyRocks = (params: GetPersonalRocksParams) => {
    return useQuery({
        queryKey: rockKeys.myRock(params),
        queryFn: () => getMyRocks(params),
        enabled: !!params.yearId && !!params.quarterId,
    });
};
