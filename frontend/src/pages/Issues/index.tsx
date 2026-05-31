import { Plus } from 'lucide-react';
import { useEffect, useState } from 'react';
import { PageHeaderGroup } from '@/components/shared/PageHeaderGroup';
import { CustomPagination } from '@/components/shared/CustomPagination';
import { EmptyTeamState } from '@/components/shared/EmptyTeamState';
import { ConfirmationDialog } from '@/components/shared/ConfirmationDialog';
import { Button } from '@/components/ui/button';
import { DEFAULT_LIMIT } from '@/types/pagination';
import { CONFIRM_MESSAGES, ERROR_MESSAGES } from '@/constants/messages';
import { useActiveTeamId } from '@/hooks/useActiveTeamId';
import {
    IssuesTable,
    IssueDialog,
    IssueTodosDialog,
    useIssueDialog,
    useIssueTodosDialog,
    useIssues,
    useDeleteIssue,
    useArchiveIssue,
    type IssueView,
    ISSUE_VIEW,
    LONG_TERM_ISSUE,
} from '@/features/issues';
import { IssueViewFilter } from '@/components/shared/IssueViewFilter';
import { toast } from 'sonner';

export function Issues() {
    const activeTeamId = useActiveTeamId();

    const [page, setPage] = useState(1);
    const [view, setView] = useState<IssueView>(ISSUE_VIEW.ISSUES);

    const { isIssueDialogOpen, setIssueDialogOpen, editingIssue, openCreate, openEdit, handleSubmit } = useIssueDialog({
        activeTeamId,
        onCreated: () => setPage(1),
    });
    const { isIssueTodosDialogOpen, setIssueTodosDialogOpen, issue, openIssueTodos } = useIssueTodosDialog();
    const { isDeleteDialogOpen, setDeleteDialogOpen, deletingIssue, deletingIssueIds, openDelete, handleConfirmDelete } = useDeleteIssue();

    const { handleArchiveToggle } = useArchiveIssue();

    const {
        data: issuesResponse,
        isPending,
        isError,
        isPlaceholderData,
        isLongTermView,
        isMissingLongTermIssueType,
        isIssueTypesError,
        isArchivedView,
    } = useIssues({
        page,
        limit: DEFAULT_LIMIT,
        teamId: activeTeamId,
        view,
    });

    const issues = issuesResponse?.data ?? [];
    const isViewDisabled = isPending && !isPlaceholderData && !isLongTermView;
    const totalPages = issuesResponse?.pagination.totalPages ?? 0;
    const emptyMessage =
        (isArchivedView && ERROR_MESSAGES.ISSUE.ARCHIVED_NOT_FOUND) ||
        (isLongTermView && ERROR_MESSAGES.ISSUE.LONG_TERM_NOT_FOUND) ||
        ERROR_MESSAGES.ISSUE.NOT_FOUND;

    useEffect(() => {
        if (isLongTermView && isIssueTypesError) {
            toast.error(ERROR_MESSAGES.ISSUE_TYPE.LOAD_FAILED, { id: 'issue-error' });
        } else if (isMissingLongTermIssueType) {
            toast.error(ERROR_MESSAGES.ISSUE_TYPE.NOT_FOUND(LONG_TERM_ISSUE), { id: 'issue-error' });
        } else {
            toast.dismiss('issue-error');
        }
    }, [isMissingLongTermIssueType, isIssueTypesError, isLongTermView]);

    const handleViewChange = (value: IssueView) => {
        setView(value);
        setPage(1);
    };

    return (
        <div className="flex flex-col gap-6">
            <PageHeaderGroup title="Issues" description="Identify, discuss, and solve key issues.">
                <Button onClick={openCreate} disabled={!activeTeamId}>
                    <Plus className="mr-2 h-4 w-4" aria-hidden="true" />
                    Add Issue
                </Button>
            </PageHeaderGroup>

            <IssueDialog isOpen={isIssueDialogOpen} onOpenChange={setIssueDialogOpen} issue={editingIssue} onSubmit={handleSubmit} />
            <IssueTodosDialog isOpen={isIssueTodosDialogOpen} onOpenChange={setIssueTodosDialogOpen} issue={issue} />

            <IssueViewFilter value={view} onChange={handleViewChange} disabled={isViewDisabled} />

            <ConfirmationDialog
                isOpen={isDeleteDialogOpen}
                onOpenChange={setDeleteDialogOpen}
                title="Delete this issue?"
                description={deletingIssue ? CONFIRM_MESSAGES.DELETE.CONFIRM_ITEM(deletingIssue.title) : CONFIRM_MESSAGES.DELETE.GENERIC}
                confirmLabel="Delete"
                cancelLabel="Cancel"
                onConfirm={handleConfirmDelete}
            />

            {!activeTeamId ? (
                <EmptyTeamState />
            ) : (
                <>
                    <div className={isPlaceholderData ? 'opacity-50 transition-opacity' : ''}>
                        <IssuesTable
                            data={issues}
                            isPending={isViewDisabled}
                            isError={isError || (isLongTermView && (isIssueTypesError || isMissingLongTermIssueType))}
                            emptyMessage={emptyMessage}
                            onUpdate={openEdit}
                            onDelete={openDelete}
                            deletingIssueIds={deletingIssueIds}
                            onToggleArchive={handleArchiveToggle}
                            onOpenTodos={openIssueTodos}
                        />
                    </div>
                    <CustomPagination currentPage={page} totalPages={totalPages} onPageChange={setPage} />
                </>
            )}
        </div>
    );
}
