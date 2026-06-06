import { useMutation, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';

import { metricKeys } from '../types/metricKeys';
import { archiveMetric } from '../services/metricService';

import type { Metric } from '../types/metric';
import { SUCCESS_MESSAGES, ERROR_MESSAGES } from '@/constants/messages';

interface ArchiveMetricParams {
    id: string;
    isArchived: boolean;
}

export const useArchiveMetric = () => {
    const queryClient = useQueryClient();

    const mutation = useMutation({
        mutationFn: ({ id, isArchived }: ArchiveMetricParams) => archiveMetric(id, isArchived),

        onError: (_err, { isArchived }) => {
            toast.error(isArchived ? ERROR_MESSAGES.METRIC.ARCHIVE_FAILED : ERROR_MESSAGES.METRIC.UNARCHIVE_FAILED);
        },

        onSuccess: (_data, { isArchived }) => {
            toast.success(isArchived ? SUCCESS_MESSAGES.METRIC.ARCHIVED : SUCCESS_MESSAGES.METRIC.UNARCHIVED);
        },
        onSettled: () => {
            queryClient.invalidateQueries({
                queryKey: metricKeys.lists(),
            });
            queryClient.invalidateQueries({
                queryKey: metricKeys.myMetrics(),
            });
        },
    });

    const handleArchiveToggle = (metric: Metric) => {
        mutation.mutate({
            id: metric.id,
            isArchived: !metric.isArchived,
        });
    };

    return {
        handleArchiveToggle,
        isArchiving: mutation.isPending,
    };
};
