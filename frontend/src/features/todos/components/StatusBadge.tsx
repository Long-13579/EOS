import { CheckCircle2, Clock, CircleDashed } from 'lucide-react';
import type { TodoStatus } from '@/features/todos/types/todo';

interface StatusBadgeProps {
    status: TodoStatus;
}

const statusConfig = {
    NOT_STARTED: {
        label: 'Not Started',
        icon: CircleDashed,
        className: 'bg-muted text-muted-foreground',
    },
    IN_PROGRESS: {
        label: 'In Progress',
        icon: Clock,
        className: 'bg-primary/10 text-primary',
    },
    COMPLETED: {
        label: 'Completed',
        icon: CheckCircle2,
        className: 'bg-chart-1/15 text-chart-1',
    },
} satisfies Record<
    TodoStatus,
    {
        label: string;
        icon: React.ComponentType<{ className?: string }>;
        className: string;
    }
>;

export function StatusBadge({ status }: StatusBadgeProps) {
    const config = statusConfig[status];
    const Icon = config.icon;

    return (
        <div className={`inline-flex items-center gap-2 rounded-md px-2.5 py-1 text-xs font-medium ${config.className}`}>
            <Icon className="h-3.5 w-3.5" />
            {config.label}
        </div>
    );
}
