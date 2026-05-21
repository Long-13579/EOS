import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Spinner } from '@/components/ui/spinner';
import { UI_MESSAGES } from '@/constants/messages';
import type { Quarter, Year } from '../types/rock';
import { QuarterSelector } from './QuarterSelector';

interface YearQuarterSelectorProps {
    years: Year[];
    quarters: Quarter[];
    yearId?: string;
    quarterId?: string;
    currentYearId?: string;
    currentQuarterId?: string;
    onYearChange: (id: string) => void;
    onQuarterChange: (id: string) => void;
    loadingYears: boolean;
    loadingQuarters: boolean;
    errorYears: boolean;
    errorQuarters: boolean;
}

export function YearQuarterSelector({
    years,
    quarters,
    yearId,
    quarterId,
    currentYearId,
    currentQuarterId,
    onYearChange,
    onQuarterChange,
    loadingYears,
    loadingQuarters,
    errorYears,
    errorQuarters,
}: YearQuarterSelectorProps) {
    const isLoading = loadingYears || loadingQuarters;
    const isError = errorYears || errorQuarters;
    const isEmpty = !isLoading && !isError && (years.length === 0 || quarters.length === 0);
    const isDisabled = isLoading || isError || isEmpty;

    const selectedYearValue = years.find((y) => y.id === yearId)?.year;

    if (isEmpty) {
        return <div className="text-sm text-muted-foreground">{UI_MESSAGES.COMMON.EMPTY}</div>;
    }

    return (
        <div className="flex items-center gap-4">
            <div className="flex items-center gap-2">
                <span className="text-sm font-medium">{UI_MESSAGES.YEAR.LABEL}</span>

                {errorYears ? (
                    <span className="text-destructive">{UI_MESSAGES.YEAR.LOAD_ERROR}</span>
                ) : (
                    <Select
                        value={yearId}
                        onValueChange={(val) => {
                            onYearChange(val);
                        }}
                        disabled={isDisabled}
                    >
                        <SelectTrigger className="w-[140px]">
                            {isLoading ? (
                                <div className="flex items-center gap-2 text-muted-foreground">
                                    <Spinner className="size-3" />
                                    <span>{UI_MESSAGES.COMMON.LOADING}</span>
                                </div>
                            ) : (
                                <SelectValue placeholder={UI_MESSAGES.YEAR.LABEL}>{selectedYearValue}</SelectValue>
                            )}
                        </SelectTrigger>

                        <SelectContent position="popper" className="min-w-[--radix-select-trigger-width]">
                            {years.map((year) => (
                                <SelectItem key={year.id} value={year.id}>
                                    {year.year}
                                </SelectItem>
                            ))}
                        </SelectContent>
                    </Select>
                )}
            </div>

            <div className="flex items-center gap-2">
                <span className="text-sm font-medium">{UI_MESSAGES.QUARTER.LABEL}</span>

                {errorQuarters ? (
                    <span className="text-destructive">{UI_MESSAGES.QUARTER.LOAD_ERROR}</span>
                ) : (
                    <Select value={quarterId} onValueChange={onQuarterChange} disabled={isDisabled}>
                        <SelectTrigger className="w-[250px]">
                            {isLoading ? (
                                <div className="flex items-center gap-2 text-muted-foreground">
                                    <Spinner className="size-3" />
                                    <span>{UI_MESSAGES.COMMON.LOADING}</span>
                                </div>
                            ) : (
                                <SelectValue placeholder={UI_MESSAGES.QUARTER.LABEL} />
                            )}
                        </SelectTrigger>

                        <SelectContent position="popper" className="min-w-[--radix-select-trigger-width]">
                            {quarters.map((quarter) => {
                                const isCurrent = yearId === currentYearId && quarter.id === currentQuarterId;
                                return <QuarterSelector key={quarter.id} quarter={quarter} isCurrent={isCurrent} />;
                            })}
                        </SelectContent>
                    </Select>
                )}
            </div>
        </div>
    );
}
