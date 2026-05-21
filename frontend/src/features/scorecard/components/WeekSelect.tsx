import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Spinner } from '@/components/ui/spinner';
import { UI_MESSAGES } from '@/constants/messages';
import { formatShortDate } from '@/utils/date';
import type { Week } from '../types/week';
import { checkCurrentWeek } from '../utils/weekUtils';

interface WeekSelectProps {
    weeks: Week[];
    value?: string;
    onValueChange: (value: string) => void;
    isPending: boolean;
    isError: boolean;
    disabled?: boolean;
}

export function WeekSelect({ weeks, value, onValueChange, isPending, isError, disabled }: WeekSelectProps) {
    if (isError) {
        return <span className="text-destructive">{UI_MESSAGES.WEEK.LOAD_ERROR}</span>;
    }

    const isSelectDisabled = (disabled ?? weeks.length === 0) || isPending;

    return (
        <Select value={value} onValueChange={onValueChange} disabled={isSelectDisabled}>
            <SelectTrigger className="w-[240px] bg-background">
                {isPending ? (
                    <div className="flex items-center gap-2 text-muted-foreground">
                        <Spinner className="h-4 w-4" />
                        <span>{UI_MESSAGES.COMMON.LOADING}</span>
                    </div>
                ) : (
                    <SelectValue placeholder="Select week" />
                )}
            </SelectTrigger>

            <SelectContent position="popper">
                {weeks.map((week, index) => {
                    const fullLabel = `Week ${weeks.length - index} (${formatShortDate(week.startDate)} - ${formatShortDate(week.endDate)})`;
                    const isCurrentWeek = checkCurrentWeek(week);
                    return (
                        <SelectItem key={week.id} value={week.id} className="data-[state=checked]:text-primary data-[state=checked]:bg-primary/10">
                            {fullLabel}
                            {isCurrentWeek && <span className="h-1.5 w-1.5 rounded-full shrink-0 bg-orange-500" />}
                        </SelectItem>
                    );
                })}
            </SelectContent>
        </Select>
    );
}
