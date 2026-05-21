import { useMutation, useQueryClient } from '@tanstack/react-query';
import { updateTeam } from '@/features/settings/services/teamService';
import { teamKeys } from '@/utils/teamKeys';
import type { UpdateTeam } from '@/types/team';

import { userKeys } from '../types/userKeys';

interface UpdateTeamMutationParams {
    id: string;
    data: UpdateTeam;
}

export const useUpdateTeam = () => {
    const queryClient = useQueryClient();

    const { mutateAsync } = useMutation({
        mutationFn: ({ id, data }: UpdateTeamMutationParams) => updateTeam(id, data),

        onSuccess: (serverData, { id }) => {
            queryClient.setQueryData(teamKeys.detail(id), serverData);
        },
        onSettled: () => {
            queryClient.invalidateQueries({
                queryKey: teamKeys.lists(),
            });

            queryClient.invalidateQueries({
                queryKey: teamKeys.myTeams(),
            });

            queryClient.invalidateQueries({
                queryKey: userKeys.lists(),
            });
        },
    });

    return {
        updateTeam: mutateAsync,
    };
};
