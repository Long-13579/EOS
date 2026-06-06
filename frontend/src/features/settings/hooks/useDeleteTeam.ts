import { useMutation, useQueryClient } from '@tanstack/react-query';
import { deleteTeam } from '@/features/settings/services/teamService';
import { teamKeys } from '@/utils/teamKeys';
import { userKeys } from '../types/userKeys';

export const useDeleteTeam = () => {
    const queryClient = useQueryClient();

    const { mutateAsync, isPending } = useMutation({
        mutationFn: (id: string) => deleteTeam(id),

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

    return { deleteTeam: mutateAsync, isDeleting: isPending };
};
