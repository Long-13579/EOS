import { useState } from 'react';
import { Pencil, Trash2, Archive, ArchiveRestore } from 'lucide-react';
import { formatDate } from '@/utils/date';
import { getUserFullName } from '@/utils/user';
import type { Headline } from '../types/headline';
import { TableActions } from '@/components/shared/Table/TableActions';
import { HeadlineEditCell } from './HeadlineEditCell';
import { useUpdateHeadline } from '../hooks/useUpdateHeadline';
import { cn } from '@/lib/utils';

interface HeadlinesListProps {
    data: Headline[];
    onDelete: (headline: Headline) => void;
    onArchive: (headline: Headline) => void;
    deletingHeadlineIds?: string[];
    archivingHeadlineIds?: string[];
}

const getHeadlineActions = (
    headline: Headline,
    onStartEdit: (headline: Headline) => void,
    onArchive: (headline: Headline) => void,
    onDelete: (headline: Headline) => void,
) => {
    const isArchived = headline.isArchived;
    return [
        ...(!isArchived
            ? [
                  {
                      label: 'Edit headline',
                      icon: Pencil,
                      onClick: () => onStartEdit(headline),
                  },
              ]
            : []),
        {
            label: isArchived ? 'Unarchive headline' : 'Archive headline',
            icon: isArchived ? ArchiveRestore : Archive,
            onClick: () => onArchive(headline),
        },
        {
            label: 'Delete headline',
            icon: Trash2,
            variant: 'destructive' as const,
            onClick: () => onDelete(headline),
        },
    ];
};

export function HeadlinesList({ data, onDelete, onArchive, deletingHeadlineIds, archivingHeadlineIds }: HeadlinesListProps) {
    const [editingId, setEditingId] = useState<string | null>(null);

    const { handleSubmit: updateHeadline } = useUpdateHeadline({
        onUpdated: () => setEditingId(null),
    });

    const handleStartEdit = (headline: Headline) => {
        setEditingId(headline.id);
    };

    const handleCancelEdit = () => {
        setEditingId(null);
    };

    return (
        <div className="flex flex-col gap-3">
            {data.map((headline) => {
                const isEditing = headline.id === editingId;
                const isBeingDeleted = deletingHeadlineIds?.includes(headline.id);
                const isBeingArchived = archivingHeadlineIds?.includes(headline.id);
                const isDisabled = isBeingDeleted || isBeingArchived;
                const isArchived = headline.isArchived;

                return (
                    <div
                        key={headline.id}
                        className={`flex items-center justify-between rounded-md border bg-card px-4 py-3 ${isDisabled ? 'opacity-50 grayscale pointer-events-none' : ''}`}
                        aria-disabled={isDisabled}
                    >
                        {isEditing ? (
                            <HeadlineEditCell
                                headline={headline}
                                onCancelEdit={handleCancelEdit}
                                onUpdate={(id, data) => updateHeadline({ id, data })}
                            />
                        ) : (
                            <>
                                <div className={cn('flex flex-col gap-1 min-w-0 w-full flex-1 overflow-hidden', isArchived && 'opacity-60')}>
                                    <div className="flex items-center gap-2 min-w-0">
                                        <span className="font-medium line-clamp-2 break-all" title={headline.title}>
                                            {headline.title}
                                        </span>
                                    </div>

                                    <span className="text-sm text-muted-foreground">
                                        Posted by {headline.createdBy ? getUserFullName(headline.createdBy) : 'System'} on{' '}
                                        {formatDate(headline.createdAt)}
                                    </span>
                                </div>

                                <TableActions actions={getHeadlineActions(headline, handleStartEdit, onArchive, onDelete)} />
                            </>
                        )}
                    </div>
                );
            })}
        </div>
    );
}
