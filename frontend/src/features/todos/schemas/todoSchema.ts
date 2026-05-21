import { z } from 'zod';
import type { TodoFormValues } from '@/features/todos/types/todo';

export const todoFormSchema = z.object({
    title: z.string().trim().min(1, 'Title is required.').max(255, 'Title must be less than or equal to 255 characters.'),

    description: z.string().trim().max(2000, 'Description must be less than or equal to 2000 characters.').optional(),

    status: z.enum(['NOT_STARTED', 'IN_PROGRESS', 'COMPLETED']),

    dueDate: z.date().optional(),

    assigneeIds: z
        .array(z.string().trim())
        .optional()
        .transform((val) => (val && val.length === 0 ? undefined : val)),
}) satisfies z.ZodType<TodoFormValues>;
