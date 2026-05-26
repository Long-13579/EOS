import { useMutation, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { normalizeApiError } from '@/utils/apiErrorNormalizer';
import { l10MeetingKeys } from '../types/l10MeetingKeys';
import { upsertRatings } from '../services/l10MeetingService';
import type { UpsertRatingsPayload } from '../types/l10Meeting';

export const useUpsertRatings = () => {
    const queryClient = useQueryClient();

    const { mutateAsync, isPending } = useMutation({
        mutationFn: ({ meetingId, payload }: { meetingId: string; payload: UpsertRatingsPayload }) =>
            upsertRatings(meetingId, payload),

        onSuccess: () => {
            toast.success('Ratings saved.');
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
        saveRatings: mutateAsync,
        isSavingRatings: isPending,
    };
};
