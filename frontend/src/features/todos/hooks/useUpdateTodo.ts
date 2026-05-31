import { useMutation, useQueryClient } from '@tanstack/react-query';
import { todoKeys } from '../types/todoKeys';
import { issueKeys } from '@/features/issues/types/issueKeys';
import type { UpdateTodo } from '../types/todo';
import { updateTodo } from '../services/todoService';
import { SUCCESS_MESSAGES } from '@/constants/messages';
import { toast } from 'sonner';

interface UpdateTodoMutationParams {
    id: string;
    data: UpdateTodo;
}

export const useUpdateTodo = () => {
    const queryClient = useQueryClient();

    const mutation = useMutation({
        mutationFn: ({ id, data }: UpdateTodoMutationParams) => updateTodo(id, data),

        onSuccess: (serverTodo, { id }) => {
            toast.success(SUCCESS_MESSAGES.TODO.UPDATED);
            queryClient.setQueryData(todoKeys.detail(id), serverTodo);
            const issueId = serverTodo.issueId ?? undefined;
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
        updateTodo: mutation.mutateAsync,
    };
};
