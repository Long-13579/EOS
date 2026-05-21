import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { UserTeamsCell } from './UserTeamsCell';
import { TableActions } from '@/components/shared/Table/TableActions';
import { Pencil } from 'lucide-react';
import { TableQueryState } from '@/components/shared/Table';

import type { DataTableProps } from '@/types/table';
import type { User } from '@/types/user';
import { ERROR_MESSAGES } from '@/constants/messages';

export interface UsersTableProps extends DataTableProps<User> {
    isPending: boolean;
    isError: boolean;
    onUpdate: (user: User) => void;
}

const getUserActions = (user: User, onUpdate: (user: User) => void) => {
    return [
        {
            label: 'Edit user',
            icon: Pencil,
            onClick: () => onUpdate(user),
        },
    ];
};

export function UsersTable({ data, isPending, isError, onUpdate }: UsersTableProps) {
    return (
        <div className="rounded-md border">
            <Table className="w-full table-fixed">
                <TableHeader className="[&_th]:font-bold">
                    <TableRow>
                        <TableHead className="pl-6 w-[150px]">First Name</TableHead>
                        <TableHead className="pl-6 w-[150px]">Last Name</TableHead>
                        <TableHead className="w-[150px]">Email</TableHead>
                        <TableHead className="w-[120px]">Role</TableHead>
                        <TableHead className="w-[220px]">Teams</TableHead>
                        <TableHead className="text-right w-[120px] pr-8">Actions</TableHead>
                    </TableRow>
                </TableHeader>

                <TableBody>
                    <TableQueryState
                        isPending={isPending}
                        isError={isError}
                        isEmpty={data.length === 0}
                        colSpan={6}
                        errorMessage={ERROR_MESSAGES.USER.LOAD_FAILED}
                        emptyMessage={ERROR_MESSAGES.USER.NOT_FOUND}
                    >
                        {data.map((user) => (
                            <TableRow key={user.id}>
                                <TableCell className="pl-6 font-medium">
                                    <div className="truncate max-w-[180px]" title={user.firstName}>
                                        {user.firstName}
                                    </div>
                                </TableCell>

                                <TableCell className="pl-6 font-medium">
                                    <div className="truncate max-w-[180px]" title={user.lastName}>
                                        {user.lastName}
                                    </div>
                                </TableCell>

                                <TableCell>
                                    <div className="truncate max-w-[240px]" title={user.email}>
                                        {user.email}
                                    </div>
                                </TableCell>

                                <TableCell>{user.role}</TableCell>

                                <TableCell>
                                    <UserTeamsCell teams={user.teams} />
                                </TableCell>

                                <TableCell className="text-right pr-4.5">
                                    <TableActions actions={getUserActions(user, onUpdate)} />
                                </TableCell>
                            </TableRow>
                        ))}
                    </TableQueryState>
                </TableBody>
            </Table>
        </div>
    );
}
