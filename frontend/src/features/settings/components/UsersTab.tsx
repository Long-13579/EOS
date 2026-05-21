import { useState } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';

import { Button } from '@/components/ui/button';
import { Plus } from 'lucide-react';

import { useUsers } from '@/features/settings/hooks/useUsers';
import { UsersTable } from '@/features/settings/components/UsersTable';
import { CustomPagination } from '@/components/shared/CustomPagination';
import { DEFAULT_LIMIT } from '@/types/pagination';
import { UserDialog } from './UserDialog';
import type { User } from '@/types/user';

export function UsersTab() {
    const [page, setPage] = useState<number>(1);
    const [selectedUser, setSelectedUser] = useState<User | undefined>(undefined);
    const [isUserDialogOpen, setIsUserDialogOpen] = useState<boolean>(false);

    const {
        data: userResponse,
        isPending,
        isError,
    } = useUsers({
        page,
        limit: DEFAULT_LIMIT,
        // TODO: Implement search
    });

    const totalPages = userResponse?.pagination.totalPages ?? 0;

    const handleCreateUser = () => {
        setSelectedUser(undefined);
        setIsUserDialogOpen(true);
    };

    const handleUpdateUser = (user: User) => {
        setSelectedUser(user);
        setIsUserDialogOpen(true);
    };

    const handleCloseUserDialog = () => {
        setIsUserDialogOpen(false);
    };

    const handleUserSaved = () => {
        if (selectedUser === undefined) {
            // "Create" action
            setPage(1);
        }
        setIsUserDialogOpen(false);
    };

    return (
        <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <div className="space-y-1">
                    <CardTitle className="text-[20px]">Users List</CardTitle>
                    <CardDescription>Manage users and their team assignments.</CardDescription>
                </div>

                <Button onClick={handleCreateUser}>
                    <Plus className="mr-2 h-4 w-4" /> Add User
                </Button>
            </CardHeader>

            <CardContent>
                {/* // TODO: Integrate with search functionality  
                <div className="flex items-center py-4">
                    <div className="relative w-full max-w-sm">
                        <Search className="absolute left-2 top-2.5 h-4 w-4 text-muted-foreground" />

                        <Input placeholder="Search by name or email..." value={search} onChange={(e) => handleSearchChange(e)} className="pl-8" />
                    </div>
                </div> */}

                <UsersTable isError={isError} isPending={isPending} data={userResponse?.data || []} onUpdate={handleUpdateUser} />
                <CustomPagination currentPage={page} totalPages={totalPages} onPageChange={setPage} />
            </CardContent>

            <UserDialog user={selectedUser} isOpen={isUserDialogOpen} onClose={handleCloseUserDialog} onUserSaved={handleUserSaved} />
        </Card>
    );
}
