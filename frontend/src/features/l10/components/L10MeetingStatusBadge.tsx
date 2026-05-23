import { Clock, PlayCircle, CheckCircle2 } from 'lucide-react';
import type { L10MeetingStatus } from '../types/l10Meeting';

interface L10MeetingStatusBadgeProps {
    status: L10MeetingStatus;
}

const statusConfig = {
    SCHEDULED: {
        label: 'Scheduled',
        icon: Clock,
        className: 'bg-primary/10 text-primary',
    },
    STARTED: {
        label: 'In Progress',
        icon: PlayCircle,
        className: 'bg-amber-100 text-amber-700 dark:bg-amber-900/30 dark:text-amber-400',
    },
    FINISHED: {
        label: 'Finished',
        icon: CheckCircle2,
        className: 'bg-chart-1/15 text-chart-1',
    },
} satisfies Record<
    L10MeetingStatus,
    {
        label: string;
        icon: React.ComponentType<{ className?: string }>;
        className: string;
    }
>;

export function L10MeetingStatusBadge({ status }: L10MeetingStatusBadgeProps) {
    const config = statusConfig[status];
    const Icon = config.icon;

    return (
        <div className={`inline-flex items-center gap-1.5 rounded-md px-2.5 py-1 text-xs font-medium ${config.className}`}>
            <Icon className="h-3.5 w-3.5" />
            {config.label}
        </div>
    );
}
