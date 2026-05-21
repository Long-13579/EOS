import { useMutation, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { headlineKeys } from '../types/headlineKeys';
import { updateHeadline } from '../services/headlineService';
import { SUCCESS_MESSAGES } from '@/constants/messages';
import type { UpdateHeadline } from '../types/headline';

type UseUpdateHeadlineParams = {
    onUpdated?: () => void;
};

interface UpdateHeadlineMutationParams {
    id: string;
    data: UpdateHeadline;
}

export const useUpdateHeadline = ({ onUpdated }: UseUpdateHeadlineParams = {}) => {
    const queryClient = useQueryClient();

    const { mutateAsync } = useMutation({
        mutationFn: ({ id, data }: UpdateHeadlineMutationParams) => updateHeadline(id, data),

        onSuccess: () => {
            toast.success(SUCCESS_MESSAGES.HEADLINE.UPDATED);
            onUpdated?.();
        },
        onSettled: () => {
            queryClient.invalidateQueries({
                queryKey: headlineKeys.lists(),
            });
        },
    });

    return {
        handleSubmit: mutateAsync,
    };
};
