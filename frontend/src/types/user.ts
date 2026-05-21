import type { BaseEntity } from './base';
import type { Team } from './team';

export type UserRole = 'ADMIN' | 'USER';

export interface User extends BaseEntity {
    id: string;
    firstName: string;
    lastName: string;
    email: string;
    role: UserRole;
    isActive: boolean;
    teams: Team[];
}

export type CreateUser = Pick<User, 'firstName' | 'lastName' | 'email' | 'role'> & {
    teamIds?: string[];
};

export type UpdateUser = CreateUser;

export type TeamMember = Pick<User, 'id' | 'firstName' | 'lastName' | 'email'>;

export type CurrentUser = Pick<User, 'id' | 'firstName' | 'lastName' | 'email' | 'role'>;
