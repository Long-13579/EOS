import { useState } from 'react';
import { useActiveTeamId } from '@/hooks/useActiveTeamId';
import { useTodoDialog } from '@/features/todos/hooks/useTodoDialog';
import { useTodos } from '@/features/todos/hooks/useTodos';
import { useDeleteTodo } from '@/features/todos/hooks/useDeleteTodo';
import { useArchiveTodo } from '@/features/todos/hooks/useArchiveTodo';
import { TodosTable } from '@/features/todos/components/TodosTable';
import { TodoStatusFilter } from '@/features/todos/components/TodoStatusFilter';
import { TodoDialog } from '@/features/todos/components/TodoDialog';
import { ConfirmationDialog } from '@/components/shared/ConfirmationDialog';
import { CustomPagination } from '@/components/shared/CustomPagination';
import { Checkbox } from '@/components/ui/checkbox';
import { DEFAULT_LIMIT } from '@/types/pagination';
import { ALL_TODO_STATUSES, type TodoStatus } from '@/features/todos';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Plus } from 'lucide-react';

export function TodoSession() {
    const teamId = useActiveTeamId();
    const [page, setPage] = useState(1);
    const [filterStatus, setFilterStatus] = useState<TodoStatus | typeof ALL_TODO_STATUSES>(ALL_TODO_STATUSES);
    const [showArchived, setShowArchived] = useState(false);

    const { isTodoDialogOpen, setTodoDialogOpen, editingTodo, openCreate, openEdit, handleSubmit } = useTodoDialog();
    const {
        data: todosResponse,
        isPending,
        isError,
    } = useTodos({
        teamId: teamId!,
        page,
        limit: DEFAULT_LIMIT,
        status: filterStatus !== ALL_TODO_STATUSES ? filterStatus : undefined,
        isArchived: showArchived,
    });
    const { deletingTodoIds, isDeleteDialogOpen, setDeleteDialogOpen, deletingTodo, handleDelete, handleConfirmDelete } = useDeleteTodo();
    const { handleToggleArchive } = useArchiveTodo({});

    const totalPages = todosResponse?.pagination.totalPages ?? 0;

    const handleFilterChange = (status: TodoStatus | typeof ALL_TODO_STATUSES) => {
        setFilterStatus(status);
        setPage(1);
    };

    const handleShowArchivedChange = (checked: boolean | 'indeterminate') => {
        setShowArchived(Boolean(checked));
        setPage(1);
    };

    return (
        <>
            <Card>
                <CardHeader>
                    <div className="flex items-center justify-between">
                        <CardTitle>To-Dos</CardTitle>
                        <Button type="button" size="sm" onClick={openCreate}>
                            <Plus className="mr-1 h-4 w-4" />
                            Add To-do
                        </Button>
                    </div>
                </CardHeader>
                <CardContent>
                    <div className="flex justify-end rounded-lg bg-muted/30 p-4 border items-center gap-4 mb-4">
                        <TodoStatusFilter value={filterStatus} onChange={handleFilterChange} isDisabled={isPending} />
                        <label className="flex items-center gap-2 text-sm font-medium cursor-pointer">
                            <Checkbox checked={showArchived} onCheckedChange={handleShowArchivedChange} disabled={isPending} />
                            <span>Show archived</span>
                        </label>
                    </div>
                    <TodosTable
                        data={todosResponse?.data ?? []}
                        isPending={isPending}
                        isError={isError}
                        onUpdate={openEdit}
                        onDelete={handleDelete}
                        onToggleArchive={handleToggleArchive}
                        deletingTodoIds={deletingTodoIds}
                        emptyMessage="No to-dos."
                    />
                    <CustomPagination currentPage={page} totalPages={totalPages} onPageChange={setPage} />
                </CardContent>
            </Card>
            <TodoDialog isOpen={isTodoDialogOpen} onOpenChange={setTodoDialogOpen} onSubmit={handleSubmit} editingTodo={editingTodo} />
            <ConfirmationDialog
                isOpen={isDeleteDialogOpen}
                onOpenChange={setDeleteDialogOpen}
                title="Delete To-do"
                description={`Are you sure you want to delete "${deletingTodo?.title}"?`}
                confirmLabel="Delete"
                onConfirm={handleConfirmDelete}
            />
        </>
    );
}
