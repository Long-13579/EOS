import { z } from 'zod';

export const scheduleL10MeetingSchema = z.object({
    meetingDate: z.date({ message: 'Meeting date is required.' }),
    meetingTime: z.string().trim().min(1, 'Meeting time is required.'),
    facilitatorId: z.string().trim().min(1, 'Facilitator is required.'),
    scribeId: z.string().trim().min(1, 'Scribe is required.'),
});

export type ScheduleL10MeetingFormValues = z.infer<typeof scheduleL10MeetingSchema>;

export const concludeSessionSchema = z.object({
    keyDecisions: z.string().trim().min(1, 'Key decisions are required.'),
    cascadingMessage: z.string().trim().min(1, 'Cascading message is required.'),
});

export type ConcludeSessionFormValues = z.infer<typeof concludeSessionSchema>;
