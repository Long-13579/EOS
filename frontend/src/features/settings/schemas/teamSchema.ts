import { z } from 'zod';
import type { CreateTeam, UpdateTeam } from '@/types/team';

const teamSchema = z.object({
    name: z
        .string()
        .trim()
        .min(1, 'Team name is required')
        .max(100, 'Team name must be less than or equal to 100 characters')
        .regex(/^[\p{L}0-9 _-]+$/u, 'Team name contains invalid characters'),
});

export const createTeamSchema = teamSchema satisfies z.ZodType<CreateTeam>;
export const updateTeamSchema = teamSchema satisfies z.ZodType<UpdateTeam>;
