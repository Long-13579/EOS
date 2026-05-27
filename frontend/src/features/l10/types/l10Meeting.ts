import type { Team } from '@/types/team';
import type { TeamMember } from '@/types/user';
import type { PaginationParams } from '@/types/pagination';

export type L10MeetingStatus = 'SCHEDULED' | 'STARTED' | 'FINISHED';

export type L10MeetingRatingValue = 'ONE' | 'TWO' | 'THREE' | 'FOUR' | 'FIVE' | 'SIX' | 'SEVEN' | 'EIGHT' | 'NINE' | 'TEN' | 'ABSENT';

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
    ratings?: L10MeetingRating[];
}

export interface L10MeetingRating {
    id: string;
    meetingId: string;
    member: TeamMember;
    rating: L10MeetingRatingValue;
    createdAt: string;
    updatedAt: string;
}

export interface RatingItem {
    memberId: string;
    rating: L10MeetingRatingValue;
}

export interface UpsertRatingsPayload {
    ratings: RatingItem[];
}

export interface UpdateConcludePayload {
    keyDecisions: string;
    cascadingMessage: string;
}

export type AgendaSession = 'segue' | 'scorecard' | 'rocks' | 'headlines' | 'todos' | 'issues' | 'conclude';

export const AGENDA_SESSIONS: AgendaSession[] = ['segue', 'scorecard', 'rocks', 'headlines', 'todos', 'issues', 'conclude'];

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

export interface UpdateL10MeetingPayload {
    meetingDate: string;
    meetingTime: string;
    facilitatorId: string;
    scribeId: string;
}
