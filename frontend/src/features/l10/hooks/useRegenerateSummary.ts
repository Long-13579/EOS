import { useMutation, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { normalizeApiError } from '@/utils/apiErrorNormalizer';
import { l10MeetingKeys } from '../types/l10MeetingKeys';
import { regenerateSummary } from '../services/l10MeetingService';

export const useRegenerateSummary = (meetingId: string) => {
    const queryClient = useQueryClient();

    const { mutateAsync, isPending } = useMutation({
        mutationFn: () => regenerateSummary(meetingId),

        onSuccess: () => {
            toast.success('Summary regeneration started.');
        },
        onError: (error) => {
            const normalized = normalizeApiError(error);
            toast.error(normalized.message);
        },
        onSettled: () => {
            queryClient.invalidateQueries({
                queryKey: l10MeetingKeys.detail(meetingId),
            });
        },
    });

    return {
        regenerate: mutateAsync,
        isRegenerating: isPending,
    };
};
