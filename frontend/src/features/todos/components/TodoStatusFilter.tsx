import { Filter } from 'lucide-react';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { ALL_TODO_STATUSES, type TodoStatus } from '@/features/todos/types/todo';

type FilterOption = TodoStatus | typeof ALL_TODO_STATUSES;

interface TodoStatusFilterProps {
    value: FilterOption;
    onChange: (status: FilterOption) => void;
    isDisabled?: boolean;
}

const FILTER_OPTIONS: Array<{ value: FilterOption; label: string }> = [
    { value: 'ALL', label: 'All Statuses' },
    { value: 'NOT_STARTED', label: 'Not Started' },
    { value: 'IN_PROGRESS', label: 'In Progress' },
    { value: 'COMPLETED', label: 'Completed' },
];

export function TodoStatusFilter({ value, onChange, isDisabled = false }: TodoStatusFilterProps) {
    return (
        <div className="flex items-center gap-2">
            <Filter className="h-4 w-4 text-muted-foreground" aria-hidden="true" />
            <Select value={value} onValueChange={(newValue) => onChange(newValue as FilterOption)} disabled={isDisabled} aria-disabled={isDisabled}>
                <SelectTrigger className="w-[180px]" aria-label="Filter todos by status">
                    <SelectValue />
                </SelectTrigger>
                <SelectContent position="popper">
                    {FILTER_OPTIONS.map((option) => (
                        <SelectItem key={option.value} value={option.value}>
                            {option.label}
                        </SelectItem>
                    ))}
                </SelectContent>
            </Select>
        </div>
    );
}
