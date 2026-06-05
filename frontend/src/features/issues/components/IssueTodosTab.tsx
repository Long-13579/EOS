import { useEffect, useRef, useState } from 'react';
import type { Issue } from '../types/issue';
import { useActiveTeamId } from '@/hooks/useActiveTeamId';
import { Button } from '@/components/ui/button';
import { Checkbox } from '@/components/ui/checkbox';
import { ConfirmationDialog } from '@/components/shared/ConfirmationDialog';
import { CustomPagination } from '@/components/shared/CustomPagination';
import { EmptyTeamState } from '@/components/shared/EmptyTeamState';
import { DEFAULT_LIMIT } from '@/types/pagination';
import { CONFIRM_MESSAGES, ERROR_MESSAGES } from '@/constants/messages';
import { TodoDialog, TodosTable, useArchiveTodo, useDeleteTodo, useQuickUpdateTodoStatus, useTodoDialog, useTodos, type Todo } from '@/features/todos';

interface IssueTodosTabProps {
    issue: Issue | null;
}

export function IssueTodosTab({ issue }: Readonly<IssueTodosTabProps>) {
    const activeTeamId = useActiveTeamId();
    const [page, setPage] = useState(1);

    const isCreateMode = !issue;
    const isReadOnly = !!issue?.isArchived;
    const teamId = issue?.team?.id ?? activeTeamId;
    const issueId = issue?.id;

    const todosLengthRef = useRef(0);
    const resetPageIfNeeded = () => {
        setPage((prev) => (todosLengthRef.current === 1 && prev > 1 ? prev - 1 : prev));
    };

    const { updateStatus: quickUpdateTodoStatus } = useQuickUpdateTodoStatus();

    const {
        showArchived,
        toggleShowArchived,
        handleToggleArchive,
        isPending: isArchiving,
    } = useArchiveTodo({
        onPageChange: setPage,
        onItemRemoved: resetPageIfNeeded,
        issueId,
    });

    const {
        data: todosResponse,
        isPending,
        isError,
        isPlaceholderData,
    } = useTodos({
        page,
        limit: DEFAULT_LIMIT,
        teamId,
        issueId: issueId ?? undefined,
        isArchived: showArchived,
    });

    const todos = todosResponse?.data ?? [];
    const totalPages = todosResponse?.pagination.totalPages ?? 0;

    useEffect(() => {
        todosLengthRef.current = todos.length;
    }, [todos.length]);

    const { isTodoDialogOpen, setTodoDialogOpen, editingTodo, openCreate, openEdit, handleSubmit } = useTodoDialog({
        onSuccess: () => setPage(1),
        issueId,
        teamId,
    });

    const { isDeleteDialogOpen, setDeleteDialogOpen, deletingTodo, deletingTodoIds, handleDelete, handleConfirmDelete } = useDeleteTodo({
        onSuccess: resetPageIfNeeded,
        issueId,
    });

    if (isCreateMode) {
        return (
            <div className="rounded-lg border border-dashed p-6 text-sm text-muted-foreground">
                Create the issue first to manage related to-dos.
            </div>
        );
    }

    if (!teamId) {
        return <EmptyTeamState message="Select a team to manage issue to-dos." />;
    }

    const handleOpenCreate = () => {
        if (!isReadOnly) {
            openCreate();
        }
    };

    const handleOpenEdit = (todo: Todo) => {
        if (!isReadOnly) {
            openEdit(todo);
        }
    };

    const handleArchive = (todo: Todo) => {
        if (!isReadOnly) {
            handleToggleArchive(todo);
        }
    };

    const handleDeleteClick = (todo: Todo) => {
        if (!isReadOnly) {
            handleDelete(todo);
        }
    };

    return (
        <div className="flex flex-col gap-5">
            {isReadOnly && (
                <div className="rounded-lg border border-dashed bg-muted/30 px-4 py-3 text-sm text-muted-foreground">
                    Archived issues are read-only. Unarchive the issue to add or edit to-dos.
                </div>
            )}

            <div className="flex flex-wrap items-center justify-between gap-3">
                <Button onClick={handleOpenCreate} disabled={isReadOnly}>
                    Add To-do
                </Button>

                <label className="flex items-center gap-2 text-sm font-medium cursor-pointer">
                    <Checkbox checked={showArchived} onCheckedChange={toggleShowArchived} disabled={isPending} />
                    <span>Show archived</span>
                </label>
            </div>

            <TodoDialog
                isOpen={isTodoDialogOpen}
                onOpenChange={setTodoDialogOpen}
                editingTodo={editingTodo}
                onSubmit={handleSubmit}
                teamId={teamId}
            />

            <ConfirmationDialog
                isOpen={isDeleteDialogOpen}
                onOpenChange={setDeleteDialogOpen}
                title="Delete this To-do?"
                description={deletingTodo ? CONFIRM_MESSAGES.DELETE.CONFIRM_ITEM(deletingTodo.title) : CONFIRM_MESSAGES.DELETE.GENERIC}
                confirmLabel="Delete"
                cancelLabel="Cancel"
                onConfirm={handleConfirmDelete}
            />

            <div className={isPlaceholderData ? 'opacity-50 transition-opacity' : ''}>
                <TodosTable
                    isError={isError}
                    isPending={isPending && !isPlaceholderData}
                    emptyMessage={showArchived ? ERROR_MESSAGES.TODO.ARCHIVED_NOT_FOUND : ERROR_MESSAGES.TODO.NOT_FOUND}
                    data={todos}
                    onUpdate={handleOpenEdit}
                    onDelete={handleDeleteClick}
                    deletingTodoIds={deletingTodoIds}
                    onToggleArchive={handleArchive}
                    isArchiving={isArchiving}
                    isReadOnly={isReadOnly}
                    onQuickStatusUpdate={isReadOnly ? undefined : quickUpdateTodoStatus}
                />
                <CustomPagination currentPage={page} totalPages={totalPages} onPageChange={setPage} />
            </div>
        </div>
    );
}
