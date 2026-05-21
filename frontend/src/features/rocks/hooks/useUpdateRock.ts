import { useMutation, useQueryClient } from '@tanstack/react-query';
import { updateRock } from '../services/rockService';
import type { UpdateRock } from '../types/rock';
import { rockKeys } from '../types/rockKeys';
import { ERROR_MESSAGES, SUCCESS_MESSAGES } from '@/constants/messages';
import { toast } from 'sonner';

interface UpdateRockMutationParams {
    id: string;
    data: UpdateRock;
}

export const useUpdateRock = () => {
    const queryClient = useQueryClient();

    const { mutateAsync } = useMutation({
        mutationFn: ({ id, data }: UpdateRockMutationParams) => updateRock(id, data),

        onSuccess: () => {
            toast.success(SUCCESS_MESSAGES.ROCK.UPDATED);
        },

        onError: () => {
            toast.error(ERROR_MESSAGES.ROCK.UPDATE_FAILED);
        },
        onSettled: () => {
            queryClient.invalidateQueries({ queryKey: rockKeys.lists() });
            queryClient.invalidateQueries({
                queryKey: rockKeys.myRocks(),
            });
        },
    });

    return {
        updateRock: mutateAsync,
    };
};
