import { useMutation, useQueryClient } from '@tanstack/react-query';
import { todoKeys } from '../types/todoKeys';
import type { CreateTodo } from '../types/todo';
import { createTodo } from '../services/todoService';
import { SUCCESS_MESSAGES } from '@/constants/messages';
import { toast } from 'sonner';

export const useCreateTodo = () => {
    const queryClient = useQueryClient();

    const mutation = useMutation({
        mutationFn: (payload: CreateTodo) => createTodo(payload),

        onSuccess: () => {
            toast.success(SUCCESS_MESSAGES.TODO.CREATED);
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
