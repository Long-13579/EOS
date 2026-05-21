import { useState } from 'react';
import { useMutation, useMutationState, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { ERROR_MESSAGES, SUCCESS_MESSAGES } from '@/constants/messages';
import { deleteTodo } from '../services/todoService';
import { todoKeys } from '../types/todoKeys';
import type { Todo } from '../types/todo';

export const useDeleteTodo = (onSuccess?: () => void) => {
    const queryClient = useQueryClient();
    const [deletingTodo, setDeletingTodo] = useState<Todo | null>(null);
    const [isDeleteDialogOpen, setDeleteDialogOpen] = useState(false);

    const handleDelete = (todo: Todo) => {
        setDeletingTodo(todo);
        setDeleteDialogOpen(true);
    };

    const { mutate } = useMutation({
        mutationKey: todoKeys.deleteTodo(),
        mutationFn: (id: string) => deleteTodo(id),

        onSuccess: () => {
            toast.success(SUCCESS_MESSAGES.TODO.DELETED);
            onSuccess?.();
        },

        onError: () => {
            toast.error(ERROR_MESSAGES.TODO.DELETE_FAILED);
        },

        onSettled: () => {
            if (queryClient.isMutating({ mutationKey: todoKeys.deleteTodo() }) <= 1) {
                queryClient.invalidateQueries({
                    queryKey: todoKeys.lists(),
                });
                queryClient.invalidateQueries({
                    queryKey: todoKeys.myTodos(),
                });
            }
        },
    });

    const handleConfirmDelete = () => {
        if (!deletingTodo) {
            return;
        }

        mutate(deletingTodo.id);
    };

    const pendingData = useMutationState({
        filters: { mutationKey: todoKeys.deleteTodo(), status: 'pending' },
        select: (mutation) => mutation.state.variables as string,
    });

    return {
        deletingTodoIds: pendingData,
        isDeleteDialogOpen,
        setDeleteDialogOpen,
        deletingTodo,
        handleDelete,
        handleConfirmDelete,
    };
};
