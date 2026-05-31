import type { BaseEntity } from '@/types/base';
import type { PaginationParams } from '@/types/pagination';
import type { Team } from '@/types/team';
import type { TeamMember } from '@/types/user';

export const ALL_TODO_STATUSES = 'ALL';

export type TodoStatus = 'NOT_STARTED' | 'IN_PROGRESS' | 'COMPLETED';

export interface Todo extends BaseEntity {
    id: string;
    title: string;
    description?: string;
    status: TodoStatus;
    isArchived: boolean;
    dueDate?: string;
    assignees?: TeamMember[];
    issueId?: string | null;
    team: Team;
}

export interface CreateTodo extends Omit<TodoFormValues, 'dueDate'> {
    dueDate?: string;
    teamId: string;
    issueId?: string | null;
}

export interface UpdateTodo extends Omit<TodoFormValues, 'dueDate'> {
    dueDate?: string;
    issueId?: string | null;
}

export interface ArchiveTodo {
    isArchived: boolean;
}

export interface TodoFormValues {
    title: string;
    description?: string;
    status: TodoStatus;
    dueDate?: Date;
    assigneeIds?: string[];
}

export interface GetTodosParams extends PaginationParams {
    teamId?: string;
    status?: TodoStatus;
    isArchived?: boolean;
    issueId?: string;
}
