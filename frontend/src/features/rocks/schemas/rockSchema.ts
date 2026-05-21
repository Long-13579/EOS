import { z } from 'zod';
import { ROCK_CATEGORY, ROCK_STATUS, type RockFormValues } from '../types/rock';

export const rockFormSchema = z.object({
    title: z.string().trim().min(1, 'Title is required.').max(255, 'Title must be at most 255 characters.'),
    category: z.enum([ROCK_CATEGORY.COMPANY, ROCK_CATEGORY.DEPARTMENT, ROCK_CATEGORY.INDIVIDUAL], {
        message: 'Category is required.',
    }),
    status: z.enum([ROCK_STATUS.ON_TRACK, ROCK_STATUS.COMPLETED, ROCK_STATUS.OFF_TRACK, ROCK_STATUS.DEFERRED]),
    ownerId: z.uuid('Owner is required.'),
    year: z.string().min(1, 'Year is required.'),
    quarterId: z.uuid('Quarter is required.'),
    dueDate: z.date().nullable(),
    description: z.string().trim().min(1, 'Description is required.').max(2000, 'Description must be at most 2000 characters.'),
}) satisfies z.ZodType<RockFormValues>;
