import type { BaseEntity } from '@/types/base';
import type { Team } from '@/types/team';
import type { TeamMember } from '@/types/user';
import type { Week } from './week';

export const SCORECARD_TAB = {
    METRICS: 'metrics',
    TRENDS: 'trends',
} as const;

export type ScorecardTab = (typeof SCORECARD_TAB)[keyof typeof SCORECARD_TAB];

export const METRIC_UNIT = {
    NUMBER: 'NUMBER',
    PERCENTAGE: 'PERCENTAGE',
    CURRENCY: 'CURRENCY',
    YES_NO: 'YES_NO',
    RYG_STATUS: 'RYG_STATUS',
} as const;

export type MetricUnit = (typeof METRIC_UNIT)[keyof typeof METRIC_UNIT];

export const METRIC_OPERATOR = {
    GREATER_THAN: 'GREATER_THAN',
    LESS_THAN: 'LESS_THAN',
    EQUAL_TO: 'EQUAL_TO',
    GREATER_THAN_OR_EQUAL_TO: 'GREATER_THAN_OR_EQUAL_TO',
    LESS_THAN_OR_EQUAL_TO: 'LESS_THAN_OR_EQUAL_TO',
} as const;

export type MetricOperator = (typeof METRIC_OPERATOR)[keyof typeof METRIC_OPERATOR];

export interface Metric extends BaseEntity {
    id: string;
    name: string;
    operator: '≥' | '≤' | '=' | '>' | '<' | null;
    goal: string;
    pastValue: string | null;
    currentValue?: MetricValue;
    team: Team;
    owner: TeamMember;
    unit: MetricUnit;
    isGoalMet?: boolean;
    isArchived?: boolean;
}

export interface MetricValue extends BaseEntity {
    id: string;
    week: Week;
    value?: string;
    isGoalMet?: boolean;
}

export interface MetricResponseItem extends BaseEntity {
    id: string;
    name: string;
    goal: string;
    operator?: MetricOperator;
    team: Team;
    owner: TeamMember;
    currentValue?: MetricValue;
    lastValue?: string;
    unit: MetricUnit;
    isArchived?: boolean;
}

export interface GetMetricsParams {
    teamId: string;
    weekId: string;
    showArchived?: boolean;
}

export type GetMyMetricsParams = Pick<GetMetricsParams, 'weekId'>;

export interface MetricFormValues {
    name: string;
    goal: string;
    unit: MetricUnit;
    operator?: MetricOperator;
    ownerId: string;
}

export type CreateMetric = MetricFormValues & {
    teamId: string;
};

export type UpdateMetricValue = {
    metricId: string;
    value: string | null;
};

export type UpdateMetric = Omit<MetricFormValues, 'unit'>;
