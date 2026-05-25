import { useMutation, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { l10MeetingKeys } from '../types/l10MeetingKeys';
import { updateL10Meeting } from '../services/l10MeetingService';
import type { UpdateL10MeetingPayload } from '../types/l10Meeting';

export const useUpdateL10Meeting = () => {
    const queryClient = useQueryClient();

    const { mutateAsync, isPending } = useMutation({
        mutationFn: ({ meetingId, payload }: { meetingId: string; payload: UpdateL10MeetingPayload }) => updateL10Meeting(meetingId, payload),

        onSuccess: () => {
            toast.success('L10 meeting updated successfully!');
        },
        onSettled: () => {
            queryClient.invalidateQueries({
                queryKey: l10MeetingKeys.lists(),
            });
        },
    });

    return {
        updateMeeting: mutateAsync,
        isUpdating: isPending,
    };
};
