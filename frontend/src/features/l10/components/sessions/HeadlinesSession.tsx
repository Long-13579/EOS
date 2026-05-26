import { useActiveTeamId } from '@/hooks/useActiveTeamId';
import { useHeadlines } from '@/features/headlines/hooks/useHeadlines';
import { useCreateHeadline } from '@/features/headlines/hooks/useCreateHeadline';
import { useDeleteHeadline } from '@/features/headlines/hooks/useDeleteHeadline';
import { useArchiveHeadline } from '@/features/headlines/hooks/useArchiveHeadline';
import { HeadlinesList } from '@/features/headlines/components/HeadlinesList';
import { HeadlineCreateForm } from '@/features/headlines/components/HeadlineCreateForm';
import { ConfirmationDialog } from '@/components/shared/ConfirmationDialog';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import type { GetHeadlinesParams } from '@/features/headlines/types/headline';

export function HeadlinesSession() {
    const teamId = useActiveTeamId();

    const { data: headlines } = useHeadlines({ teamId: teamId! } as GetHeadlinesParams);

    const { handleSubmit: createHeadline } = useCreateHeadline({ activeTeamId: teamId });
    const {
        isDeleteDialogOpen,
        setDeleteDialogOpen,
        deletingHeadline,
        deletingHeadlineIds,
        handleOpenDelete,
        handleConfirmDelete,
    } = useDeleteHeadline();
    const { handleArchiveToggle, archivingHeadlineIds } = useArchiveHeadline();

    return (
        <>
            <Card>
                <CardHeader>
                    <CardTitle>Headlines</CardTitle>
                </CardHeader>
                <CardContent className="space-y-4">
                    <HeadlineCreateForm onSubmit={createHeadline} />
                    <HeadlinesList
                        data={headlines?.data ?? []}
                        onDelete={handleOpenDelete}
                        onArchive={handleArchiveToggle}
                        deletingHeadlineIds={deletingHeadlineIds}
                        archivingHeadlineIds={archivingHeadlineIds}
                    />
                </CardContent>
            </Card>
            <ConfirmationDialog
                isOpen={isDeleteDialogOpen}
                onOpenChange={setDeleteDialogOpen}
                title="Delete Headline"
                description={`Are you sure you want to delete "${deletingHeadline?.title}"?`}
                confirmLabel="Delete"
                onConfirm={handleConfirmDelete}
            />
        </>
    );
}
