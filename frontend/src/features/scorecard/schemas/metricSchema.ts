import { z } from 'zod';
import { METRIC_OPERATOR, METRIC_UNIT, type MetricFormValues } from '../types/metric';

export const metricFormSchema = z.object({
    name: z.string().trim().min(1, 'Name is required.').max(255, 'Name must be 255 characters or fewer.'),
    goal: z.string().trim().min(1, 'Goal is required.'),
    unit: z.enum([METRIC_UNIT.NUMBER, METRIC_UNIT.CURRENCY, METRIC_UNIT.PERCENTAGE, METRIC_UNIT.YES_NO, METRIC_UNIT.RYG_STATUS]),
    operator: z
        .enum([
            METRIC_OPERATOR.LESS_THAN,
            METRIC_OPERATOR.LESS_THAN_OR_EQUAL_TO,
            METRIC_OPERATOR.GREATER_THAN,
            METRIC_OPERATOR.GREATER_THAN_OR_EQUAL_TO,
            METRIC_OPERATOR.EQUAL_TO,
        ])
        .optional(),
    ownerId: z.uuid('Owner is required.'),
}) satisfies z.ZodType<MetricFormValues>;
