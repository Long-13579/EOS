import { useState } from 'react';
import { Plus } from 'lucide-react';
import { PageHeaderGroup } from '@/components/shared/PageHeaderGroup';
import { CustomPagination } from '@/components/shared/CustomPagination';
import { Button } from '@/components/ui/button';
import { DEFAULT_LIMIT } from '@/types/pagination';
import { CONFIRM_MESSAGES, ERROR_MESSAGES } from '@/constants/messages';
import {
    useTodos,
    TodoDialog,
    TodosTable,
    TodoStatusFilter,
    useTodoDialog,
    useFilterTodo,
    type TodoFormValues,
    ALL_TODO_STATUSES,
    useArchiveTodo,
    useDeleteTodo,
    useQuickUpdateTodoStatus,
} from '@/features/todos';
import { EmptyTeamState } from '@/components/shared/EmptyTeamState';
import { Checkbox } from '@/components/ui/checkbox';
import { ConfirmationDialog } from '@/components/shared/ConfirmationDialog';
import { useActiveTeamId } from '@/hooks/useActiveTeamId';

export function Todos() {
    const activeTeamId = useActiveTeamId();
    const [page, setPage] = useState(1);

    const resetPageIfNeeded = () => {
        if (todos.length === 1 && page > 1) {
            setPage((prev) => prev - 1);
        }
    };

    const { isTodoDialogOpen, setTodoDialogOpen, editingTodo, openCreate, openEdit, handleSubmit } = useTodoDialog({
        onSuccess: () => setPage(1),
    });
    const { filterStatus, handleFilterChange } = useFilterTodo(() => setPage(1));

    const {
        showArchived,
        toggleShowArchived,
        handleToggleArchive,
        isPending: isArchiving,
    } = useArchiveTodo({
        onPageChange: setPage,
        onItemRemoved: resetPageIfNeeded,
    });

    const { updateStatus: quickUpdateTodoStatus } = useQuickUpdateTodoStatus();

    const {
        data: todosResponse,
        isPending,
        isError,
        isPlaceholderData,
    } = useTodos({
        page,
        limit: DEFAULT_LIMIT,
        teamId: activeTeamId,
        status: filterStatus !== ALL_TODO_STATUSES ? filterStatus : undefined,
        isArchived: showArchived,
    });

    const emptyMessage = showArchived ? ERROR_MESSAGES.TODO.ARCHIVED_NOT_FOUND : ERROR_MESSAGES.TODO.NOT_FOUND;

    const todos = todosResponse?.data ?? [];
    const totalPages = todosResponse?.pagination.totalPages ?? 0;

    const { isDeleteDialogOpen, setDeleteDialogOpen, deletingTodo, deletingTodoIds, handleDelete, handleConfirmDelete } = useDeleteTodo({
        onSuccess: resetPageIfNeeded,
    });

    const handleTodoSubmit = async (data: TodoFormValues) => {
        await handleSubmit(data);
    };

    return (
        <div className="flex flex-col gap-6">
            <PageHeaderGroup title="To-dos" description="Stay focused on what needs to get done.">
                <Button onClick={openCreate} disabled={!activeTeamId}>
                    <Plus className="mr-2 h-4 w-4" aria-hidden="true" />
                    Add To-do
                </Button>
            </PageHeaderGroup>

            <TodoDialog isOpen={isTodoDialogOpen} onOpenChange={setTodoDialogOpen} editingTodo={editingTodo} onSubmit={handleTodoSubmit} />

            <ConfirmationDialog
                isOpen={isDeleteDialogOpen}
                onOpenChange={setDeleteDialogOpen}
                title="Delete this To-do?"
                description={deletingTodo ? CONFIRM_MESSAGES.DELETE.CONFIRM_ITEM(deletingTodo.title) : CONFIRM_MESSAGES.DELETE.GENERIC}
                confirmLabel="Delete"
                cancelLabel="Cancel"
                onConfirm={handleConfirmDelete}
            />

            {activeTeamId && (
                <div className={'flex justify-end rounded-lg bg-muted/30 p-4 border items-center gap-4'}>
                    <TodoStatusFilter value={filterStatus} onChange={handleFilterChange} isDisabled={isPending} />

                    <label className="flex items-center gap-2 text-sm font-medium cursor-pointer">
                        <Checkbox checked={showArchived} onCheckedChange={toggleShowArchived} disabled={isPending} />
                        <span>Show archived</span>
                    </label>
                </div>
            )}

            {!activeTeamId ? (
                <EmptyTeamState />
            ) : (
                <div className={isPlaceholderData ? 'opacity-50 transition-opacity' : ''}>
                    <TodosTable
                        isError={isError}
                        isPending={isPending && !isPlaceholderData}
                        emptyMessage={emptyMessage}
                        data={todos}
                        onUpdate={openEdit}
                        onDelete={handleDelete}
                        deletingTodoIds={deletingTodoIds}
                        onToggleArchive={handleToggleArchive}
                        isArchiving={isArchiving}
                        onQuickStatusUpdate={quickUpdateTodoStatus}
                    />
                    <CustomPagination currentPage={page} totalPages={totalPages} onPageChange={setPage} />
                </div>
            )}
        </div>
    );
}
