import { useState } from 'react';
import { useCreateTodo, useUpdateTodo, type Todo, type TodoFormValues, type CreateTodo } from '@/features/todos';
import { useActiveTeamId } from '@/hooks/useActiveTeamId';

interface UseTodoDialogParams {
    onSuccess?: () => void;
    issueId?: string;
    teamId?: string;
}

export function useTodoDialog({ onSuccess, issueId, teamId }: UseTodoDialogParams = {}) {
    const activeTeamId = useActiveTeamId();
    const [isTodoDialogOpen, setTodoDialogOpen] = useState(false);
    const [editingTodo, setEditingTodo] = useState<Todo | null>(null);

    const { createTodo } = useCreateTodo();
    const { updateTodo } = useUpdateTodo();

    const openCreate = () => {
        setEditingTodo(null);
        setTodoDialogOpen(true);
    };

    const openEdit = (todo: Todo) => {
        setEditingTodo(todo);
        setTodoDialogOpen(true);
    };

    const handleSubmit = async (data: TodoFormValues) => {
        const resolvedTeamId = teamId ?? activeTeamId;

        if (!resolvedTeamId) {
            return;
        }

        if (editingTodo) {
            await updateTodo({
                id: editingTodo.id,
                data: {
                    ...data,
                    dueDate: data.dueDate?.toISOString(),
                    issueId: issueId ?? editingTodo.issueId,
                },
            });
        } else {
            const payload: CreateTodo = {
                ...data,
                teamId: resolvedTeamId,
                dueDate: data.dueDate?.toISOString(),
                issueId,
            };

            await createTodo(payload);
            onSuccess?.();
        }
        setTodoDialogOpen(false);
    };

    return { isTodoDialogOpen, setTodoDialogOpen, editingTodo, openCreate, openEdit, handleSubmit };
}
