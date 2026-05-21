import { useQuery, keepPreviousData } from '@tanstack/react-query';
import { headlineKeys } from '../types/headlineKeys';
import { getHeadlines } from '../services/headlineService';
import type { GetHeadlinesParams } from '../types/headline';

export const useHeadlines = (params: GetHeadlinesParams) => {
    return useQuery({
        queryKey: headlineKeys.list(params),
        queryFn: () => getHeadlines(params),
        enabled: !!params.teamId,
        placeholderData: keepPreviousData,
    });
};
