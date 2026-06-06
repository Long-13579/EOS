import { useState } from 'react';
import { useMutation, useMutationState, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { ERROR_MESSAGES, SUCCESS_MESSAGES } from '@/constants/messages';
import type { Rock } from '../types/rock';
import { deleteRock } from '../services/rockService';
import { rockKeys } from '../types/rockKeys';

export function useDeleteRock() {
    const queryClient = useQueryClient();
    const [isDeleteDialogOpen, setDeleteDialogOpen] = useState(false);
    const [deletingRock, setDeletingRock] = useState<Rock | null>(null);

    const { mutate } = useMutation({
        mutationKey: [...rockKeys.all, 'delete'],
        mutationFn: (id: string) => deleteRock(id),

        onSuccess: () => {
            toast.success(SUCCESS_MESSAGES.ROCK.DELETED);
        },

        onError: () => {
            toast.error(ERROR_MESSAGES.ROCK.DELETE_FAILED);
        },

        onSettled: () => {
            if (queryClient.isMutating({ mutationKey: [...rockKeys.all, 'delete'] }) <= 1) {
                queryClient.invalidateQueries({ queryKey: rockKeys.lists() });
                queryClient.invalidateQueries({ queryKey: rockKeys.myRocks() });
            }
        },
    });

    const pendingData = useMutationState({
        filters: { mutationKey: [...rockKeys.all, 'delete'], status: 'pending' },
        select: (mutation) => mutation.state.variables as string,
    });

    const openDelete = (rock: Rock) => {
        setDeletingRock(rock);
        setDeleteDialogOpen(true);
    };

    const handleConfirmDelete = () => {
        if (!deletingRock) {
            return;
        }

        const deletedRockId = deletingRock.id;

        mutate(deletedRockId);
        setDeleteDialogOpen(false);
    };

    return {
        isDeleteDialogOpen,
        setDeleteDialogOpen,
        deletingRock,
        deletingRockIds: pendingData,
        openDelete,
        handleConfirmDelete,
    };
}
