import { useState } from 'react';
import { Checkbox } from '@/components/ui/checkbox';
import { PageHeaderGroup } from '@/components/shared/PageHeaderGroup';
import { QueryState } from '@/components/shared/QueryState';
import { CustomPagination } from '@/components/shared/CustomPagination';
import { ConfirmationDialog } from '@/components/shared/ConfirmationDialog';
import { EmptyTeamState } from '@/components/shared/EmptyTeamState';
import { DEFAULT_LIMIT } from '@/types/pagination';
import { CONFIRM_MESSAGES, ERROR_MESSAGES } from '@/constants/messages';
import { useHeadlines, HeadlinesList, useCreateHeadline, HeadlineCreateForm, useDeleteHeadline, useArchiveHeadline } from '@/features/headlines';
import { useActiveTeamId } from '@/hooks/useActiveTeamId';

export function Headlines() {
    const activeTeamId = useActiveTeamId();
    const [page, setPage] = useState(1);
    const [showArchived, setShowArchived] = useState(false);

    const {
        data: headlinesResponse,
        isPending,
        isError,
        isPlaceholderData,
    } = useHeadlines({
        page,
        limit: DEFAULT_LIMIT,
        teamId: activeTeamId!,
        isArchived: showArchived,
    });

    const headlines = headlinesResponse?.data ?? [];
    const totalPages = headlinesResponse?.pagination.totalPages ?? 0;
    const emptyMessage = showArchived ? ERROR_MESSAGES.HEADLINE.ARCHIVED_NOT_FOUND : ERROR_MESSAGES.HEADLINE.NOT_FOUND;

    const { handleSubmit } = useCreateHeadline({ activeTeamId, onCreated: () => setPage(1) });
    const { isDeleteDialogOpen, setDeleteDialogOpen, deletingHeadline, deletingHeadlineIds, handleOpenDelete, handleConfirmDelete } =
        useDeleteHeadline();
    const { handleArchiveToggle, archivingHeadlineIds } = useArchiveHeadline();

    const toggleShowArchived = () => {
        setShowArchived(!showArchived);
        setPage(1);
    };

    return (
        <div className="flex flex-col gap-6">
            <PageHeaderGroup title="Headlines" description="Company updates and announcements." />

            {activeTeamId && (
                <div className="flex flex-row items-start gap-6 w-full py-2">
                    <div className="flex-1">
                        <HeadlineCreateForm onSubmit={handleSubmit} />
                    </div>

                    <div className="shrink-0 pt-2">
                        <label
                            htmlFor="show-archived"
                            className={`flex items-center gap-2 text-sm font-medium text-slate-600 transition-opacity ${
                                isPending ? 'cursor-not-allowed opacity-50' : 'cursor-pointer hover:text-slate-900'
                            }`}
                        >
                            <Checkbox id="show-archived" checked={showArchived} onCheckedChange={toggleShowArchived} disabled={isPending} />
                            <span className="whitespace-nowrap">Show archived</span>
                        </label>
                    </div>
                </div>
            )}

            <ConfirmationDialog
                isOpen={isDeleteDialogOpen}
                onOpenChange={setDeleteDialogOpen}
                title="Delete this headline?"
                description={deletingHeadline ? CONFIRM_MESSAGES.DELETE.CONFIRM_ITEM(deletingHeadline.title) : CONFIRM_MESSAGES.DELETE.GENERIC}
                confirmLabel="Delete"
                cancelLabel="Cancel"
                onConfirm={handleConfirmDelete}
            />

            {!activeTeamId ? (
                <EmptyTeamState />
            ) : (
                <QueryState
                    key={activeTeamId}
                    isPending={isPending && !isPlaceholderData}
                    isError={isError}
                    isEmpty={headlines.length === 0}
                    errorMessage={ERROR_MESSAGES.HEADLINE.LOAD_FAILED}
                    emptyMessage={emptyMessage}
                >
                    <div className={isPlaceholderData ? 'opacity-50 transition-opacity' : ''}>
                        <HeadlinesList
                            data={headlines}
                            onDelete={handleOpenDelete}
                            onArchive={handleArchiveToggle}
                            deletingHeadlineIds={deletingHeadlineIds}
                            archivingHeadlineIds={archivingHeadlineIds}
                        />

                        <CustomPagination currentPage={page} totalPages={totalPages} onPageChange={setPage} />
                    </div>
                </QueryState>
            )}
        </div>
    );
}
