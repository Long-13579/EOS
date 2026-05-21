import { useMutation, useQueryClient } from '@tanstack/react-query';
import { createTeam } from '@/features/settings/services/teamService';
import type { CreateTeam } from '@/types/team';
import { teamKeys } from '@/utils/teamKeys';

export const useCreateTeam = () => {
    const queryClient = useQueryClient();

    const { mutateAsync, isPending } = useMutation({
        mutationFn: (payload: CreateTeam) => createTeam(payload),

        onSettled: () => {
            void queryClient.invalidateQueries({
                queryKey: teamKeys.lists(),
            });
            void queryClient.invalidateQueries({
                queryKey: teamKeys.myTeams(),
            });
        },
    });

    return { createTeam: mutateAsync, isCreating: isPending };
};
