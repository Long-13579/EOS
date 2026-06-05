import { useMutation, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { todoKeys } from '../types/todoKeys';
import { updateTodoStatus } from '../services/todoService';
import type { TodoStatus, Todo, UpdateTodoStatus } from '../types/todo';
import type { PaginatedResponse } from '@/types/pagination';
import { SUCCESS_MESSAGES, ERROR_MESSAGES } from '@/constants/messages';

interface UseQuickUpdateTodoStatusMutationParams {
    id: string;
    status: TodoStatus;
}

export const useQuickUpdateTodoStatus = () => {
    const queryClient = useQueryClient();

    const { mutateAsync, isPending, variables } = useMutation({
        mutationFn: ({ id, status }: UseQuickUpdateTodoStatusMutationParams) =>
            updateTodoStatus(id, { status } satisfies UpdateTodoStatus),

        onMutate: async ({ id, status }) => {
            await queryClient.cancelQueries({
                queryKey: todoKeys.lists(),
            });

            const previous = queryClient.getQueriesData<PaginatedResponse<Todo>>({
                queryKey: todoKeys.lists(),
            });

            queryClient.setQueriesData<PaginatedResponse<Todo>>({ queryKey: todoKeys.lists() }, (old) => {
                if (!old?.data) return old;
                return {
                    ...old,
                    data: old.data.map((todo) => (todo.id === id ? { ...todo, status } : todo)),
                };
            });

            return { previous };
        },

        onError: (_err, _vars, context) => {
            context?.previous?.forEach(([key, data]) => {
                queryClient.setQueryData(key, data);
            });
            toast.error(ERROR_MESSAGES.TODO.UPDATE_STATUS_FAILED);
        },

        onSuccess: () => {
            toast.success(SUCCESS_MESSAGES.TODO.STATUS_UPDATED);
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
        updateStatus: mutateAsync,
        isPending,
        pendingStatus: variables?.status ?? null,
    };
};
