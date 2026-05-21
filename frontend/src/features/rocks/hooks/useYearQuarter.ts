import { useYears } from '../hooks/useYears';
import { useQuarters } from '../hooks/useQuarters';

export const useYearQuarter = (yearId?: string, quarterId?: string) => {
    const { data: years = [], isPending: loadingYears, isError: errorYears } = useYears();
    const { data: quarters = [], isPending: loadingQuarters, isError: errorQuarters } = useQuarters();

    const currentYearId = years.find((y) => y.isCurrent)?.id;
    const currentQuarterId = quarters.find((q) => q.isCurrent)?.id;

    const effectiveYearId = yearId ?? currentYearId ?? years[0]?.id;
    const effectiveQuarterId = quarterId ?? currentQuarterId ?? quarters[0]?.id;

    return {
        years,
        quarters,
        loadingYears,
        loadingQuarters,
        errorYears,
        errorQuarters,
        currentYearId,
        currentQuarterId,
        effectiveYearId,
        effectiveQuarterId,
    };
};
