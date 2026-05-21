import type { MetricUnit } from '../types/metric';
import { z } from 'zod';

const baseValueSchema = z
    .string()
    .nullable()
    .transform((value): string | null => {
        if (value === '' || value === undefined || value === null) {
            return null;
        }
        return String(value).trim().toUpperCase();
    });

const numberSchema = z.object({
    value: baseValueSchema.refine((value) => value === null || !isNaN(Number(value)), 'Must be a valid number'),
});

const yesNoSchema = z.object({
    value: baseValueSchema.refine((value) => value === null || value === 'YES' || value === 'NO', 'Must be YES or NO'),
});

const rygSchema = z.object({
    value: baseValueSchema.refine((value) => value === null || value === 'RED' || value === 'YELLOW' || value === 'GREEN', 'Invalid status'),
});

const defaultSchema = z.object({
    value: baseValueSchema,
});

export const getMetricValueSchema = (unit: MetricUnit) => {
    switch (unit) {
        case 'NUMBER':
        case 'PERCENTAGE':
        case 'CURRENCY':
            return numberSchema;

        case 'YES_NO':
            return yesNoSchema;

        case 'RYG_STATUS':
            return rygSchema;

        default:
            return defaultSchema;
    }
};
