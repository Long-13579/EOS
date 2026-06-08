import { useState } from 'react';
import { CheckCircle2, Clock, CircleDashed } from 'lucide-react';
import { Select, SelectContent, SelectItem, SelectTrigger } from '@/components/ui/select';
import type { TodoStatus } from '@/features/todos/types/todo';

const statusOptions: { value: TodoStatus; label: string; icon: React.ComponentType<{ className?: string }>; className: string }[] = [
    {
        value: 'NOT_STARTED',
        label: 'Not Started',
        icon: CircleDashed,
        className: 'bg-muted text-muted-foreground',
    },
    {
        value: 'IN_PROGRESS',
        label: 'In Progress',
        icon: Clock,
        className: 'bg-primary/10 text-primary',
    },
    {
        value: 'COMPLETED',
        label: 'Completed',
        icon: CheckCircle2,
        className: 'bg-chart-1/15 text-chart-1',
    },
];

interface TodoStatusSelectProps {
    currentStatus: TodoStatus;
    onSelect: (status: TodoStatus) => void;
    disabled?: boolean;
}

export function TodoStatusSelect({ currentStatus, onSelect, disabled }: TodoStatusSelectProps) {
    const [open, setOpen] = useState(false);

    const current = statusOptions.find((o) => o.value === currentStatus)!;
    const CurrentIcon = current.icon;

    return (
        <Select
            open={open}
            onOpenChange={setOpen}
            value={currentStatus}
            onValueChange={(value) => {
                if (value === currentStatus) {
                    setOpen(false);
                    return;
                }
                onSelect(value as TodoStatus);
            }}
            disabled={disabled}
        >
            <SelectTrigger
                className="h-7 gap-1.5 border-none bg-transparent p-0 text-xs font-medium hover:bg-accent/50 shadow-none [&>svg]:hidden"
                aria-label="Change status"
            >
                <div className={`inline-flex items-center gap-1.5 rounded-md px-2 py-0.5 ${current.className}`}>
                    <CurrentIcon className="h-3.5 w-3.5" />
                    {current.label}
                </div>
            </SelectTrigger>
            <SelectContent position="popper" align="start" className="min-w-[140px]">
                {statusOptions.map((option) => {
                    const OptionIcon = option.icon;
                    return (
                        <SelectItem key={option.value} value={option.value} className="text-xs">
                            <div className={`inline-flex items-center gap-1.5 rounded-md px-2 py-0.5 ${option.className}`}>
                                <OptionIcon className="h-3.5 w-3.5" />
                                {option.label}
                            </div>
                        </SelectItem>
                    );
                })}
            </SelectContent>
        </Select>
    );
}
