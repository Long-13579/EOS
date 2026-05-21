import { useMutation, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { rockKeys } from '../types/rockKeys';
import { updateRockStatus } from '../services/rockService';
import type { Rock, RockStatus, GetRocksResponse } from '../types/rock';
import { SUCCESS_MESSAGES, ERROR_MESSAGES } from '@/constants/messages';

interface UseUpdateRockStatusParams {
    onUpdated?: () => void;
}

interface UseUpdateRockStatusMutationParams {
    id: string;
    status: RockStatus;
}

export const useUpdateRockStatus = ({ onUpdated }: UseUpdateRockStatusParams = {}) => {
    const queryClient = useQueryClient();

    const { mutateAsync, isPending, variables } = useMutation({
        mutationFn: ({ id, status }: UseUpdateRockStatusMutationParams) => updateRockStatus(id, { status }),

        onMutate: async ({ id, status }) => {
            await queryClient.cancelQueries({
                queryKey: rockKeys.lists(),
            });

            const previous = queryClient.getQueriesData<GetRocksResponse>({
                queryKey: rockKeys.lists(),
            });

            queryClient.setQueriesData<GetRocksResponse>({ queryKey: rockKeys.lists() }, (old) => {
                if (!old) {
                    return old;
                }

                const updateList = (rocks: Rock[]) => rocks.map((r) => (r.id === id ? { ...r, status } : r));

                return {
                    ...old,
                    companyRocks: updateList(old.companyRocks || []),
                    departmentRocks: updateList(old.departmentRocks),
                    individualRocks: updateList(old.individualRocks),
                };
            });

            return { previous };
        },

        onError: (_err, _vars, context) => {
            context?.previous?.forEach(([key, data]) => {
                queryClient.setQueryData(key, data);
            });
            toast.error(ERROR_MESSAGES.ROCK.UPDATE_STATUS_FAILED);
        },

        onSuccess: () => {
            toast.success(SUCCESS_MESSAGES.ROCK.STATUS_UPDATED);
            onUpdated?.();
        },

        onSettled: () => {
            queryClient.invalidateQueries({
                queryKey: rockKeys.lists(),
            });
            queryClient.invalidateQueries({
                queryKey: rockKeys.myRocks(),
            });
        },
    });

    return {
        updateStatus: mutateAsync,
        isPending,
        pendingStatus: variables?.status ?? null,
    };
};
