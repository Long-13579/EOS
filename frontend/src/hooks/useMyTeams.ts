import { useQuery, keepPreviousData } from '@tanstack/react-query';
import { getMyTeams } from '@/services/myTeamsService';
import { teamKeys } from '@/utils/teamKeys';

export const useMyTeams = () => {
    return useQuery({
        queryKey: teamKeys.myTeams(),
        queryFn: () => getMyTeams(),

        placeholderData: keepPreviousData,
    });
};
