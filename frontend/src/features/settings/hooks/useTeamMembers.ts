import { useQuery } from '@tanstack/react-query';
import { getTeamMembers } from '../services/userService';
import { userKeys } from '../types/userKeys';

export const useTeamMembers = (teamId?: string) =>
    useQuery({
        queryKey: userKeys.byTeam(teamId ?? ''),
        queryFn: () => getTeamMembers(teamId!),
        enabled: !!teamId,
    });
