import { useState } from 'react';
import { useMutation, useMutationState, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { ERROR_MESSAGES, SUCCESS_MESSAGES } from '@/constants/messages';
import type { Metric } from '../types/metric';
import { deleteMetric } from '../services/metricService';
import { metricKeys } from '../types/metricKeys';

export function useDeleteMetric() {
    const queryClient = useQueryClient();
    const [isDeleteDialogOpen, setDeleteDialogOpen] = useState(false);
    const [deletingMetric, setDeletingMetric] = useState<Metric | null>(null);
    const { mutate } = useMutation({
        mutationKey: metricKeys.deleteMetric(),
        mutationFn: (id: string) => deleteMetric(id),

        onSuccess: () => {
            toast.success(SUCCESS_MESSAGES.METRIC.DELETED);
        },

        onError: () => {
            toast.error(ERROR_MESSAGES.METRIC.DELETE_FAILED);
        },

        onSettled: () => {
            if (queryClient.isMutating({ mutationKey: metricKeys.deleteMetric() }) <= 1) {
                queryClient.invalidateQueries({
                    queryKey: metricKeys.lists(),
                });
                queryClient.invalidateQueries({
                    queryKey: metricKeys.myMetrics(),
                });
            }
        },
    });

    const pendingData = useMutationState({
        filters: { mutationKey: metricKeys.deleteMetric(), status: 'pending' },
        select: (mutation) => mutation.state.variables as string,
    });

    const openDelete = (metric: Metric) => {
        setDeletingMetric(metric);
        setDeleteDialogOpen(true);
    };

    const handleConfirmDelete = () => {
        if (!deletingMetric) {
            return;
        }

        const deletedMetricId = deletingMetric.id;

        mutate(deletedMetricId);
        setDeleteDialogOpen(false);

        return deletedMetricId;
    };

    return {
        isDeleteDialogOpen,
        setDeleteDialogOpen,
        deletingMetric,
        deletingMetricIds: pendingData,
        openDelete,
        handleConfirmDelete,
    };
}
