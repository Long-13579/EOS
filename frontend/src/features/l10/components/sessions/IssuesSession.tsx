import { useActiveTeamId } from '@/hooks/useActiveTeamId';
import { useIssues } from '@/features/issues/hooks/useIssues';
import { useIssueDialog } from '@/features/issues/hooks/useIssueDialog';
import { useDeleteIssue } from '@/features/issues/hooks/useDeleteIssue';
import { useArchiveIssue } from '@/features/issues/hooks/useArchiveIssue';
import { IssuesTable } from '@/features/issues/components/IssuesTable';
import { IssueDialog } from '@/features/issues/components/IssueDialog';
import { ConfirmationDialog } from '@/components/shared/ConfirmationDialog';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Plus } from 'lucide-react';
import { DEFAULT_LIMIT } from '@/types/pagination';

export function IssuesSession() {
    const teamId = useActiveTeamId();

    const { isIssueDialogOpen, setIssueDialogOpen, editingIssue, openCreate, openEdit, handleSubmit } = useIssueDialog({
        activeTeamId: teamId,
    });
    const { data: issuesResponse, isPending, isError } = useIssues({
        page: 1,
        limit: DEFAULT_LIMIT,
        teamId: teamId!,
    });
    const {
        deletingIssueIds,
        isDeleteDialogOpen,
        setDeleteDialogOpen,
        deletingIssue,
        openDelete,
        handleConfirmDelete,
    } = useDeleteIssue();
    const { handleArchiveToggle } = useArchiveIssue();

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
                    <IssuesTable
                        data={issuesResponse?.data ?? []}
                        isPending={isPending}
                        isError={isError}
                        onUpdate={openEdit}
                        onDelete={openDelete}
                        onToggleArchive={handleArchiveToggle}
                        deletingIssueIds={deletingIssueIds}
                        emptyMessage="No issues."
                    />
                </CardContent>
            </Card>
            <IssueDialog isOpen={isIssueDialogOpen} onOpenChange={setIssueDialogOpen} onSubmit={handleSubmit} issue={editingIssue} />
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
