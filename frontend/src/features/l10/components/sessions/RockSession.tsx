import { useActiveTeamId } from '@/hooks/useActiveTeamId';
import { Plus } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { useYearQuarter } from '@/features/rocks/hooks/useYearQuarter';
import { useRocks } from '@/features/rocks/hooks/useRocks';
import { useRockDialog } from '@/features/rocks/hooks/useRockDialog';
import { RocksList } from '@/features/rocks/components/RocksList';
import { RockDialog } from '@/features/rocks/components/RockDialog';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import type { Rock } from '@/features/rocks/types/rock';

export function RockSession() {
    const teamId = useActiveTeamId();
    const { effectiveYearId, effectiveQuarterId, loadingYears, loadingQuarters } = useYearQuarter();
    const { isRockDialogOpen, setRockDialogOpen, editingRock, openCreate, openUpdate, handleSubmit } = useRockDialog();
    const { data: rocks } = useRocks({
        teamId: teamId!,
        yearId: effectiveYearId!,
        quarterId: effectiveQuarterId!,
    });

    const handleUpdate = (rock: Rock) => {
        openUpdate(rock);
    };

    if (loadingYears || loadingQuarters) {
        return null;
    }

    return (
        <>
            <Card>
                <CardHeader>
                    <div className="flex items-center justify-between">
                        <CardTitle>Rocks</CardTitle>
                        <Button type="button" size="sm" onClick={openCreate}>
                            <Plus className="mr-1 h-4 w-4" />
                            Add Rock
                        </Button>
                    </div>
                </CardHeader>
                <CardContent>
                    <RocksList data={rocks} onUpdate={handleUpdate} />
                </CardContent>
            </Card>
            <RockDialog isOpen={isRockDialogOpen} onOpenChange={setRockDialogOpen} onSubmit={handleSubmit} editingRock={editingRock} />
        </>
    );
}
