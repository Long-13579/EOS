import { useMutation, useQueryClient } from '@tanstack/react-query';
import { ERROR_MESSAGES, SUCCESS_MESSAGES } from '@/constants/messages';
import { toast } from 'sonner';
import type { CreateMetric } from '../types/metric';
import { createMetric } from '../services/metricService';
import { metricKeys } from '../types/metricKeys';

export const useCreateMetric = () => {
    const queryClient = useQueryClient();

    const { mutateAsync, isPending } = useMutation({
        mutationFn: (payload: CreateMetric) => createMetric(payload),

        onSuccess: () => {
            toast.success(SUCCESS_MESSAGES.METRIC.CREATED);
        },

        onError: () => {
            toast.error(ERROR_MESSAGES.METRIC.CREATE_FAILED);
        },
        onSettled: () => {
            queryClient.invalidateQueries({ queryKey: metricKeys.lists() });
            queryClient.invalidateQueries({ queryKey: metricKeys.myMetrics() });
        },
    });

    return {
        createMetric: mutateAsync,
        isCreatingMetric: isPending,
    };
};
