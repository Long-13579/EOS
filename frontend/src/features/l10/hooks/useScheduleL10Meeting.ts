import { useMutation, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { l10MeetingKeys } from '../types/l10MeetingKeys';
import { scheduleL10Meeting } from '../services/l10MeetingService';
import type { ScheduleL10MeetingPayload } from '../types/l10Meeting';

export const useScheduleL10Meeting = () => {
    const queryClient = useQueryClient();

    const { mutateAsync, isPending } = useMutation({
        mutationFn: (payload: ScheduleL10MeetingPayload) => scheduleL10Meeting(payload),

        onSuccess: () => {
            toast.success('L10 meeting scheduled successfully!');
        },
        onSettled: () => {
            queryClient.invalidateQueries({
                queryKey: l10MeetingKeys.lists(),
            });
        },
    });

    return {
        scheduleMeeting: mutateAsync,
        isScheduling: isPending,
    };
};
