import { useMutation, useQueryClient } from '@tanstack/react-query';
import { todoKeys } from '../types/todoKeys';
import { issueKeys } from '@/features/issues/types/issueKeys';
import type { CreateTodo } from '../types/todo';
import { createTodo } from '../services/todoService';
import { SUCCESS_MESSAGES } from '@/constants/messages';
import { toast } from 'sonner';

export const useCreateTodo = () => {
    const queryClient = useQueryClient();

    const mutation = useMutation({
        mutationFn: (payload: CreateTodo) => createTodo(payload),

        onSuccess: (serverTodo, variables) => {
            toast.success(SUCCESS_MESSAGES.TODO.CREATED);
            const issueId = variables.issueId ?? serverTodo.issueId ?? undefined;
            if (issueId) {
                queryClient.invalidateQueries({
                    queryKey: issueKeys.lists(),
                });
                queryClient.invalidateQueries({
                    queryKey: issueKeys.detail(issueId),
                });
            }
        },
        onSettled: () => {
            queryClient.invalidateQueries({
                queryKey: todoKeys.lists(),
            });
            queryClient.invalidateQueries({
                queryKey: todoKeys.myTodos(),
            });
        },
    });

    return {
        createTodo: mutation.mutateAsync,
    };
};
