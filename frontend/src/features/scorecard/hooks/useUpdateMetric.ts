import { useMutation, useQueryClient } from '@tanstack/react-query';
import { metricKeys } from '../types/metricKeys';
import { updateMetric } from '../services/metricService';
import type { UpdateMetric } from '../types/metric';
import { toast } from 'sonner';
import { ERROR_MESSAGES, SUCCESS_MESSAGES } from '@/constants/messages';
import { trendKeys } from '../types/trendKeys';

export const useUpdateMetric = () => {
    const queryClient = useQueryClient();

    const { mutateAsync, isPending } = useMutation({
        mutationFn: ({ id, payload }: { id: string; payload: UpdateMetric }) => updateMetric(id, payload),

        onSuccess: () => {
            toast.success(SUCCESS_MESSAGES.METRIC.UPDATED);
        },

        onError: () => {
            toast.error(ERROR_MESSAGES.METRIC.UPDATE_FAILED);
        },
        onSettled: () => {
            queryClient.invalidateQueries({ queryKey: metricKeys.lists() });
            queryClient.invalidateQueries({ queryKey: metricKeys.myMetrics() });
            queryClient.invalidateQueries({ queryKey: trendKeys.lists() });
        },
    });

    return {
        updateMetric: mutateAsync,
        isUpdatingMetric: isPending,
    };
};
