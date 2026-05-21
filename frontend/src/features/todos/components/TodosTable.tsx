import { cn } from '@/lib/utils';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { Pencil, Trash2, Calendar, Archive, ArchiveRestore } from 'lucide-react';
import { TableActions } from '@/components/shared/Table/TableActions';
import { TableQueryState } from '@/components/shared/Table';
import type { Todo } from '@/features/todos/types/todo';
import { formatDate } from '@/utils/date';
import { StatusBadge } from './StatusBadge';
import { TodoAssigneesCell } from './TodoAssigneesCell';
import type { DataTableProps } from '@/types/table';
import { ERROR_MESSAGES } from '@/constants/messages';

interface TodosTableProps extends DataTableProps<Todo> {
    isPending: boolean;
    isError: boolean;
    emptyMessage?: string;
    onUpdate: (data: Todo) => void;
    onDelete: (data: Todo) => void;
    onToggleArchive: (data: Todo) => void;
    deletingTodoIds?: string[];
    isArchiving?: boolean;
}

const getTodoActions = (todo: Todo, onUpdate: (data: Todo) => void, onDelete: (data: Todo) => void, onToggleArchive: (data: Todo) => void) => {
    const { isArchived } = todo;
    return [
        ...(isArchived
            ? []
            : [
                  {
                      label: 'Edit To-do',
                      icon: Pencil,
                      onClick: () => onUpdate(todo),
                  },
              ]),
        {
            label: isArchived ? 'Unarchive To-do' : 'Archive To-do',
            icon: isArchived ? ArchiveRestore : Archive,
            onClick: () => onToggleArchive(todo),
        },
        {
            label: 'Delete To-do',
            icon: Trash2,
            variant: 'destructive' as const,
            onClick: () => onDelete(todo),
        },
    ];
};

export function TodosTable({
    data,
    isPending,
    isError,
    emptyMessage,
    onUpdate,
    onDelete,
    onToggleArchive,
    deletingTodoIds,
    isArchiving,
}: TodosTableProps) {
    return (
        <div className="rounded-md border">
            <Table className="w-full table-fixed">
                <TableHeader className="[&_th]:font-bold">
                    <TableRow>
                        <TableHead className="pl-6 w-75">Title</TableHead>
                        <TableHead className="w-55">Assignees</TableHead>
                        <TableHead className="w-45">Due Date</TableHead>
                        <TableHead className="w-35">Status</TableHead>
                        <TableHead className="text-right pr-12 w-30">Actions</TableHead>
                    </TableRow>
                </TableHeader>

                <TableBody>
                    <TableQueryState
                        isPending={isPending}
                        isError={isError}
                        isEmpty={data.length === 0}
                        colSpan={5}
                        errorMessage={ERROR_MESSAGES.TODO.LOAD_FAILED}
                        emptyMessage={emptyMessage ?? ERROR_MESSAGES.TODO.NOT_FOUND}
                    >
                        {data.map((todo) => {
                            const isBeingDeleted = deletingTodoIds?.includes(todo.id);
                            const isArchived = todo.isArchived;
                            return (
                                <TableRow
                                    key={todo.id}
                                    className={cn(
                                        (isBeingDeleted || isArchiving) && 'pointer-events-none',
                                        isBeingDeleted && 'opacity-50 grayscale',
                                        isArchiving && 'opacity-70',
                                        isArchived && '[&>td:not(:last-child)]:opacity-60',
                                    )}
                                    aria-disabled={isBeingDeleted || isArchived || isArchiving}
                                >
                                    <TableCell className="pl-6 font-medium">
                                        <button
                                            type="button"
                                            className={cn('block max-w-[320px] truncate', !isArchived && 'hover:text-primary hover:underline')}
                                            onClick={() => onUpdate(todo)}
                                            title={todo.title}
                                            aria-label={`Edit To-do: ${todo.title}`}
                                            disabled={isBeingDeleted || isArchived || isArchiving}
                                        >
                                            {todo.title}
                                        </button>
                                    </TableCell>

                                    <TableCell>
                                        <TodoAssigneesCell assignees={todo.assignees} />
                                    </TableCell>

                                    <TableCell>
                                        {todo.dueDate ? (
                                            <div className="flex items-center gap-2 text-muted-foreground">
                                                <Calendar className="h-4 w-4" />
                                                <span>{formatDate(todo.dueDate)}</span>
                                            </div>
                                        ) : (
                                            <span className="text-muted-foreground">No due date</span>
                                        )}
                                    </TableCell>

                                    <TableCell>
                                        <StatusBadge status={todo.status} />
                                    </TableCell>

                                    <TableCell className="text-right pr-4.5">
                                        <TableActions actions={getTodoActions(todo, onUpdate, onDelete, onToggleArchive)} />
                                    </TableCell>
                                </TableRow>
                            );
                        })}
                    </TableQueryState>
                </TableBody>
            </Table>
        </div>
    );
}
