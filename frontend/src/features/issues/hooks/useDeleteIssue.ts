import { useState } from 'react';
import { useMutation, useMutationState, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { ERROR_MESSAGES, SUCCESS_MESSAGES } from '@/constants/messages';
import type { Issue } from '../types/issue';
import { deleteIssue } from '../services/issueService';
import { issueKeys } from '../types/issueKeys';

export function useDeleteIssue() {
    const queryClient = useQueryClient();
    const [isDeleteDialogOpen, setDeleteDialogOpen] = useState(false);
    const [deletingIssue, setDeletingIssue] = useState<Issue | null>(null);
    const { mutate } = useMutation({
        mutationKey: issueKeys.deleteIssue(),
        mutationFn: (id: string) => deleteIssue(id),

        onSuccess: () => {
            toast.success(SUCCESS_MESSAGES.ISSUE.DELETED);
        },

        onError: () => {
            toast.error(ERROR_MESSAGES.ISSUE.DELETE_FAILED);
        },

        onSettled: () => {
            // Only invalidate if there are no more PENDING deletes
            if (queryClient.isMutating({ mutationKey: issueKeys.deleteIssue() }) <= 1) {
                queryClient.invalidateQueries({
                    queryKey: issueKeys.lists(),
                });
            }
        },
    });

    const pendingData = useMutationState({
        filters: { mutationKey: issueKeys.deleteIssue(), status: 'pending' },
        select: (mutation) => mutation.state.variables as string,
    });

    const openDelete = (issue: Issue) => {
        setDeletingIssue(issue);
        setDeleteDialogOpen(true);
    };

    const handleConfirmDelete = () => {
        if (!deletingIssue) {
            return;
        }

        const deletedIssueId = deletingIssue.id;

        mutate(deletedIssueId);
        setDeleteDialogOpen(false);

        return deletedIssueId;
    };

    return {
        isDeleteDialogOpen,
        setDeleteDialogOpen,
        deletingIssue,
        deletingIssueIds: pendingData,
        openDelete,
        handleConfirmDelete,
    };
}
