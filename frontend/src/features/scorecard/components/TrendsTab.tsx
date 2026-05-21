import { useActiveTeamId } from '@/hooks/useActiveTeamId';
import { useTrendMetrics } from '../hooks/useTrendMetrics';
import { mapTrendToChartData } from '../mappers/mapTrendMetric';
import { TrendMetricChart } from './TrendMetricChart';
import { getUserFullName } from '@/utils/user';
import { QueryState } from '@/components/shared/QueryState';
import { ERROR_MESSAGES } from '@/constants/messages';
import { mapOperatorFromApi } from '../mappers/mapMetric';
import { formatMetricValue } from '../utils/metricFormat';

export function TrendsTab() {
    const activeTeamId = useActiveTeamId();

    const { data, isPending, isError } = useTrendMetrics(activeTeamId!);

    return (
        <QueryState
            isPending={isPending}
            isError={isError}
            isEmpty={!data?.items.length}
            emptyMessage={ERROR_MESSAGES.METRIC.TREND_NOT_FOUND}
            errorMessage={ERROR_MESSAGES.METRIC.TREND_LOAD_FAILED}
        >
            <div className="flex flex-col gap-6">
                {data?.items.map((metric) => {
                    const chartData = mapTrendToChartData(metric.values, metric.unit);
                    const operator = mapOperatorFromApi(metric.operator) || '';
                    const goalValue = formatMetricValue(metric.goal, metric.unit);
                    const goalText = `Goal: ${operator} ${goalValue}`;

                    return (
                        <div key={metric.id} className="border rounded-lg p-4">
                            <div className="flex justify-between items-center mb-3 gap-4">
                                <h3 className="font-semibold truncate flex-1 w-0.5" title={metric.name}>
                                    {metric.name}
                                </h3>
                                <span className="text-right text-xs text-muted-foreground shrink-0 truncate w-1/4" title={goalText}>
                                    {goalText}
                                </span>
                            </div>

                            <TrendMetricChart data={chartData} unit={metric.unit} />

                            <div className="text-xs text-muted-foreground mt-6">Owner: {getUserFullName(metric.owner) ?? 'N/A'}</div>
                        </div>
                    );
                })}
            </div>
        </QueryState>
    );
}
