import { useState } from 'react';
import type { Metric, MetricFormValues } from '../types/metric';
import { useCreateMetric } from './useCreateMetric';
import { useUpdateMetric } from './useUpdateMetric';
import { useActiveTeamId } from '@/hooks/useActiveTeamId';

export function useMetricDialog() {
    const activeTeamId = useActiveTeamId();
    const [isMetricDialogOpen, setMetricDialogOpen] = useState(false);

    const [editingMetric, setEditingMetric] = useState<Metric | undefined>();

    const openCreate = () => {
        setEditingMetric(undefined);
        setMetricDialogOpen(true);
    };

    const openUpdate = (metric: Metric) => {
        setEditingMetric(metric);
        setMetricDialogOpen(true);
    };

    const { createMetric } = useCreateMetric();
    const { updateMetric } = useUpdateMetric();

    const handleSubmit = async (data: MetricFormValues) => {
        if (editingMetric) {
            const payload = {
                name: data.name,
                goal: data.goal,
                operator: data.operator,
                ownerId: data.ownerId,
            };

            await updateMetric({
                id: editingMetric.id,
                payload,
            });
        } else {
            if (!activeTeamId) {
                return;
            }

            const payload = {
                ...data,
                teamId: activeTeamId,
            };

            await createMetric(payload);
        }

        setMetricDialogOpen(false);
    };

    return { isMetricDialogOpen, setMetricDialogOpen, editingMetric, openCreate, openUpdate, handleSubmit };
}
