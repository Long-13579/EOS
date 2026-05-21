import { useMutation, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { headlineKeys } from '../types/headlineKeys';
import { createHeadline } from '../services/headlineService';
import type { CreateHeadline } from '../types/headline';
import { SUCCESS_MESSAGES } from '@/constants/messages';

type UseCreateHeadlineParams = {
    activeTeamId?: string;
    onCreated?: () => void;
};

export const useCreateHeadline = ({ activeTeamId, onCreated }: UseCreateHeadlineParams) => {
    const queryClient = useQueryClient();

    const { mutateAsync } = useMutation({
        mutationFn: (payload: CreateHeadline) => createHeadline(payload),

        onSuccess: () => {
            toast.success(SUCCESS_MESSAGES.HEADLINE.CREATED);
            onCreated?.();
        },
        onSettled: () => {
            queryClient.invalidateQueries({
                queryKey: headlineKeys.lists(),
            });
        },
    });

    const handleSubmit = async (title: string) => {
        if (!activeTeamId) {
            return;
        }

        await mutateAsync({
            title,
            teamId: activeTeamId,
        });
    };

    return {
        handleSubmit,
    };
};
