import { useState } from 'react';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import type { Team, UpdateTeam } from '@/types/team';
import { Pencil } from 'lucide-react';
import type { DataTableProps } from '@/types/table';
import { TableActions } from '@/components/shared/Table/TableActions';
import { TeamEditCell } from './TeamEditCell';
import { TableQueryState } from '@/components/shared/Table';
import { ERROR_MESSAGES } from '@/constants/messages';
import { Badge } from '@/components/ui/badge';

export interface TeamsTableProps extends DataTableProps<Team> {
    isPending: boolean;
    isError: boolean;
    onUpdate: (id: string, data: UpdateTeam) => Promise<void>;
}

const getTeamActions = (team: Team, onStartEdit: (team: Team) => void) => {
    return [
        {
            label: 'Edit team',
            icon: Pencil,
            onClick: () => onStartEdit(team),
        },
    ];
};

export function TeamsTable({ data, isPending, isError, onUpdate }: TeamsTableProps) {
    const [editingTeamId, setEditingTeamId] = useState<string | null>(null);

    const handleStartEdit = (team: Team) => {
        setEditingTeamId(team.id);
    };

    const handleCancelEdit = () => {
        setEditingTeamId(null);
    };

    return (
        <div className="rounded-md border">
            <Table className="w-full table-fixed">
                <TableHeader className="[&_th]:font-bold">
                    <TableRow>
                        <TableHead className="pl-6 w-[400px]">Team Name</TableHead>
                        <TableHead className="text-right w-[140px] pr-8">Actions</TableHead>
                    </TableRow>
                </TableHeader>

                <TableBody>
                    <TableQueryState
                        isPending={isPending}
                        isError={isError}
                        isEmpty={data.length === 0}
                        colSpan={2}
                        errorMessage={ERROR_MESSAGES.TEAM.LOAD_FAILED}
                        emptyMessage={ERROR_MESSAGES.TEAM.NOT_FOUND}
                    >
                        {data.map((team) => {
                            const isEditing = team.id === editingTeamId;

                            return (
                                <TableRow key={team.id}>
                                    {isEditing ? (
                                        <TeamEditCell team={team} onCancelEdit={handleCancelEdit} onUpdate={onUpdate} />
                                    ) : (
                                        <>
                                            <TableCell className="pl-6">
                                                <div className="flex w-full flex-wrap justify-start gap-2">
                                                    <div className="font-medium max-w-[380px] truncate overflow-hidden" title={team.name}>
                                                        {team.name}
                                                    </div>
                                                    {team.isLeadership && (
                                                        <Badge variant="default" className="ml-2 h-5 px-1.5 text-xs">
                                                            Default
                                                        </Badge>
                                                    )}
                                                </div>
                                            </TableCell>

                                            <TableCell className="text-right pr-4.5">
                                                <TableActions actions={getTeamActions(team, handleStartEdit)} />
                                            </TableCell>
                                        </>
                                    )}
                                </TableRow>
                            );
                        })}
                    </TableQueryState>
                </TableBody>
            </Table>
        </div>
    );
}
