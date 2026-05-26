import { useMutation, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { normalizeApiError } from '@/utils/apiErrorNormalizer';
import { l10MeetingKeys } from '../types/l10MeetingKeys';
import { updateConclude } from '../services/l10MeetingService';
import type { UpdateConcludePayload } from '../types/l10Meeting';

export const useUpdateConclude = () => {
    const queryClient = useQueryClient();

    const { mutateAsync, isPending } = useMutation({
        mutationFn: ({ meetingId, payload }: { meetingId: string; payload: UpdateConcludePayload }) =>
            updateConclude(meetingId, payload),

        onSuccess: () => {
            toast.success('Conclude notes saved.');
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
        saveConclude: mutateAsync,
        isSaving: isPending,
    };
};
