import { format } from 'date-fns';
import { Calendar as CalendarIcon } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Calendar } from '@/components/ui/calendar';
import { Popover, PopoverContent, PopoverTrigger } from '@/components/ui/popover';
import type { Matcher } from 'react-day-picker';

interface DatePickerProps {
    id?: string;
    value?: Date | null;
    onChange?: (value: Date | null) => void;
    isDisabled?: boolean;
    disabledDays?: Matcher | Matcher[];
}

export function DatePicker({ value, onChange, id, isDisabled, disabledDays }: Readonly<DatePickerProps>) {
    return (
        <Popover>
            <PopoverTrigger asChild>
                <Button id={id} type="button" variant="outline" className="w-full justify-start text-left font-normal" disabled={isDisabled}>
                    <CalendarIcon className="mr-2 h-4 w-4 text-muted-foreground" />
                    {value ? format(value, 'PPP') : 'Pick a date'}
                </Button>
            </PopoverTrigger>

            <PopoverContent className="w-auto p-0">
                <Calendar
                    classNames={{
                        today: 'bg-primary/10 text-primary font-semibold rounded-md hover:[&>*:nth-child(1)]:bg-primary/20 hover:[&>*:nth-child(1)]:text-primary',
                    }}
                    mode="single"
                    captionLayout="dropdown"
                    startMonth={new Date(1900, 0)}
                    endMonth={new Date(new Date().getFullYear() + 100, 0)}
                    selected={value ?? undefined}
                    onSelect={(selected) => onChange?.(selected ?? null)}
                    disabled={disabledDays}
                    fixedWeeks
                    showOutsideDays
                />
            </PopoverContent>
        </Popover>
    );
}
