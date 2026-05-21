import { useState } from 'react';
import { Button } from '@/components/ui/button';
import { Plus } from 'lucide-react';
import { PageHeaderGroup } from '@/components/shared/PageHeaderGroup';
import { useActiveTeamId } from '@/hooks/useActiveTeamId';
import {
    MetricsTable,
    SCORECARD_TAB,
    useMetrics,
    useWeeks,
    type ScorecardTab,
    useMetricDialog,
    MetricDialog,
    WeekSelect,
} from '@/features/scorecard';
import { Tabs, TabsList, TabsTrigger, TabsContent } from '@/components/ui/tabs';
import { UI_MESSAGES, ERROR_MESSAGES } from '@/constants/messages';
import { EmptyTeamState } from '@/components/shared/EmptyTeamState';
import { TrendsTab } from '@/features/scorecard';
import { checkCurrentWeek } from '@/features/scorecard/utils/weekUtils';

export function Scorecards() {
    const activeTeamId = useActiveTeamId();
    const { data: weeks = [], isPending: isWeeksPending, isError: isWeeksError } = useWeeks();
    const [selectedWeek, setSelectedWeek] = useState<string | undefined>(weeks[0]?.id);
    const selectedWeekValue = selectedWeek ?? weeks[0]?.id;

    const isEditingCurrentWeek = weeks.find((week) => week.id === selectedWeekValue && checkCurrentWeek(week)) !== undefined;

    const [activeTab, setActiveTab] = useState<ScorecardTab>(SCORECARD_TAB.METRICS);
    const emptyMessage = !isWeeksPending && weeks.length === 0 ? UI_MESSAGES.WEEK.NOT_FOUND : ERROR_MESSAGES.METRIC.NOT_FOUND;

    const { isMetricDialogOpen, setMetricDialogOpen, editingMetric, openCreate, openUpdate, handleSubmit } = useMetricDialog();

    const {
        data: metrics,
        isPending: isMetricsPending,
        isError: isMetricsError,
    } = useMetrics({
        teamId: activeTeamId!,
        weekId: selectedWeekValue!,
    });

    const handleTabChange = (val: string) => {
        if (val === SCORECARD_TAB.METRICS || val === SCORECARD_TAB.TRENDS) {
            setActiveTab(val);
        }
    };

    return (
        <div className="flex flex-col gap-6">
            <PageHeaderGroup title="Scorecards" description="Track weekly numbers that predict performance.">
                <Button type="button" onClick={openCreate} disabled={!activeTeamId}>
                    <Plus className="size-4" aria-hidden="true" />
                    Add Metric
                </Button>
            </PageHeaderGroup>

            <MetricDialog isOpen={isMetricDialogOpen} onOpenChange={setMetricDialogOpen} onSubmit={handleSubmit} editingMetric={editingMetric} />

            {!activeTeamId ? (
                <EmptyTeamState />
            ) : (
                <Tabs value={activeTab} onValueChange={handleTabChange} className="flex flex-col gap-6">
                    <div>
                        <TabsList className="grid w-[300px] grid-cols-2">
                            <TabsTrigger value={SCORECARD_TAB.METRICS}>Weekly Metrics</TabsTrigger>
                            <TabsTrigger value={SCORECARD_TAB.TRENDS}>Trends</TabsTrigger>
                        </TabsList>
                    </div>

                    <TabsContent value={SCORECARD_TAB.METRICS} className="m-0 flex flex-col gap-6">
                        <div className="flex items-center gap-2 justify-end">
                            <span className="text-sm font-medium">{UI_MESSAGES.WEEK.LABEL}</span>
                            <WeekSelect
                                weeks={weeks}
                                value={selectedWeekValue}
                                onValueChange={setSelectedWeek}
                                isPending={isWeeksPending}
                                isError={isWeeksError}
                            />
                        </div>

                        <MetricsTable
                            data={metrics}
                            emptyMessage={emptyMessage}
                            isPending={isMetricsPending}
                            isError={isMetricsError}
                            isEditable={isEditingCurrentWeek}
                            onUpdate={openUpdate}
                        />
                    </TabsContent>

                    <TabsContent value={SCORECARD_TAB.TRENDS} className="m-0">
                        <TrendsTab />
                    </TabsContent>
                </Tabs>
            )}
        </div>
    );
}
