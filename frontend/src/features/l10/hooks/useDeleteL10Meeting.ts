import { useMutation, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { l10MeetingKeys } from '../types/l10MeetingKeys';
import { deleteL10Meeting } from '../services/l10MeetingService';

export const useDeleteL10Meeting = () => {
    const queryClient = useQueryClient();

    const { mutateAsync, isPending } = useMutation({
        mutationFn: (meetingId: string) => deleteL10Meeting(meetingId),

        onSuccess: () => {
            toast.success('L10 meeting deleted successfully!');
        },
        onSettled: () => {
            queryClient.invalidateQueries({
                queryKey: l10MeetingKeys.lists(),
            });
        },
    });

    return {
        deleteMeeting: mutateAsync,
        isDeleting: isPending,
    };
};
