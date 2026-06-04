import { useMemo, useState } from 'react';
import { useActiveTeamId } from '@/hooks/useActiveTeamId';
import { Plus } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Tabs, TabsList, TabsTrigger, TabsContent } from '@/components/ui/tabs';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { useWeeks } from '@/features/scorecard/hooks/useWeeks';
import { useMetrics } from '@/features/scorecard/hooks/useMetrics';
import { useMetricDialog } from '@/features/scorecard/hooks/useMetricDialog';
import { useDeleteMetric } from '@/features/scorecard/hooks/useDeleteMetric';
import { MetricsTable } from '@/features/scorecard/components/MetricsTable';
import { MetricDialog } from '@/features/scorecard/components/MetricDialog';
import { WeekSelect } from '@/features/scorecard/components/WeekSelect';
import { TrendsTab } from '@/features/scorecard/components/TrendsTab';
import { ConfirmationDialog } from '@/components/shared/ConfirmationDialog';
import { CONFIRM_MESSAGES } from '@/constants/messages';

interface ScorecardSessionProps {
    weekStartDate: string;
    meetingStatus: string;
}

export function ScorecardSession({ weekStartDate, meetingStatus }: ScorecardSessionProps) {
    const teamId = useActiveTeamId();
    const { data: weeks = [], isPending: isWeeksPending, isError: isWeeksError } = useWeeks();
    const [selectedWeekId, setSelectedWeekId] = useState<string | undefined>();
    const [activeTab, setActiveTab] = useState('metrics');
    const { isMetricDialogOpen, setMetricDialogOpen, editingMetric, openCreate, openUpdate, handleSubmit } = useMetricDialog();

    const { isDeleteDialogOpen, setDeleteDialogOpen, openDelete, handleConfirmDelete, deletingMetric } = useDeleteMetric();

    const weekId = useMemo(() => {
        if (!weeks.length || !weekStartDate) return undefined;
        const match = weeks.find((w) => w.startDate <= weekStartDate && w.endDate >= weekStartDate);
        return match?.id;
    }, [weeks, weekStartDate]);

    const effectiveWeekId = selectedWeekId ?? weekId;

    const {
        data: metrics,
        isPending,
        isError,
    } = useMetrics({
        teamId: teamId!,
        weekId: effectiveWeekId!,
    });

    const isEditable = meetingStatus === 'STARTED';

    return (
        <>
            <Card>
                <CardHeader>
                    <div className="flex items-center justify-between">
                        <CardTitle>Scorecard</CardTitle>
                        <div className="flex items-center gap-2">
                            <WeekSelect
                                weeks={weeks}
                                value={effectiveWeekId}
                                onValueChange={setSelectedWeekId}
                                isPending={isWeeksPending}
                                isError={isWeeksError}
                            />
                            <Button type="button" size="sm" onClick={openCreate}>
                                <Plus className="mr-1 h-4 w-4" />
                                Add Metric
                            </Button>
                        </div>
                    </div>
                </CardHeader>
                <CardContent>
                    <Tabs value={activeTab} onValueChange={setActiveTab} className="flex flex-col gap-6">
                        <TabsList className="grid w-[300px] grid-cols-2">
                            <TabsTrigger value="metrics">Weekly Metrics</TabsTrigger>
                            <TabsTrigger value="trends">Trends</TabsTrigger>
                        </TabsList>

                        <TabsContent value="metrics" className="m-0">
                            <MetricsTable
                                data={metrics}
                                isPending={isPending}
                                isError={isError}
                                emptyMessage="No metrics for this week."
                                onUpdate={openUpdate}
                                onDelete={openDelete}
                                isDashboardView
                                isEditable={isEditable}
                            />
                        </TabsContent>

                        <TabsContent value="trends" className="m-0">
                            <TrendsTab />
                        </TabsContent>
                    </Tabs>
                </CardContent>
            </Card>
            <MetricDialog isOpen={isMetricDialogOpen} onOpenChange={setMetricDialogOpen} onSubmit={handleSubmit} editingMetric={editingMetric} />

            <ConfirmationDialog
                isOpen={isDeleteDialogOpen}
                onOpenChange={setDeleteDialogOpen}
                title="Delete Metric"
                description={CONFIRM_MESSAGES.DELETE.CONFIRM_ITEM(deletingMetric?.name ?? '')}
                confirmLabel="Delete"
                onConfirm={handleConfirmDelete}
            />
        </>
    );
}
