import type { TeamMember } from '@/types/user';
import type { BaseEntity } from '@/types/base';
import type { PaginationParams } from '@/types/pagination';
import type { Team } from '@/types/team';

export interface Headline extends Omit<BaseEntity, 'createdBy' | 'updatedBy'> {
    id: string;
    title: string;
    isArchived: boolean;
    createdBy: TeamMember | null;
    updatedBy: TeamMember | null;
    team: Team;
}

export interface GetHeadlinesParams extends PaginationParams {
    teamId: string;
    isArchived?: boolean;
}

export type HeadlineFormValues = {
    title: string;
};

export type CreateHeadline = HeadlineFormValues & {
    teamId: string;
};

export type UpdateHeadline = HeadlineFormValues;

export type ArchiveHeadline = Pick<Headline, 'isArchived'>;
