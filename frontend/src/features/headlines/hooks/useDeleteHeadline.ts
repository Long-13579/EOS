import { useState } from 'react';
import { useMutation, useMutationState, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { ERROR_MESSAGES, SUCCESS_MESSAGES } from '@/constants/messages';
import { deleteHeadline } from '../services/headlineService';
import { headlineKeys } from '../types/headlineKeys';
import type { Headline } from '../types/headline';

export const useDeleteHeadline = () => {
    const queryClient = useQueryClient();
    const [isDeleteDialogOpen, setDeleteDialogOpen] = useState(false);
    const [deletingHeadline, setDeletingHeadline] = useState<Headline | null>(null);

    const { mutate } = useMutation({
        mutationKey: headlineKeys.deleteHeadline(),
        mutationFn: (id: string) => deleteHeadline(id),

        onSuccess: () => {
            toast.success(SUCCESS_MESSAGES.HEADLINE.DELETED);
        },

        onError: () => {
            toast.error(ERROR_MESSAGES.HEADLINE.DELETE_FAILED);
        },

        onSettled: () => {
            if (queryClient.isMutating({ mutationKey: headlineKeys.deleteHeadline() }) <= 1) {
                queryClient.invalidateQueries({
                    queryKey: headlineKeys.lists(),
                });
            }
        },
    });

    const handleOpenDelete = (headline: Headline) => {
        setDeletingHeadline(headline);
        setDeleteDialogOpen(true);
    };

    const handleConfirmDelete = () => {
        if (!deletingHeadline) {
            return;
        }
        mutate(deletingHeadline.id);
        setDeleteDialogOpen(false);
    };

    const pendingData = useMutationState({
        filters: { mutationKey: headlineKeys.deleteHeadline(), status: 'pending' },
        select: (mutation) => mutation.state.variables as string,
    });

    return {
        isDeleteDialogOpen,
        setDeleteDialogOpen,
        deletingHeadline,
        deletingHeadlineIds: pendingData,
        handleOpenDelete,
        handleConfirmDelete,
    };
};
