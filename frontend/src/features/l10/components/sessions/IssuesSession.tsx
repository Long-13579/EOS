import { useState } from 'react';
import { useActiveTeamId } from '@/hooks/useActiveTeamId';
import { useIssues } from '@/features/issues/hooks/useIssues';
import { useIssueDialog } from '@/features/issues/hooks/useIssueDialog';
import { useDeleteIssue } from '@/features/issues/hooks/useDeleteIssue';
import { useArchiveIssue } from '@/features/issues/hooks/useArchiveIssue';
import { IssuesTable } from '@/features/issues/components/IssuesTable';
import { IssueDialog } from '@/features/issues/components/IssueDialog';
import { IssueTodosDialog } from '@/features/issues/components/IssueTodosDialog';
import { useIssueTodosDialog } from '@/features/issues/hooks/useIssueTodosDialog';
import { ConfirmationDialog } from '@/components/shared/ConfirmationDialog';
import { CustomPagination } from '@/components/shared/CustomPagination';
import { IssueViewFilter } from '@/components/shared/IssueViewFilter';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Plus } from 'lucide-react';
import { DEFAULT_LIMIT } from '@/types/pagination';
import { ISSUE_VIEW, type IssueView } from '@/features/issues';

export function IssuesSession() {
    const teamId = useActiveTeamId();
    const [page, setPage] = useState(1);
    const [view, setView] = useState<IssueView>(ISSUE_VIEW.ISSUES);

    const { isIssueDialogOpen, setIssueDialogOpen, editingIssue, openCreate, openEdit, handleSubmit } = useIssueDialog({
        activeTeamId: teamId,
    });
    const { isIssueTodosDialogOpen, setIssueTodosDialogOpen, issue, openIssueTodos } = useIssueTodosDialog();
    const {
        data: issuesResponse,
        isPending,
        isError,
    } = useIssues({
        page,
        limit: DEFAULT_LIMIT,
        teamId: teamId!,
        view,
    });
    const { deletingIssueIds, isDeleteDialogOpen, setDeleteDialogOpen, deletingIssue, openDelete, handleConfirmDelete } = useDeleteIssue();
    const { handleArchiveToggle } = useArchiveIssue();

    const totalPages = issuesResponse?.pagination.totalPages ?? 0;

    const handleViewChange = (value: IssueView) => {
        setView(value);
        setPage(1);
    };

    return (
        <>
            <Card>
                <CardHeader>
                    <div className="flex items-center justify-between">
                        <CardTitle>Issues</CardTitle>
                        <Button type="button" size="sm" onClick={openCreate}>
                            <Plus className="mr-1 h-4 w-4" />
                            Add Issue
                        </Button>
                    </div>
                </CardHeader>
                <CardContent>
                    <div className="mb-4">
                        <IssueViewFilter value={view} onChange={handleViewChange} disabled={isPending} />
                    </div>
                    <IssuesTable
                        data={issuesResponse?.data ?? []}
                        isPending={isPending}
                        isError={isError}
                        onUpdate={openEdit}
                        onDelete={openDelete}
                        onToggleArchive={handleArchiveToggle}
                        deletingIssueIds={deletingIssueIds}
                        emptyMessage="No issues."
                        onOpenTodos={openIssueTodos}
                    />
                    <CustomPagination currentPage={page} totalPages={totalPages} onPageChange={setPage} />
                </CardContent>
            </Card>
            <IssueDialog isOpen={isIssueDialogOpen} onOpenChange={setIssueDialogOpen} onSubmit={handleSubmit} issue={editingIssue} />
            <IssueTodosDialog isOpen={isIssueTodosDialogOpen} onOpenChange={setIssueTodosDialogOpen} issue={issue} />
            <ConfirmationDialog
                isOpen={isDeleteDialogOpen}
                onOpenChange={setDeleteDialogOpen}
                title="Delete Issue"
                description={`Are you sure you want to delete "${deletingIssue?.title}"?`}
                confirmLabel="Delete"
                onConfirm={handleConfirmDelete}
            />
        </>
    );
}
