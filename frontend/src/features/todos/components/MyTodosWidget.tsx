import { useState } from 'react';
import { QueryState } from '@/components/shared/QueryState';
import { ERROR_MESSAGES } from '@/constants/messages';
import { TodoDialog } from './TodoDialog';
import { useTodoDialog } from '../hooks/useTodoDialog';
import { useMyTodos } from '../hooks/useMyTodos';
import { CustomPagination } from '@/components/shared/CustomPagination';
import { DEFAULT_LIMIT } from '@/types/pagination';
import { TodoListItem } from './TodoListItem';

export function MyTodosWidget() {
    const [page, setPage] = useState(1);

    const { isTodoDialogOpen, setTodoDialogOpen, editingTodo, openEdit, handleSubmit } = useTodoDialog();

    const {
        data: myTodosResponse,
        isPending,
        isError,
    } = useMyTodos({
        page,
        limit: DEFAULT_LIMIT,
    });

    const todos = myTodosResponse?.data ?? [];
    const totalPages = myTodosResponse?.pagination.totalPages ?? 0;

    const isEmpty = !isPending && !isError && todos.length === 0;

    return (
        <div className="rounded-xl border bg-card shadow-sm p-5 space-y-4">
            <h2 className="text-lg font-semibold">My Todos</h2>

            <QueryState
                isPending={isPending}
                isError={isError}
                isEmpty={isEmpty}
                errorMessage={ERROR_MESSAGES.TODO?.LOAD_FAILED}
                emptyMessage={ERROR_MESSAGES.TODO?.NOT_FOUND}
            >
                <div className="space-y-3">
                    {todos.map((todo) => (
                        <TodoListItem key={todo.id} todo={todo} onUpdate={openEdit} />
                    ))}
                </div>
                <CustomPagination currentPage={page} totalPages={totalPages} onPageChange={setPage} />
            </QueryState>

            <TodoDialog editingTodo={editingTodo} isOpen={isTodoDialogOpen} onOpenChange={setTodoDialogOpen} onSubmit={handleSubmit} />
        </div>
    );
}
