import { useMutation } from '@tanstack/react-query';
import { createRock } from '../services/rockService';
import { toast } from 'sonner';
import type { CreateRock } from '../types/rock';
import { ERROR_MESSAGES, SUCCESS_MESSAGES } from '@/constants/messages';
import { rockKeys } from '../types/rockKeys';
import { useQueryClient } from '@tanstack/react-query';

export function useCreateRock() {
    const queryClient = useQueryClient();

    const { mutateAsync } = useMutation({
        mutationFn: (payload: CreateRock) => createRock(payload),

        onError: () => {
            toast.error(ERROR_MESSAGES.ROCK.CREATE_FAILED);
        },

        onSuccess: () => {
            toast.success(SUCCESS_MESSAGES.ROCK.CREATED);
        },
        onSettled: () => {
            queryClient.invalidateQueries({
                queryKey: rockKeys.lists(),
            });

            // Invalidate years query to ensure the new year is included in the list if it wasn't already
            queryClient.invalidateQueries({
                queryKey: rockKeys.years(),
            });

            queryClient.invalidateQueries({
                queryKey: rockKeys.myRocks(),
            });
        },
    });

    return {
        createRock: mutateAsync,
    };
}
