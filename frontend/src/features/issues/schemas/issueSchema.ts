import { z } from 'zod';
import type { IssueFormValues } from '@/features/issues';

export const issueFormSchema = z.object({
    title: z.string().trim().min(1, 'Title is required.').max(255, 'Title must be less than or equal to 255 characters.'),

    description: z.string().trim().max(2000, 'Description must be less than or equal to 2000 characters.').optional(),

    issueTypeId: z.string().nullable().optional(),
}) satisfies z.ZodType<IssueFormValues>;
