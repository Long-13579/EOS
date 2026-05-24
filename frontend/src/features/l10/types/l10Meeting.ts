import type { Team } from '@/types/team';
import type { TeamMember } from '@/types/user';
import type { PaginationParams } from '@/types/pagination';

export type L10MeetingStatus = 'SCHEDULED' | 'STARTED' | 'FINISHED';

export interface L10Meeting {
    id: string;
    team: Team;
    meetingDate: string;
    meetingTime: string;
    weekStartDate: string;
    facilitator: TeamMember;
    scribe: TeamMember;
    status: L10MeetingStatus;
    concludeKeyDecisions?: string;
    concludeCascadingMessage?: string;
    createdAt: string;
    updatedAt: string;
    createdBy: TeamMember | null;
    updatedBy: TeamMember | null;
}

export interface GetL10MeetingsParams extends PaginationParams {
    teamId: string;
    statuses: string;
}

export interface ScheduleL10MeetingPayload {
    teamId: string;
    meetingDate: string;
    meetingTime: string;
    facilitatorId: string;
    scribeId: string;
}
