import * as z from 'zod';
import type { CreateUser, UpdateUser } from '@/types/user';

const nameRegex = new RegExp(/^[a-zA-ZÀ-ỹ]+(?:[ \-'][a-zA-ZÀ-ỹ]+)*$/);

export const baseUserSchema = z.object({
    firstName: z
        .string()
        .trim()
        .min(1, { error: 'First name must not be empty.' })
        .max(100, { error: 'First name must be at most 100 characters.' })
        .regex(nameRegex, { error: 'First name must only contain letters, spaces, hyphens, and apostrophes.' }),
    lastName: z
        .string()
        .trim()
        .min(1, { error: 'Last name must not be empty.' })
        .max(100, { error: 'Last name must be at most 100 characters.' })
        .regex(nameRegex, { error: 'Last name must only contain letters, spaces, hyphens, and apostrophes.' }),
    email: z.email({ error: 'Must be a valid email address.' }).min(1, 'Email is required'),
    teamIds: z.array(z.uuid({ error: 'Each team ID must be a valid UUID.' })).optional(),
    role: z.enum(['ADMIN', 'USER'], { error: 'Role must be one of ADMIN, or USER.' }),
});

export const createUserSchema = baseUserSchema satisfies z.ZodType<CreateUser>;

export const updateUserSchema = baseUserSchema satisfies z.ZodType<UpdateUser>;
