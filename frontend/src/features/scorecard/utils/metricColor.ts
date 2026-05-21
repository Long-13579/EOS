import { METRIC_UNIT, type Metric, type MetricUnit } from '../types/metric';
import type { ChartData } from '../mappers/mapTrendMetric';

export const getTrendColor = (data: ChartData, unit: MetricUnit) => {
    if (data.value === null || !Number.isFinite(data.value)) {
        return 'transparent';
    }

    if (unit === METRIC_UNIT.RYG_STATUS) {
        switch (data.ryg) {
            case 'green':
                return 'var(--status-green)';
            case 'yellow':
                return 'var(--status-yellow)';
            case 'red':
                return 'var(--status-red)';
            default:
                return 'oklch(from var(--muted-foreground) l c h / 0.15)';
        }
    }

    if (data.isGoalMet === true) {
        return 'var(--status-green)';
    }
    if (data.isGoalMet === false) {
        return 'var(--status-red)';
    }

    return 'oklch(from var(--muted-foreground) l c h / 0.15)';
};

export const getMetricColor = (metric: Metric) => {
    if (metric.unit === METRIC_UNIT.RYG_STATUS) {
        const value = metric.currentValue?.value?.toUpperCase();

        if (value === 'GREEN') {
            return 'text-[var(--status-green-text)]';
        }
        if (value === 'YELLOW') {
            return 'text-[var(--status-yellow-text)]';
        }
        if (value === 'RED') {
            return 'text-[var(--status-red-text)]';
        }

        return 'text-muted-foreground';
    }

    if (metric.currentValue?.isGoalMet === true) {
        return 'text-[var(--status-green-text)]';
    }

    if (metric.currentValue?.isGoalMet === false) {
        return 'text-[var(--status-red-text)]';
    }

    return 'text-muted-foreground';
};
