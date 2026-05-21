import { cn } from '@/lib/utils';
import { ROCK_STATUS, type RockStatus } from '../types/rock';
import { CheckCircle2, XCircle, CheckSquare, PauseCircle } from 'lucide-react';

const STATUS_CONFIG: Record<
    RockStatus,
    {
        label: string;
        icon: React.ComponentType<{ className?: string }>;
        activeClass: string;
    }
> = {
    [ROCK_STATUS.ON_TRACK]: {
        label: 'On Track',
        icon: CheckCircle2,
        activeClass: 'bg-emerald-500/15 text-emerald-600 dark:text-emerald-400 border-emerald-500/30',
    },
    [ROCK_STATUS.OFF_TRACK]: {
        label: 'Off Track',
        icon: XCircle,
        activeClass: 'bg-destructive/15 text-destructive border-destructive/30',
    },
    [ROCK_STATUS.COMPLETED]: {
        label: 'Completed',
        icon: CheckSquare,
        activeClass: 'bg-primary/15 text-primary border-primary/30',
    },
    [ROCK_STATUS.DEFERRED]: {
        label: 'Deferred',
        icon: PauseCircle,
        activeClass: 'bg-status-defer text-status-defer-foreground border border-status-defer/110',
    },
};

interface StatusButtonProps {
    status: RockStatus;
    currentStatus: RockStatus;
    onClick?: (status: RockStatus) => void;
    disabled?: boolean;
}

export function StatusButton({ status, currentStatus, onClick, disabled }: StatusButtonProps) {
    const config = STATUS_CONFIG[status];
    const Icon = config.icon;

    const isActive = status === currentStatus;

    return (
        <button
            disabled={disabled}
            onClick={() => onClick?.(status)}
            className={cn(
                'flex items-center gap-2 px-3 py-1.5 rounded-lg border text-sm font-medium transition-colors',
                isActive ? config.activeClass : 'bg-background text-muted-foreground border-border hover:bg-accent',
            )}
        >
            <Icon className="w-4 h-4" />
            {config.label}
        </button>
    );
}
