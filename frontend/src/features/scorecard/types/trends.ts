import type { Week } from './week';
import type { MetricUnit } from './metric';
import type { TeamMember } from '@/types/user';
import type { Team } from '@/types/team';
import type { BaseEntity } from '@/types/base';

export interface MetricValueBaseResponse {
    id: string;
    value?: string;
    isGoalMet?: boolean;
}

export interface TrendDataPointResponse {
    week: Week;
    metricValue?: MetricValueBaseResponse;
}

export interface TrendsTabMetricResponse extends BaseEntity {
    id: string;
    name: string;
    goal: string;
    unit: MetricUnit;
    operator: string;
    owner: TeamMember;
    values: TrendDataPointResponse[];
    team: Team;
}

export interface TrendsTabMetricListResponse {
    items: TrendsTabMetricResponse[];
}
