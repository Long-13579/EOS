import { useMutation, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { l10MeetingKeys } from '../types/l10MeetingKeys';
import { startL10Meeting } from '../services/l10MeetingService';

export const useStartL10Meeting = () => {
    const queryClient = useQueryClient();

    const { mutateAsync, isPending } = useMutation({
        mutationFn: (meetingId: string) => startL10Meeting(meetingId),

        onSuccess: () => {
            toast.success('L10 meeting started successfully!');
        },
        onSettled: () => {
            queryClient.invalidateQueries({
                queryKey: l10MeetingKeys.lists(),
            });
        },
    });

    return {
        startMeeting: mutateAsync,
        isStarting: isPending,
    };
};
