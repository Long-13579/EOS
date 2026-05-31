import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { Pencil, Trash2, Calendar, User, Archive, ArchiveRestore } from 'lucide-react';
import { TableActions } from '@/components/shared/Table/TableActions';
import type { DataTableProps } from '@/types/table';
import { formatDate } from '@/utils/date';
import { getUserFullName } from '@/utils/user';
import type { Issue } from '../types/issue';
import { TableQueryState } from '@/components/shared/Table';
import { ERROR_MESSAGES } from '@/constants/messages';
import { cn } from '@/lib/utils';

interface IssuesTableProps extends DataTableProps<Issue> {
    isPending: boolean;
    isError: boolean;
    emptyMessage?: string;
    onUpdate: (data: Issue) => void;
    onDelete: (data: Issue) => void;
    deletingIssueIds?: string[];
    onToggleArchive: (issue: Issue) => void;
    onOpenTodos?: (issue: Issue) => void;
}

const getIssueActions = (issue: Issue, onUpdate: (data: Issue) => void, onDelete: (data: Issue) => void, onToggleArchive: (issue: Issue) => void) => {
    const isArchived = issue.isArchived;

    return [
        ...(!isArchived
            ? [
                  {
                      label: 'Edit issue',
                      icon: Pencil,
                      onClick: () => onUpdate(issue),
                  },
              ]
            : []),
        {
            label: isArchived ? 'Unarchive issue' : 'Archive issue',
            icon: isArchived ? ArchiveRestore : Archive,
            onClick: () => onToggleArchive(issue),
        },
        {
            label: 'Delete issue',
            icon: Trash2,
            variant: 'destructive' as const,
            onClick: () => onDelete(issue),
        },
    ];
};

export function IssuesTable({
    data,
    isPending,
    isError,
    emptyMessage,
    onUpdate,
    onDelete,
    onToggleArchive,
    deletingIssueIds,
    onOpenTodos,
}: IssuesTableProps) {
    return (
        <div className="rounded-md border">
            <Table className="table-fixed w-full">
                <TableHeader className="[&_th]:font-bold">
                    <TableRow>
                        <TableHead className="pl-6 w-110">Title</TableHead>
                        <TableHead>Issue Type</TableHead>
                        <TableHead className="w-28">Todos</TableHead>
                        <TableHead>Created By</TableHead>
                        <TableHead>Date Created</TableHead>
                        <TableHead className="text-right pr-12 w-30">Actions</TableHead>
                    </TableRow>
                </TableHeader>

                <TableBody>
                    <TableQueryState
                        isPending={isPending}
                        isError={isError}
                        isEmpty={data.length === 0}
                        colSpan={6}
                        errorMessage={ERROR_MESSAGES.ISSUE.LOAD_FAILED}
                        emptyMessage={emptyMessage ?? ERROR_MESSAGES.ISSUE.NOT_FOUND}
                    >
                        {data.map((issue) => {
                            const isArchived = issue.isArchived;
                            const isBeingDeleted = deletingIssueIds?.includes(issue.id);

                            return (
                                <TableRow
                                    key={issue.id}
                                    className={cn(
                                        isArchived && '[&>td:not(:last-child)]:opacity-60',
                                        isBeingDeleted && 'pointer-events-none opacity-50 grayscale transition-all duration-200',
                                    )}
                                    aria-disabled={isBeingDeleted || isArchived}
                                >
                                    <TableCell className="pl-6 font-medium">
                                        <button
                                            type="button"
                                            className={cn('block max-w-130 truncate', !isArchived && 'hover:text-primary hover:underline')}
                                            onClick={() => onUpdate(issue)}
                                            title={issue.title}
                                            aria-label={`Edit Issue: ${issue.title}`}
                                            disabled={isBeingDeleted || isArchived}
                                        >
                                            {issue.title}
                                        </button>
                                    </TableCell>

                                    <TableCell>{issue.issueType?.name ?? '-'}</TableCell>

                                    <TableCell>
                                        {onOpenTodos ? (
                                            <button
                                                type="button"
                                                className={cn(
                                                    'text-sm font-medium text-foreground/80 hover:text-primary hover:underline',
                                                    isArchived && 'text-muted-foreground',
                                                )}
                                                onClick={() => onOpenTodos(issue)}
                                                aria-label={`Open todos for issue ${issue.title}`}
                                                disabled={isBeingDeleted}
                                            >
                                                {issue.totalTodosCount ?? 0}
                                            </button>
                                        ) : (
                                            <span className="text-sm text-muted-foreground">{issue.totalTodosCount ?? 0}</span>
                                        )}
                                    </TableCell>

                                    <TableCell>
                                        {issue.creator ? (
                                            <div className="flex items-center gap-2 text-muted-foreground">
                                                <User className="h-4 w-4 shrink-0" />
                                                <span className="truncate max-w-[160px]" title={getUserFullName(issue.creator)}>
                                                    {getUserFullName(issue.creator)}
                                                </span>
                                            </div>
                                        ) : (
                                            <span className="text-muted-foreground">System</span>
                                        )}
                                    </TableCell>

                                    <TableCell>
                                        <div className="flex items-center gap-2 text-muted-foreground">
                                            <Calendar className="h-4 w-4" />
                                            <span>{formatDate(issue.createdAt)}</span>
                                        </div>
                                    </TableCell>

                                    <TableCell className="text-right pr-4.5">
                                        <TableActions actions={getIssueActions(issue, onUpdate, onDelete, onToggleArchive)} />
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
