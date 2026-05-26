import { useActiveTeamId } from '@/hooks/useActiveTeamId';
import { useTodoDialog } from '@/features/todos/hooks/useTodoDialog';
import { useTodos } from '@/features/todos/hooks/useTodos';
import { useDeleteTodo } from '@/features/todos/hooks/useDeleteTodo';
import { useArchiveTodo } from '@/features/todos/hooks/useArchiveTodo';
import { TodosTable } from '@/features/todos/components/TodosTable';
import { TodoDialog } from '@/features/todos/components/TodoDialog';
import { ConfirmationDialog } from '@/components/shared/ConfirmationDialog';
import { DEFAULT_LIMIT } from '@/types/pagination';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Plus } from 'lucide-react';

export function TodoSession() {
    const teamId = useActiveTeamId();

    const { isTodoDialogOpen, setTodoDialogOpen, editingTodo, openCreate, openEdit, handleSubmit } = useTodoDialog();
    const { data: todosResponse, isPending, isError } = useTodos({
        teamId: teamId!,
        page: 1,
        limit: DEFAULT_LIMIT,
    });
    const {
        deletingTodoIds,
        isDeleteDialogOpen,
        setDeleteDialogOpen,
        deletingTodo,
        handleDelete,
        handleConfirmDelete,
    } = useDeleteTodo();
    const { handleToggleArchive } = useArchiveTodo({});

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
