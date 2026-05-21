import { useMutation, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';

import { issueKeys } from '../types/issueKeys';
import { archiveIssue } from '../services/issueService';

import type { Issue } from '../types/issue';
import { SUCCESS_MESSAGES, ERROR_MESSAGES } from '@/constants/messages';

interface ArchiveIssueParams {
    id: string;
    isArchived: boolean;
}

export const useArchiveIssue = () => {
    const queryClient = useQueryClient();

    const mutation = useMutation({
        mutationFn: ({ id, isArchived }: ArchiveIssueParams) => archiveIssue(id, isArchived),

        onError: (_err, { isArchived }) => {
            toast.error(isArchived ? ERROR_MESSAGES.ISSUE.ARCHIVE_FAILED : ERROR_MESSAGES.ISSUE.UNARCHIVE_FAILED);
        },

        onSuccess: (_data, { isArchived }) => {
            toast.success(isArchived ? SUCCESS_MESSAGES.ISSUE.ARCHIVED : SUCCESS_MESSAGES.ISSUE.UNARCHIVED);
        },
        onSettled: () => {
            queryClient.invalidateQueries({
                queryKey: issueKeys.lists(),
            });
        },
    });

    const handleArchiveToggle = (issue: Issue) => {
        mutation.mutate({
            id: issue.id,
            isArchived: !issue.isArchived,
        });
    };

    return {
        handleArchiveToggle,
        isArchiving: mutation.isPending,
    };
};
