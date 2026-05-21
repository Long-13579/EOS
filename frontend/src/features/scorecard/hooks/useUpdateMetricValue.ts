import { useMutation, useQueryClient } from '@tanstack/react-query';
import { metricKeys } from '../types/metricKeys';
import { updateMetricValue } from '../services/metricService';
import { trendKeys } from '../types/trendKeys';

interface UpdateMetricValueParams {
    value: string | null;
    metricId: string;
}

export const useUpdateMetricValue = () => {
    const queryClient = useQueryClient();

    const { mutateAsync, isPending, isError } = useMutation({
        mutationFn: ({ metricId, value }: UpdateMetricValueParams) => updateMetricValue({ metricId, value }),

        onSuccess: (updatedMetric) => {
            queryClient.setQueryData(metricKeys.detail(updatedMetric.id), updatedMetric);
        },
        onSettled: () => {
            queryClient.invalidateQueries({
                queryKey: metricKeys.lists(),
            });

            queryClient.invalidateQueries({
                queryKey: metricKeys.myMetrics(),
            });

            queryClient.invalidateQueries({
                queryKey: trendKeys.lists(),
            });
        },
    });

    return {
        updateMetricValue: mutateAsync,
        isUpdating: isPending,
        isError,
    };
};
