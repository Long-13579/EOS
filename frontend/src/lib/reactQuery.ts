import { useTeamStore } from '@/stores/useTeamStore';
import { MutationCache, QueryClient } from '@tanstack/react-query';
import type { TeamMember, User } from '@/types/user';
import { userKeys } from '@/features/settings/types/userKeys';
import axios from 'axios';

export const queryClient = new QueryClient({
    mutationCache: new MutationCache({
        onSuccess: (data: unknown, variables: unknown, _context, mutation) => {
            if (mutation.options.mutationKey?.includes('users')) {
                const activeTeam = useTeamStore.getState().activeTeam;
                if (!activeTeam) {
                    return;
                }

                const userId = (variables as { id?: string })?.id;
                const teamMembersKey = userKeys.byTeam(activeTeam.id);

                const cachedData = queryClient.getQueryData<TeamMember[]>(teamMembersKey);
                const memberList = Array.isArray(cachedData) ? cachedData : [];
                const wasInTeam = memberList.some((u: TeamMember) => u.id === userId);

                const isInTeamNow = (data as User)?.teams?.some((t) => t.id === activeTeam.id);

                if (wasInTeam || isInTeamNow) {
                    queryClient.invalidateQueries({ queryKey: teamMembersKey });
                }
            }
        },
    }),
    defaultOptions: {
        queries: {
            staleTime: 5 * 60 * 1000,
            retry: (failureCount, error) => {
                // Do not retry on 401 Unauthorized errors
                if (axios.isAxiosError(error)) {
                    const status = error.response?.status;
                    if (status === 401) {
                        return false;
                    }
                }

                // Otherwise, retry the request once (when failureCount is 0)
                return failureCount < 1;
            },
        },
    },
});
