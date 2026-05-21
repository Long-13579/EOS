import { useState } from 'react';
import { ChevronDown, ChevronUp, ClipboardList, Pencil, Archive, ArchiveRestore } from 'lucide-react';
import type { Rock, RockStatus } from '../types/rock';
import { TableActions } from '@/components/shared/Table';
import { formatDate } from '@/utils/date';
import { useIsLeadershipTeam } from '@/hooks/useIsLeadershipTeam';
import { StatusBadge } from './StatusBadge';
import { getUserFullName } from '@/utils/user';
import { StatusButton } from './StatusButton';
import { ROCK_STATUS } from '../types/rock';
import { useUpdateRockStatus } from '../hooks/useUpdateRockStatus';
import { cn } from '@/lib/utils';
import { ROCK_CATEGORY } from '../types/rock';

interface RockListItemProps {
    rock: Rock;
    onUpdate?: (rock: Rock) => void;
    onArchive?: (rock: Rock) => void;
    showTeam?: boolean;
    isArchiving?: boolean;
}

const getRockActions = (
    rock: Rock,
    onUpdate?: (rock: Rock) => void,
    onArchive?: (rock: Rock) => void,
    isDashboard?: boolean,
    isLeadershipTeam: boolean = false,
) => {
    const isArchived = rock.isArchived;
    const canModifyRock = isLeadershipTeam || rock.category !== ROCK_CATEGORY.COMPANY;
    const canEditRock = !isArchived && canModifyRock;

    const actions = [];

    if (canEditRock) {
        actions.push({
            label: 'Edit Rock',
            icon: Pencil,
            onClick: () => onUpdate?.(rock),
        });
    }

    if (!isDashboard && canModifyRock) {
        actions.push({
            label: isArchived ? 'Unarchive Rock' : 'Archive Rock',
            icon: isArchived ? ArchiveRestore : Archive,
            onClick: () => onArchive?.(rock),
        });
    }

    return actions;
};

export function RockListItem({ rock, onUpdate, onArchive, showTeam, isArchiving }: RockListItemProps) {
    const [isExpanded, setIsExpanded] = useState(false);
    const isLeadershipTeam = useIsLeadershipTeam();

    const statuses = Object.values(ROCK_STATUS);

    const { updateStatus, isPending, pendingStatus } = useUpdateRockStatus();

    const handleStatusClick = (status: RockStatus) => {
        if (status !== rock.status) {
            updateStatus({ id: rock.id, status });
        }
    };

    const ownerName = rock.owner ? getUserFullName(rock.owner) : 'Unassigned';
    const archivedClass = rock.isArchived ? 'opacity-60' : '';

    return (
        <div
            className={cn(
                'w-full max-w-full min-w-0 border border-border rounded-xl bg-card text-card-foreground overflow-hidden shadow-sm transition-all',
                isArchiving && 'opacity-50 pointer-events-none animate-pulse',
            )}
        >
            <div
                className={cn(
                    'flex items-center justify-between gap-4 p-4 cursor-pointer hover:bg-accent hover:text-accent-foreground transition-colors',
                    archivedClass,
                )}
                onClick={() => setIsExpanded(!isExpanded)}
            >
                <div className="flex min-w-0 flex-1 items-center gap-3">
                    <ClipboardList className="h-5 w-5 shrink-0 text-primary" />
                    <span className="min-w-0 flex-1 line-clamp-2 wrap-break-word font-semibold leading-snug">{rock.title}</span>
                </div>

                <div className="flex shrink-0 items-center gap-3">
                    {showTeam && (
                        <span className="shrink-0 rounded-md border bg-muted px-2 py-1 text-xs text-muted-foreground whitespace-nowrap">
                            {rock.team?.name}
                        </span>
                    )}

                    <StatusBadge status={rock.status} className="shrink-0 whitespace-nowrap" />

                    {isExpanded ? (
                        <ChevronUp className="h-5 w-5 shrink-0 text-muted-foreground" />
                    ) : (
                        <ChevronDown className="h-5 w-5 shrink-0 text-muted-foreground" />
                    )}
                </div>
            </div>

            {isExpanded && (
                <div className="p-5 border-t border-border bg-muted/20">
                    <div className={cn(archivedClass)}>
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-6">
                            <div>
                                <p className="text-sm text-muted-foreground mb-1">Owner</p>
                                <p className="font-medium">{ownerName}</p>
                            </div>

                            <div>
                                <p className="text-sm text-muted-foreground mb-1">Due Date</p>
                                <p className="font-medium">{formatDate(rock.dueDate)}</p>
                            </div>
                        </div>

                        <div className="mb-6">
                            <p className="text-sm text-muted-foreground mb-1">Description</p>
                            <p className="text-sm break-words line-clamp-3" title={rock.description}>
                                {rock.description || 'No description provided.'}
                            </p>
                        </div>
                    </div>

                    <div className="flex flex-col sm:flex-row items-center justify-between pt-4 border-t border-border gap-4">
                        <div className={cn('flex items-center gap-2', archivedClass)}>
                            {statuses.map((status) => (
                                <StatusButton
                                    key={status}
                                    status={status as RockStatus}
                                    currentStatus={rock.status}
                                    onClick={handleStatusClick}
                                    disabled={rock.isArchived || (isPending && pendingStatus === status)}
                                />
                            ))}
                        </div>

                        <div className="flex items-center gap-2 w-full sm:w-auto">
                            <TableActions actions={getRockActions(rock, onUpdate, onArchive, showTeam, isLeadershipTeam)} />
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}
