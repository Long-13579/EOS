import { useMutation, useQueryClient } from '@tanstack/react-query';

import type { UpdateUser } from '@/types/user';

import { useUserStore } from '@/stores/useUserStore';

import { updateUser } from '@/features/settings/services/userService';
import { userKeys } from '../types/userKeys';

import { teamKeys } from '@/utils/teamKeys';

export const useUpdateUser = () => {
    const queryClient = useQueryClient();
    const currentUser = useUserStore((state) => state.user);
    const setUser = useUserStore((state) => state.setUser);

    const { mutateAsync, isPending } = useMutation({
        mutationKey: ['users'],
        mutationFn: ({ id, payload }: { id: string; payload: UpdateUser }) => updateUser({ id, payload }),

        onSuccess: (data, variables) => {
            if (currentUser?.id === variables.id && data) {
                setUser(data);
                queryClient.invalidateQueries({ queryKey: teamKeys.myTeams() });
            }
        },
        onSettled: () => {
            queryClient.invalidateQueries({ queryKey: userKeys.lists() });
        },
    });

    return { isUpdatingUser: isPending, updateUser: mutateAsync };
};
