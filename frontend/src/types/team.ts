export interface Team {
    id: string;
    name: string;
    isLeadership: boolean;
}

export type CreateTeam = Pick<Team, 'name'>;
export type UpdateTeam = Pick<Team, 'name'>;
