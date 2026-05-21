import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { TableQueryState } from '@/components/shared/Table';
import { TableActions } from '@/components/shared/Table/TableActions';
import { Pencil, Trash2, User } from 'lucide-react';
import type { Metric } from '../types/metric';
import { ERROR_MESSAGES } from '@/constants/messages';
import { formatMetricValue } from '../utils/metricFormat';
import { getUserFullName } from '@/utils/user';
import { useUpdateMetricValue } from '../hooks/useUpdateMetricValue';
import { MetricValueCell } from './MetricValueCell';
import { getMetricColor } from '../utils/metricColor';

interface MetricsTableProps {
    data?: Metric[];
    isPending: boolean;
    isError: boolean;
    emptyMessage?: string;
    onUpdate?: (metric: Metric) => void;
    onDelete?: (metric: Metric) => void;
    isEditable?: boolean;
    isDashboardView?: boolean;
}

interface MetricActionsProps {
    metric: Metric;
    onUpdate?: (metric: Metric) => void;
    onDelete?: (metric: Metric) => void;
}

const getActions = ({ metric, onUpdate, onDelete }: MetricActionsProps) => [
    ...(onUpdate
        ? [
              {
                  label: 'Edit Metric',
                  icon: Pencil,
                  onClick: () => onUpdate(metric),
              },
          ]
        : []),
    ...(onDelete
        ? [
              {
                  label: 'Delete Metric',
                  icon: Trash2,
                  variant: 'destructive' as const,
                  onClick: () => onDelete(metric),
              },
          ]
        : []),
];

export function MetricsTable({ data, isPending, isError, emptyMessage, onUpdate, onDelete, isEditable, isDashboardView = false }: MetricsTableProps) {
    const { updateMetricValue, isUpdating } = useUpdateMetricValue();

    const handleUpdate = (metricId: string, value: string | null) => {
        return updateMetricValue({ metricId, value });
    };

    return (
        <div className="rounded-md border">
            <div className="max-h-[500px] overflow-y-auto">
                <Table className="w-full table-fixed">
                    <TableHeader className="sticky top-0 bg-background z-10 [&_th]:font-bold">
                        <TableRow>
                            <TableHead className="pl-6 w-[240px]">Metric</TableHead>
                            <TableHead className="w-[80px] text-center">Operator</TableHead>
                            <TableHead className="w-[120px] text-right">Goal</TableHead>
                            <TableHead className="w-[120px] text-right">Past Value</TableHead>
                            <TableHead className="w-[120px] text-right pr-8">Value</TableHead>
                            <TableHead className="w-[180px] text-center">{isDashboardView ? 'Team' : 'Owner'}</TableHead>
                            <TableHead className="text-right pr-8 w-[80px]">Actions</TableHead>
                        </TableRow>
                    </TableHeader>

                    <TableBody>
                        <TableQueryState
                            isPending={isPending}
                            isError={isError}
                            isEmpty={data?.length === 0}
                            colSpan={7}
                            emptyMessage={emptyMessage ?? ERROR_MESSAGES.METRIC.NOT_FOUND}
                            errorMessage={ERROR_MESSAGES.METRIC.LOAD_FAILED}
                        >
                            {data?.map((metric) => {
                                return (
                                    <TableRow key={metric.id}>
                                        <TableCell className="pl-6 font-medium max-w-[240px]">
                                            {onUpdate ? (
                                                <button
                                                    type="button"
                                                    className="block max-w-full truncate overflow-hidden hover:text-primary hover:underline"
                                                    title={metric.name}
                                                    onClick={() => onUpdate(metric)}
                                                    aria-label={`Edit Metric: ${metric.name}`}
                                                >
                                                    {metric.name}
                                                </button>
                                            ) : (
                                                <span className="block max-w-full truncate overflow-hidden" title={metric.name}>
                                                    {metric.name}
                                                </span>
                                            )}
                                        </TableCell>

                                        <TableCell className="text-center">
                                            <span className="text-muted-foreground">{metric.operator || '-'}</span>
                                        </TableCell>

                                        <TableCell className="text-right truncate" title={formatMetricValue(metric.goal, metric.unit)}>
                                            {formatMetricValue(metric.goal, metric.unit)}
                                        </TableCell>

                                        <TableCell className="text-right truncate" title={formatMetricValue(metric.pastValue, metric.unit)}>
                                            <span className="text-muted-foreground">{formatMetricValue(metric.pastValue, metric.unit)}</span>
                                        </TableCell>

                                        <TableCell className="text-right">
                                            <span className={`font-medium ${getMetricColor(metric)}`}>
                                                <MetricValueCell
                                                    metric={metric}
                                                    onUpdate={handleUpdate}
                                                    isUpdating={isUpdating}
                                                    isEditable={isEditable}
                                                />
                                            </span>
                                        </TableCell>

                                        <TableCell className="text-center align-middle">
                                            {isDashboardView ? (
                                                metric.team?.name ? (
                                                    <div className="text-muted-foreground w-full">
                                                        <span className="max-w-[180px] truncate" title={metric.team.name}>
                                                            {metric.team.name}
                                                        </span>
                                                    </div>
                                                ) : (
                                                    <span className="text-muted-foreground">-</span>
                                                )
                                            ) : metric.owner ? (
                                                <div className="flex items-center gap-2 text-muted-foreground justify-center w-full truncate">
                                                    <User className="h-4 w-4" />
                                                    <span className="max-w-[180px] truncate" title={getUserFullName(metric.owner)}>
                                                        {getUserFullName(metric.owner)}
                                                    </span>
                                                </div>
                                            ) : (
                                                <span className="text-muted-foreground">Unassigned</span>
                                            )}
                                        </TableCell>

                                        <TableCell className="text-right pr-8">
                                            <TableActions actions={getActions({ metric, onUpdate, onDelete })} />
                                        </TableCell>
                                    </TableRow>
                                );
                            })}
                        </TableQueryState>
                    </TableBody>
                </Table>
            </div>
        </div>
    );
}
