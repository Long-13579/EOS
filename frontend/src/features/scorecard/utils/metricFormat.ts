import { parseNumber, formatNumber } from '@/utils/number';
import type { MetricUnit } from '../types/metric';

export const formatMetricValue = (value: string | null, unit: MetricUnit) => {
    if (!value) {
        return 'N/A';
    }

    const normalized = value.trim().toUpperCase();
    const num = parseNumber(value);
    const isNumeric = num !== null;

    switch (unit) {
        case 'NUMBER':
            return isNumeric ? formatNumber(num) : value;

        case 'CURRENCY':
            return isNumeric ? `$${formatNumber(num)}` : `$${value}`;

        case 'PERCENTAGE':
            return isNumeric ? `${formatNumber(num)}%` : `${value}%`;

        case 'YES_NO':
            if (normalized === 'YES') {
                return 'Yes';
            }
            if (normalized === 'NO') {
                return 'No';
            }
            return value;

        case 'RYG_STATUS':
            if (normalized === 'GREEN') {
                return 'Green';
            }
            if (normalized === 'YELLOW') {
                return 'Yellow';
            }
            if (normalized === 'RED') {
                return 'Red';
            }
            return value;

        default:
            return value;
    }
};
