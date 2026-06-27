import { useMutation, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';

import { userKeys } from '../types/userKeys';
import { deactivateUser, activateUser } from '../services/userService';
import { normalizeApiError } from '@/utils/apiErrorNormalizer';

import { SUCCESS_MESSAGES, ERROR_MESSAGES } from '@/constants/messages';

import type { User } from '@/types/user';

interface ToggleUserStatusParams {
    user: User;
    activate: boolean;
}

export const useToggleUserStatus = () => {
    const queryClient = useQueryClient();

    const mutation = useMutation({
        mutationFn: ({ user, activate }: ToggleUserStatusParams) =>
            activate ? activateUser(user.id) : deactivateUser(user.id),

        onError: (err, { activate, user }) => {
            const normalized = normalizeApiError(err);

            if (normalized.type === 'conflict' && normalized.fieldErrors?.deactivation) {
                toast.error(normalized.fieldErrors.deactivation, {
                    duration: 8000,
                });
            } else if (normalized.type === 'forbidden') {
                toast.error(normalized.message);
            } else {
                toast.error(
                    activate
                        ? ERROR_MESSAGES.USER.ACTIVATE_FAILED
                        : ERROR_MESSAGES.USER.DEACTIVATE_FAILED
                );
            }
        },

        onSuccess: (_data, { activate, user }) => {
            toast.success(
                activate
                    ? SUCCESS_MESSAGES.USER.ACTIVATED
                    : SUCCESS_MESSAGES.USER.DEACTIVATED
            );
        },

        onSettled: () => {
            queryClient.invalidateQueries({
                queryKey: userKeys.lists(),
            });
        },
    });

    const handleToggleStatus = (user: User) => {
        mutation.mutate({ user, activate: !user.isActive });
    };

    return {
        handleToggleStatus,
        isTogglingStatus: mutation.isPending,
    };
};
