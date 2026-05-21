import type { TrendDataPointResponse } from '../types/trends';
import type { MetricUnit } from '../types/metric';
import { formatShortDate } from '@/utils/date';

export type ChartData = {
    weekLabel: string;
    value: number | null;
    isGoalMet?: boolean;
    ryg?: 'red' | 'yellow' | 'green';
    raw?: string;
};
export const mapTrendToChartData = (values: TrendDataPointResponse[], unit: MetricUnit): ChartData[] => {
    return values.map((item) => {
        const raw = item.metricValue?.value?.trim().toUpperCase();

        let value: number | null = null;
        let ryg: ChartData['ryg'];

        if (unit === 'RYG_STATUS') {
            if (raw) {
                ryg = raw.toLowerCase() as 'red' | 'yellow' | 'green';
                value = 1;
            }
        } else if (unit === 'YES_NO') {
            if (raw === 'YES' || raw === 'NO') {
                value = 1;
            } else {
                value = null;
            }
        } else if (raw) {
            const parsed = Number(raw);
            value = Number.isFinite(parsed) ? parsed : null;
        }

        return {
            weekLabel: formatShortDate(item.week.startDate),
            value,
            isGoalMet: item.metricValue?.isGoalMet,
            ryg,
            raw,
        };
    });
};
