import { useMemo } from 'react';
import { useActiveTeamId } from '@/hooks/useActiveTeamId';
import { Plus } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { useWeeks } from '@/features/scorecard/hooks/useWeeks';
import { useMetrics } from '@/features/scorecard/hooks/useMetrics';
import { useMetricDialog } from '@/features/scorecard/hooks/useMetricDialog';
import { MetricsTable } from '@/features/scorecard/components/MetricsTable';
import { MetricDialog } from '@/features/scorecard/components/MetricDialog';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';

interface ScorecardSessionProps {
    weekStartDate: string;
}

export function ScorecardSession({ weekStartDate }: ScorecardSessionProps) {
    const teamId = useActiveTeamId();
    const { data: weeks } = useWeeks();
    const weekId = useMemo(() => {
        if (!weeks || !weekStartDate) return undefined;
        const match = weeks.find((w) => w.startDate <= weekStartDate && w.endDate >= weekStartDate);
        return match?.id;
    }, [weeks, weekStartDate]);

    const { isMetricDialogOpen, setMetricDialogOpen, editingMetric, openCreate, openUpdate, handleSubmit } = useMetricDialog();

    const { data: metrics, isPending, isError } = useMetrics({
        teamId: teamId!,
        weekId: weekId!,
    });

    return (
        <>
            <Card>
                <CardHeader>
                    <div className="flex items-center justify-between">
                        <CardTitle>Scorecard</CardTitle>
                        <Button type="button" size="sm" onClick={openCreate}>
                            <Plus className="mr-1 h-4 w-4" />
                            Add Metric
                        </Button>
                    </div>
                </CardHeader>
                <CardContent>
                    <MetricsTable
                        data={metrics}
                        isPending={isPending}
                        isError={isError}
                        emptyMessage="No metrics for this week."
                        onUpdate={openUpdate}
                        isDashboardView
                    />
                </CardContent>
            </Card>
            <MetricDialog isOpen={isMetricDialogOpen} onOpenChange={setMetricDialogOpen} onSubmit={handleSubmit} editingMetric={editingMetric} />
        </>
    );
}
