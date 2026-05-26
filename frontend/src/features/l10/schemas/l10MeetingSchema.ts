import { z } from 'zod';

export const scheduleL10MeetingSchema = z.object({
    meetingDate: z.date({ message: 'Meeting date is required.' }),
    meetingTime: z.string().trim().min(1, 'Meeting time is required.'),
    facilitatorId: z.string().trim().min(1, 'Facilitator is required.'),
    scribeId: z.string().trim().min(1, 'Scribe is required.'),
});

export type ScheduleL10MeetingFormValues = z.infer<typeof scheduleL10MeetingSchema>;
