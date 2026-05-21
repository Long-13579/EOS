import { useState } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { TeamsTable } from './TeamsTable';
import { useTeams } from '../hooks/useTeams';
import { CustomPagination } from '@/components/shared/CustomPagination';
import { DEFAULT_LIMIT } from '@/types/pagination';
import { useUpdateTeam } from '../hooks/useUpdateTeam';
import { useCreateTeam } from '../hooks/useCreateTeam';
import { toast } from 'sonner';
import { CreateTeamForm } from './CreateTeamForm';
import type { CreateTeam, UpdateTeam } from '@/types/team';
import { SUCCESS_MESSAGES } from '@/constants/messages';

export function TeamsTab() {
    const [page, setPage] = useState<number>(1);

    const {
        data: teamResponse,
        isPending,
        isError,
    } = useTeams({
        page,
        limit: DEFAULT_LIMIT,
    });

    const totalPages = teamResponse?.pagination.totalPages ?? 0;

    const { createTeam, isCreating } = useCreateTeam();
    const { updateTeam } = useUpdateTeam();

    const handleCreate = async (data: CreateTeam) => {
        await createTeam(data);
        toast.success(SUCCESS_MESSAGES.TEAM.CREATED);
    };

    const handleUpdate = async (id: string, data: UpdateTeam) => {
        await updateTeam({
            id,
            data,
        });

        toast.success(SUCCESS_MESSAGES.TEAM.UPDATED);
    };

    return (
        <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <div className="space-y-1">
                    <CardTitle className="text-[20px]">Teams List</CardTitle>
                    <CardDescription>Manage teams and their members.</CardDescription>
                </div>

                <CreateTeamForm onCreate={handleCreate} isCreating={isCreating} />
            </CardHeader>

            <CardContent>
                <TeamsTable isError={isError} isPending={isPending} data={teamResponse?.data ?? []} onUpdate={handleUpdate} />
                <CustomPagination currentPage={page} totalPages={totalPages} onPageChange={setPage} />
            </CardContent>
        </Card>
    );
}
