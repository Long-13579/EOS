import { useState } from 'react';
import { useMutation } from '@tanstack/react-query';
import type { CheckedState } from '@radix-ui/react-checkbox';
import { toast } from 'sonner';

import { queryClient } from '@/lib/reactQuery';
import { ERROR_MESSAGES, SUCCESS_MESSAGES } from '@/constants/messages';

import { todoKeys } from '../types/todoKeys';
import { issueKeys } from '@/features/issues/types/issueKeys';
import type { Todo } from '../types/todo';
import { archiveTodo } from '../services/todoService';

interface ArchiveTodoParams {
    id: string;
    isArchived: boolean;
}

interface UseArchiveTodoProps {
    onPageChange?: (page: number) => void;
    onItemRemoved?: () => void;
    issueId?: string;
}

export const useArchiveTodo = ({ onPageChange, onItemRemoved, issueId }: UseArchiveTodoProps) => {
    const [showArchived, setShowArchived] = useState(false);

    const toggleShowArchived = (checked: CheckedState) => {
        setShowArchived(Boolean(checked));
        onPageChange?.(1);
    };

    const mutation = useMutation({
        mutationKey: todoKeys.archiveTodo(),
        mutationFn: ({ id, isArchived }: ArchiveTodoParams) => archiveTodo(id, { isArchived }),

        onError: (_error, { isArchived }) => {
            toast.error(isArchived ? ERROR_MESSAGES.TODO.ARCHIVE_FAILED : ERROR_MESSAGES.TODO.UNARCHIVE_FAILED);
        },

        onSuccess: (serverTodo, { id, isArchived }) => {
            onItemRemoved?.();
            queryClient.setQueryData(todoKeys.detail(id), serverTodo);
            toast.success(isArchived ? SUCCESS_MESSAGES.TODO.ARCHIVED : SUCCESS_MESSAGES.TODO.UNARCHIVED);
            const linkedIssueId = issueId ?? serverTodo.issueId ?? undefined;
            if (linkedIssueId) {
                queryClient.invalidateQueries({
                    queryKey: issueKeys.lists(),
                });
                queryClient.invalidateQueries({
                    queryKey: issueKeys.detail(linkedIssueId),
                });
            }
        },

        onSettled: () => {
            if (queryClient.isMutating({ mutationKey: todoKeys.archiveTodo() }) <= 1) {
                queryClient.invalidateQueries({
                    queryKey: todoKeys.lists(),
                });
                queryClient.invalidateQueries({
                    queryKey: todoKeys.myTodos(),
                });
            }
        },
    });

    const handleToggleArchive = (todo: Todo) => {
        mutation.mutate({ id: todo.id, isArchived: !todo.isArchived });
    };

    return {
        showArchived,
        toggleShowArchived,
        handleToggleArchive,
        isPending: mutation.isPending,
    };
};
