import { useMutation, useQueryClient } from '@tanstack/react-query';
import { issueKeys } from '../types/issueKeys';
import type { UpdateIssue } from '../types/issue';
import { updateIssue } from '../services/issueService';
import { SUCCESS_MESSAGES } from '@/constants/messages';
import { toast } from 'sonner';

interface UpdateIssueMutationParams {
    id: string;
    data: UpdateIssue;
}

export const useUpdateIssue = () => {
    const queryClient = useQueryClient();

    const { mutateAsync } = useMutation({
        mutationFn: ({ id, data }: UpdateIssueMutationParams) => updateIssue(id, data),

        onSuccess: (serverIssue, { id }) => {
            toast.success(SUCCESS_MESSAGES.ISSUE.UPDATED);
            queryClient.setQueryData(issueKeys.detail(id), serverIssue);
        },
        onSettled: () => {
            queryClient.invalidateQueries({
                queryKey: issueKeys.lists(),
            });
        },
    });

    return {
        updateIssue: mutateAsync,
    };
};
