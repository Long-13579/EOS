import { useMutation, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { normalizeApiError } from '@/utils/apiErrorNormalizer';
import { l10MeetingKeys } from '../types/l10MeetingKeys';
import { finishL10Meeting } from '../services/l10MeetingService';

export const useFinishL10Meeting = () => {
    const queryClient = useQueryClient();

    const { mutateAsync, isPending } = useMutation({
        mutationFn: (meetingId: string) => finishL10Meeting(meetingId),

        onSuccess: () => {
            toast.success('L10 meeting finished!');
        },
        onError: (error) => {
            const normalized = normalizeApiError(error);
            toast.error(normalized.message);
        },
        onSettled: () => {
            queryClient.invalidateQueries({
                queryKey: l10MeetingKeys.lists(),
            });
        },
    });

    return {
        finishMeeting: mutateAsync,
        isFinishing: isPending,
    };
};
