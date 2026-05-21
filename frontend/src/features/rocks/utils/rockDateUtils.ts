import { formatShortDate } from '@/utils/date';
import type { Quarter } from '../types/rock';

export const getQuarterEndYear = (quarter: Quarter, year: string): string => {
    return quarter.endDate < quarter.startDate ? String(Number(year) + 1) : year;
};

export const formatQuarterRangeHint = (quarter: Quarter, year: string) => {
    const startDate = formatShortDate(quarter.startDate);
    const endDate = formatShortDate(quarter.endDate);
    const endYear = getQuarterEndYear(quarter, year);
    return `${quarter.name} (${startDate} ${year} - ${endDate} ${endYear})`;
};

export const createSafeDate = (monthDayString: string, yearString: string): Date => {
    // Assuming monthDayString is "MM-DD" (e.g. "03-31")
    const [month, day] = monthDayString.split('-');

    // Note: JS months are 0-indexed (0 = Jan, 1 = Feb), so we subtract 1
    return new Date(Number(yearString), Number(month) - 1, Number(day));
};

export const isDueDateInQuarterRange = (dueDate: Date, quarter: Quarter | undefined, year: string | undefined) => {
    if (!dueDate || !quarter || !year) {
        return false;
    }

    const due = new Date(dueDate);
    const start = createSafeDate(quarter.startDate, year);

    const endYear = getQuarterEndYear(quarter, year);
    const end = createSafeDate(quarter.endDate, endYear);

    due.setHours(0, 0, 0, 0);
    start.setHours(0, 0, 0, 0);
    end.setHours(23, 59, 59, 999);

    return due >= start && due <= end;
};

export const getDueDateQuarterValidationMessage = (
    dueDate: Date | null | undefined,
    quarter: Quarter | undefined,
    year: string | number | null | undefined,
): string | null => {
    if (!dueDate || !quarter || !year) {
        return null;
    }

    const yearString = String(year);
    const isValid = isDueDateInQuarterRange(dueDate, quarter, yearString);

    if (isValid) {
        return null;
    }

    return `Due date must be within ${formatQuarterRangeHint(quarter, yearString)}.`;
};
