import { useMutation, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { issueKeys } from '../types/issueKeys';
import { updateIssueType } from '../services/issueService';
import type { UpdateIssueType } from '../types/issue';
import { SUCCESS_MESSAGES, ERROR_MESSAGES } from '@/constants/messages';

interface UseQuickUpdateIssueTypeMutationParams {
    id: string;
    issueTypeId: string | null;
}

export const useQuickUpdateIssueType = () => {
    const queryClient = useQueryClient();

    const { mutateAsync, isPending } = useMutation({
        mutationFn: ({ id, issueTypeId }: UseQuickUpdateIssueTypeMutationParams) =>
            updateIssueType(id, { issueTypeId } satisfies UpdateIssueType),

        onSuccess: () => {
            toast.success(SUCCESS_MESSAGES.ISSUE.TYPE_UPDATED);
        },

        onError: () => {
            toast.error(ERROR_MESSAGES.ISSUE.UPDATE_TYPE_FAILED);
        },

        onSettled: () => {
            queryClient.invalidateQueries({
                queryKey: issueKeys.lists(),
            });
        },
    });

    return {
        updateIssueType: mutateAsync,
        isPending,
    };
};
