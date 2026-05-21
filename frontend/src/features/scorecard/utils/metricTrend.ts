import { formatNumber } from '@/utils/number';
import type { ChartData } from '../mappers/mapTrendMetric';
import { type MetricUnit, METRIC_UNIT } from '../types/metric';

const MILLION = 1_000_000;
const THOUSAND = 1_000;

export const formatCompactNumber = (num: number) => {
    if (num >= MILLION) {
        const value = num / MILLION;
        return `${value % 1 === 0 ? value.toFixed(0) : value.toFixed(1)}M`;
    }

    if (num >= THOUSAND) {
        const value = num / THOUSAND;
        return `${value % 1 === 0 ? value.toFixed(0) : value.toFixed(1)}K`;
    }

    return formatNumber(num);
};

export const formatMetricDisplay = (data: ChartData, unit: MetricUnit): string => {
    if (data.value == null) {
        return 'N/A';
    }

    if (unit === METRIC_UNIT.RYG_STATUS) {
        return data.ryg ? data.ryg.charAt(0).toUpperCase() + data.ryg.slice(1) : 'N/A';
    }

    if (unit === METRIC_UNIT.YES_NO) {
        return data.raw === 'YES' ? 'Yes' : data.raw === 'NO' ? 'No' : 'N/A';
    }

    return formatNumber(data.value);
};

export const formatShortMetric = (item: ChartData, unit: MetricUnit) => {
    const val = item.value;

    if (val == null) {
        return 'N/A';
    }

    switch (unit) {
        case METRIC_UNIT.NUMBER:
        case METRIC_UNIT.CURRENCY:
            return formatCompactNumber(val);

        case METRIC_UNIT.PERCENTAGE:
            return `${val % 1 === 0 ? val : val.toFixed(1)}%`;

        default:
            return formatMetricDisplay(item, unit);
    }
};
