import { z } from 'zod';
import type { HeadlineFormValues } from '../types/headline';

export const headlineSchema = z.object({
    title: z.string().trim().min(1, 'Headline cannot be empty').max(2000, 'Headline is too long'),
}) satisfies z.ZodType<HeadlineFormValues>;
