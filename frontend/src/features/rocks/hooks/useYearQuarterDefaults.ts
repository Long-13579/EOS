import { useMemo } from 'react';
import type { Year, Quarter } from '../types/rock';
import { useYears } from './useYears';
import { useQuarters } from './useQuarters';

interface UseYearQuarterDefaultsReturn {
    years: Year[];
    quarters: Quarter[];
    loadingYears: boolean;
    loadingQuarters: boolean;
    errorYears: boolean;
    errorQuarters: boolean;
    currentYearId?: string;
    currentQuarterId?: string;
    defaultYearId?: string;
    defaultQuarterId?: string;
    currentYear?: Year;
    currentQuarter?: Quarter;
}

export function useYearQuarterDefaults(): UseYearQuarterDefaultsReturn {
    const { data: years = [], isPending: loadingYears, isError: errorYears } = useYears();
    const { data: quarters = [], isPending: loadingQuarters, isError: errorQuarters } = useQuarters();
    const currentYear = useMemo(() => years.find((y) => y.isCurrent), [years]);
    const currentQuarter = useMemo(() => quarters.find((q) => q.isCurrent), [quarters]);

    const currentYearId = useMemo(() => currentYear?.id, [currentYear]);
    const currentQuarterId = useMemo(() => currentQuarter?.id, [currentQuarter]);

    const defaultYearId = useMemo(() => currentYearId ?? years[0]?.id, [currentYearId, years]);
    const defaultQuarterId = useMemo(() => currentQuarterId ?? quarters[0]?.id, [currentQuarterId, quarters]);

    return {
        years,
        quarters,
        loadingYears,
        loadingQuarters,
        errorYears,
        errorQuarters,
        currentYearId,
        currentQuarterId,
        defaultYearId,
        defaultQuarterId,
        currentYear,
        currentQuarter,
    };
}
