import type { BaseEntity } from '@/types/base';
import type { Team } from '@/types/team';
import type { TeamMember } from '@/types/user';

export interface Year extends BaseEntity {
    id: string;
    year: number;
    isCurrent: boolean;
}

export interface Quarter extends BaseEntity {
    id: string;
    name: string;
    startDate: string;
    endDate: string;
    isCurrent: boolean;
}

export const ROCK_STATUS = {
    ON_TRACK: 'ON_TRACK',
    OFF_TRACK: 'OFF_TRACK',
    COMPLETED: 'COMPLETED',
    DEFERRED: 'DEFERRED',
} as const;

export type RockStatus = (typeof ROCK_STATUS)[keyof typeof ROCK_STATUS];

export const ROCK_CATEGORY = {
    COMPANY: 'COMPANY',
    DEPARTMENT: 'DEPARTMENT',
    INDIVIDUAL: 'INDIVIDUAL',
} as const;

export type RockCategory = (typeof ROCK_CATEGORY)[keyof typeof ROCK_CATEGORY];

export interface Rock extends BaseEntity {
    id: string;
    title: string;
    description: string;
    status: RockStatus;
    category: RockCategory;
    dueDate: string;
    team: Team;
    year: Year;
    quarter: Quarter;
    owner: TeamMember;
    isArchived: boolean;
}

export interface GetRocksResponse {
    companyRocks?: Rock[];
    departmentRocks: Rock[];
    individualRocks: Rock[];
}

export interface GetRocksParams {
    teamId: string;
    yearId?: string;
    quarterId?: string;
    isArchived?: boolean;
}

export interface GetPersonalRocksParams {
    quarterId?: string;
    yearId?: string;
}

export interface RockFormValues {
    title: string;
    category: RockCategory;
    ownerId: string;
    year: string;
    status: RockStatus;
    quarterId: string;
    dueDate: Date | null;
    description: string;
}

export interface CreateRock {
    title: string;
    description: string;
    status: RockStatus;
    category: RockCategory;
    dueDate: string;
    teamId: string;
    year: number;
    quarterId: string;
    ownerId: string;
}

export interface UpdateRockStatus {
    status: RockStatus;
}

export interface UpdateRock {
    title: string;
    description: string;
    status: RockStatus;
    category: RockCategory;
    dueDate: string;
    year: number;
    quarterId: string;
    ownerId: string;
}

export interface PersonalRockListResponse {
    data: Rock[];
}
