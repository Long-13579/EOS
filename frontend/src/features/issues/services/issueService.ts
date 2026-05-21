import { apiGet, apiPatch, apiPost, apiPut, apiDelete } from '@/utils/apiRequest';
import type { CreateIssue, GetIssuesParams, Issue, IssueType, UpdateIssue } from '../types/issue';
import type { PaginatedResponse } from '@/types/pagination';

export const getIssues = (params: GetIssuesParams): Promise<PaginatedResponse<Issue>> => apiGet<PaginatedResponse<Issue>>('/issues', { params });

export const createIssue = (payload: CreateIssue): Promise<Issue> => apiPost<Issue>('/issues', payload);

export const getIssueTypes = (): Promise<IssueType[]> => apiGet<IssueType[]>('/issue-types');

export const updateIssue = (id: string, payload: UpdateIssue): Promise<Issue> => apiPut<Issue>(`/issues/${id}`, payload);

export const deleteIssue = (id: string): Promise<void> => apiDelete<void>(`/issues/${id}`);

export const archiveIssue = (id: string, isArchived: boolean): Promise<Issue> => apiPatch<Issue>(`/issues/${id}`, { isArchived });
