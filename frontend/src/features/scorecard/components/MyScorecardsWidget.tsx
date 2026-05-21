import { useMemo, useState } from 'react';
import { ERROR_MESSAGES, UI_MESSAGES } from '@/constants/messages';
import { MetricsTable } from './MetricsTable';
import { WeekSelect } from './WeekSelect';
import { useWeeks } from '../hooks/useWeeks';
import { useMyMetrics } from '../hooks/useMyMetrics';
import { checkCurrentWeek } from '../utils/weekUtils';
import { useMetricDialog } from '../hooks/useMetricDialog';
import { MetricDialog } from './MetricDialog';

export function MyScorecardsWidget() {
    const { data: weeks = [], isPending: isWeeksPending, isError: isWeeksError } = useWeeks();
    const [selectedWeek, setSelectedWeek] = useState<string | undefined>();

    const { isMetricDialogOpen, setMetricDialogOpen, editingMetric, openUpdate, handleSubmit } = useMetricDialog();

    const defaultWeekId = useMemo(() => {
        const currentWeek = weeks.find(checkCurrentWeek);
        return currentWeek?.id ?? weeks[0]?.id;
    }, [weeks]);

    const selectedWeekId = selectedWeek ?? defaultWeekId;
    const { data: metrics, isPending: isMetricsPending, isError: isMetricsError } = useMyMetrics({ weekId: selectedWeekId });

    const isEditingCurrentWeek = weeks.some((week) => week.id === selectedWeekId && checkCurrentWeek(week));

    const emptyMessage = !isWeeksPending && weeks.length === 0 ? UI_MESSAGES.WEEK.NOT_FOUND : ERROR_MESSAGES.METRIC.NOT_FOUND;

    return (
        <div className="rounded-xl border bg-card shadow-sm p-5 space-y-4">
            <MetricDialog isOpen={isMetricDialogOpen} onOpenChange={setMetricDialogOpen} onSubmit={handleSubmit} editingMetric={editingMetric} />

            <div className="flex items-center justify-between">
                <h2 className="text-lg font-semibold">My Scorecards</h2>

                <WeekSelect weeks={weeks} value={selectedWeekId} onValueChange={setSelectedWeek} isPending={isWeeksPending} isError={isWeeksError} />
            </div>

            <div className="py-4">
                <MetricsTable
                    data={metrics}
                    emptyMessage={emptyMessage}
                    isPending={isMetricsPending}
                    isError={isMetricsError}
                    isEditable={isEditingCurrentWeek}
                    isDashboardView={true}
                    onUpdate={openUpdate}
                />
            </div>
        </div>
    );
}
