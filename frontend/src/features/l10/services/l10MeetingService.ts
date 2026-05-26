import { apiGet, apiPost, apiPut, apiDelete } from '@/utils/apiRequest';
import type { PaginatedResponse } from '@/types/pagination';
import type { GetL10MeetingsParams, L10Meeting, L10MeetingRating, ScheduleL10MeetingPayload, UpdateL10MeetingPayload, UpdateConcludePayload, UpsertRatingsPayload } from '../types/l10Meeting';

export const getL10Meetings = (params: GetL10MeetingsParams): Promise<PaginatedResponse<L10Meeting>> =>
    apiGet<PaginatedResponse<L10Meeting>>('/l10-meetings', { params });

export const getL10Meeting = (meetingId: string): Promise<L10Meeting> =>
    apiGet<L10Meeting>(`/l10-meetings/${meetingId}`);

export const scheduleL10Meeting = (payload: ScheduleL10MeetingPayload): Promise<L10Meeting> => apiPost<L10Meeting>('/l10-meetings', payload);

export const startL10Meeting = (meetingId: string): Promise<L10Meeting> => apiPost<L10Meeting>(`/l10-meetings/${meetingId}/start`);

export const updateL10Meeting = (meetingId: string, payload: UpdateL10MeetingPayload): Promise<L10Meeting> =>
    apiPut<L10Meeting>(`/l10-meetings/${meetingId}`, payload);

export const deleteL10Meeting = (meetingId: string): Promise<void> => apiDelete<void>(`/l10-meetings/${meetingId}`);

export const updateConclude = (meetingId: string, payload: UpdateConcludePayload): Promise<L10Meeting> =>
    apiPut<L10Meeting>(`/l10-meetings/${meetingId}/conclude`, payload);

export const upsertRatings = (meetingId: string, payload: UpsertRatingsPayload): Promise<L10MeetingRating[]> =>
    apiPut<L10MeetingRating[]>(`/l10-meetings/${meetingId}/ratings`, payload);

export const finishL10Meeting = (meetingId: string): Promise<L10Meeting> =>
    apiPost<L10Meeting>(`/l10-meetings/${meetingId}/finish`);

export const getRatings = (meetingId: string): Promise<L10MeetingRating[]> =>
    apiGet<L10MeetingRating[]>(`/l10-meetings/${meetingId}/ratings`);
