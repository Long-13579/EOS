import type { BaseEntity } from '@/types/base';
import type { PaginationParams } from '@/types/pagination';
import type { Team } from '@/types/team';
import type { TeamMember } from '@/types/user';

export const ISSUE_VIEW = {
    ISSUES: 'issues',
    LONG_TERM: 'long_term',
    ARCHIVED: 'archived',
} as const;

export type IssueView = (typeof ISSUE_VIEW)[keyof typeof ISSUE_VIEW];

export const LONG_TERM_ISSUE = 'Long Term Issue';

export interface IssueType {
    id: string;
    name: string;
}

export interface Issue extends BaseEntity {
    id: string;
    title: string;
    description?: string;
    issueType: IssueType | null;
    creator: TeamMember | null;
    isArchived: boolean;
    totalTodosCount?: number;
    team?: Team;
}

export interface IssueFormValues {
    title: string;
    description?: string;
    issueTypeId?: string | null;
}

export type CreateIssue = IssueFormValues & {
    teamId: string;
};

export type UpdateIssue = IssueFormValues;

export interface UpdateIssueType {
    issueTypeId: string | null;
}

export interface GetIssuesParams extends PaginationParams {
    teamId?: string;
    isArchived?: boolean;
    issueTypeId?: string;
    view?: IssueView;
}
