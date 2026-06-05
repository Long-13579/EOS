import { useState } from 'react';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Spinner } from '@/components/ui/spinner';
import { useIssueTypes } from '../hooks/useIssueTypes';
import type { IssueType } from '../types/issue';

interface IssueTypeInlineSelectProps {
    currentIssueType: IssueType | null;
    onSelect: (issueTypeId: string | null) => void;
    disabled?: boolean;
}

export function IssueTypeInlineSelect({ currentIssueType, onSelect, disabled }: IssueTypeInlineSelectProps) {
    const [open, setOpen] = useState(false);
    const { data, isPending } = useIssueTypes();
    const issueTypes = data ?? [];

    const currentValue = currentIssueType?.id ?? 'none';

    return (
        <Select
            open={open}
            onOpenChange={setOpen}
            value={currentValue}
            onValueChange={(value) => {
                if (value === currentValue) {
                    setOpen(false);
                    return;
                }
                onSelect(value === 'none' ? null : value);
            }}
            disabled={disabled || isPending}
        >
            <SelectTrigger
                className="h-7 border-none bg-transparent p-0 text-sm font-medium hover:bg-accent/50 shadow-none min-w-[3rem] [&>svg]:hidden"
                aria-label="Change issue type"
            >
                {isPending ? (
                    <div className="flex items-center gap-2 text-muted-foreground">
                        <Spinner className="size-3" />
                    </div>
                ) : (
                    <span>{currentIssueType?.name ?? '-'}</span>
                )}
            </SelectTrigger>
            <SelectContent position="popper" align="start" className="min-w-[140px]">
                <SelectItem value="none">-</SelectItem>
                {issueTypes.map((type) => (
                    <SelectItem key={type.id} value={type.id}>
                        {type.name}
                    </SelectItem>
                ))}
            </SelectContent>
        </Select>
    );
}
