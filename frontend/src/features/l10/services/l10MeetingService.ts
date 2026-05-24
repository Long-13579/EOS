import { apiGet, apiPost } from '@/utils/apiRequest';
import type { PaginatedResponse } from '@/types/pagination';
import type { GetL10MeetingsParams, L10Meeting, ScheduleL10MeetingPayload } from '../types/l10Meeting';

export const getL10Meetings = (params: GetL10MeetingsParams): Promise<PaginatedResponse<L10Meeting>> =>
    apiGet<PaginatedResponse<L10Meeting>>('/l10-meetings', { params });

export const scheduleL10Meeting = (payload: ScheduleL10MeetingPayload): Promise<L10Meeting> =>
    apiPost<L10Meeting>('/l10-meetings', payload);

export const startL10Meeting = (meetingId: string): Promise<L10Meeting> =>
    apiPost<L10Meeting>(`/l10-meetings/${meetingId}/start`);
