import { useMutation, useQueryClient } from '@tanstack/react-query';
import type { CreateUser } from '@/types/user';
import { createUser } from '@/features/settings/services/userService';
import { userKeys } from '../types/userKeys';

export const useCreateUser = () => {
    const queryClient = useQueryClient();
    const { mutateAsync, isPending, isError } = useMutation({
        mutationFn: (payload: CreateUser) => createUser({ payload }),
        onSettled: () => {
            queryClient.invalidateQueries({ queryKey: userKeys.all });
        },
    });
    return { isCreatingUser: isPending, createUser: mutateAsync, createUserError: isError };
};
