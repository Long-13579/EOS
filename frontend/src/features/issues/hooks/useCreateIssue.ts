import { useMutation, useQueryClient } from '@tanstack/react-query';
import { issueKeys } from '../types/issueKeys';
import type { CreateIssue } from '../types/issue';
import { createIssue } from '../services/issueService';
import { SUCCESS_MESSAGES } from '@/constants/messages';
import { toast } from 'sonner';

export const useCreateIssue = () => {
    const queryClient = useQueryClient();

    const { mutateAsync } = useMutation({
        mutationFn: (payload: CreateIssue) => createIssue(payload),

        onSuccess: () => {
            toast.success(SUCCESS_MESSAGES.ISSUE.CREATED);
        },
        onSettled: () => {
            queryClient.invalidateQueries({
                queryKey: issueKeys.lists(),
            });
        },
    });

    return {
        createIssue: mutateAsync,
    };
};
