import { cn } from '@/lib/utils';
import { ROCK_STATUS, type RockStatus } from '../types/rock';

const STATUS_CONFIG: Record<RockStatus, { label: string; className: string }> = {
    [ROCK_STATUS.ON_TRACK]: {
        label: 'On Track',
        className: 'bg-emerald-500/15 text-emerald-600 dark:text-emerald-400',
    },
    [ROCK_STATUS.OFF_TRACK]: {
        label: 'Off Track',
        className: 'bg-destructive/15 text-destructive',
    },
    [ROCK_STATUS.COMPLETED]: {
        label: 'Completed',
        className: 'bg-primary/15 text-primary',
    },
    [ROCK_STATUS.DEFERRED]: {
        label: 'Deferred',
        className: 'bg-status-defer text-status-defer-foreground',
    },
};

interface StatusBadgeProps {
    status: RockStatus;
    className?: string;
}

export function StatusBadge({ status, className }: StatusBadgeProps) {
    const config = STATUS_CONFIG[status];

    return <span className={cn('px-3 py-1 text-xs font-medium rounded-full', config?.className, className)}>{config?.label ?? status}</span>;
}
