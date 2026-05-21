import { SelectItem } from '@/components/ui/select';
import { cn } from '@/lib/utils';
import { formatShortDate } from '@/utils/date';
import type { Quarter } from '../types/rock';

interface QuarterSelectorProps {
    quarter: Quarter;
    isCurrent: boolean;
}

export function QuarterSelector({ quarter, isCurrent }: Readonly<QuarterSelectorProps>) {
    const startDate = formatShortDate(quarter.startDate);
    const endDate = formatShortDate(quarter.endDate);

    return (
        <SelectItem key={quarter.id} value={quarter.id}>
            <div className="flex w-full items-center justify-between">
                <span>
                    {quarter.name} ({startDate} - {endDate})
                </span>
                {isCurrent && (
                    <span
                        className={cn(
                            'ml-2 inline-flex items-center gap-1 rounded px-1.5 py-0.5 text-[10px] font-semibold uppercase tracking-wider ring-1 ring-inset',
                            'bg-primary/10 text-primary ring-primary/20',
                        )}
                    >
                        <span className="h-1.5 w-1.5 rounded-full bg-primary" />
                        Current
                    </span>
                )}
            </div>
        </SelectItem>
    );
}
