import { toUtcStartOfDayISOString } from '@/utils/date';
import type { Rock, RockFormValues } from '../types/rock';
import { useCreateRock } from './useCreateRock';
import { useUpdateRock } from './useUpdateRock';
import { useState } from 'react';
import { useActiveTeamId } from '@/hooks/useActiveTeamId';

export function useRockDialog() {
    const activeTeamId = useActiveTeamId();
    const [isRockDialogOpen, setRockDialogOpen] = useState(false);
    const [editingRock, setEditingRock] = useState<Rock | undefined>(undefined);

    const openCreate = () => {
        setEditingRock(undefined);
        setRockDialogOpen(true);
    };

    const openUpdate = (rock: Rock) => {
        setEditingRock(rock);
        setRockDialogOpen(true);
    };

    const { createRock } = useCreateRock();
    const { updateRock } = useUpdateRock();

    const handleSubmit = async (data: RockFormValues) => {
        if (!activeTeamId || !data.dueDate) {
            return;
        }

        const payload = {
            title: data.title,
            description: data.description,
            status: data.status,
            category: data.category,
            dueDate: toUtcStartOfDayISOString(data.dueDate),
            year: Number(data.year),
            quarterId: data.quarterId,
            ownerId: data.ownerId,
        };

        if (editingRock) {
            await updateRock({
                id: editingRock.id,
                data: payload,
            });
        } else {
            await createRock({
                ...payload,
                teamId: activeTeamId,
            });
        }

        setRockDialogOpen(false);
    };

    return {
        isRockDialogOpen,
        setRockDialogOpen,
        editingRock,
        openCreate,
        openUpdate,
        handleSubmit,
    };
}
