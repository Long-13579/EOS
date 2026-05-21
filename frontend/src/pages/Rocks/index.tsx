import { useState } from 'react';
import { Button } from '@/components/ui/button';
import { Plus } from 'lucide-react';
import { PageHeaderGroup } from '@/components/shared/PageHeaderGroup';
import { QueryState } from '@/components/shared/QueryState';
import { EmptyTeamState } from '@/components/shared/EmptyTeamState';
import { YearQuarterSelector, useRocks, RocksList, RockDialog, useRockDialog, useYearQuarter } from '@/features/rocks';
import { useActiveTeamId } from '@/hooks/useActiveTeamId';
import { ERROR_MESSAGES } from '@/constants/messages';
import { Checkbox } from '@/components/ui/checkbox';

export function Rocks() {
    const activeTeamId = useActiveTeamId();
    const { isRockDialogOpen, setRockDialogOpen, editingRock, openCreate, openUpdate, handleSubmit } = useRockDialog();

    const [yearId, setYearId] = useState<string>();
    const [quarterId, setQuarterId] = useState<string>();
    const [showArchived, setShowArchived] = useState(false);

    const {
        years,
        quarters,
        loadingYears,
        loadingQuarters,
        errorYears,
        errorQuarters,
        currentYearId,
        currentQuarterId,
        effectiveYearId,
        effectiveQuarterId,
    } = useYearQuarter(yearId, quarterId);

    const { data, isPending, isError } = useRocks({
        teamId: activeTeamId!,
        yearId: effectiveYearId,
        quarterId: effectiveQuarterId,
        isArchived: showArchived,
    });

    const isEmpty = !isPending && !isError && !data?.companyRocks?.length && !data?.departmentRocks.length && !data?.individualRocks.length;

    const emptyMessage = showArchived ? ERROR_MESSAGES.ROCK.ARCHIVED_NOT_FOUND : ERROR_MESSAGES.ROCK.NOT_FOUND;

    return (
        <div className="flex flex-col gap-6">
            <PageHeaderGroup title="Quarterly Rocks" description="Define and track your top priorities for this quarter.">
                <Button onClick={openCreate} disabled={!activeTeamId}>
                    <Plus className="mr-2 h-4 w-4" aria-hidden="true" />
                    Add Rock
                </Button>
            </PageHeaderGroup>

            <RockDialog editingRock={editingRock} isOpen={isRockDialogOpen} onOpenChange={setRockDialogOpen} onSubmit={handleSubmit} />

            {!activeTeamId ? (
                <EmptyTeamState />
            ) : (
                <>
                    <div className="flex justify-between rounded-lg bg-muted/30 p-4 border items-center gap-4">
                        <YearQuarterSelector
                            years={years}
                            quarters={quarters}
                            yearId={effectiveYearId}
                            quarterId={effectiveQuarterId}
                            currentYearId={currentYearId}
                            currentQuarterId={currentQuarterId}
                            onYearChange={setYearId}
                            onQuarterChange={setQuarterId}
                            loadingYears={loadingYears}
                            loadingQuarters={loadingQuarters}
                            errorYears={errorYears}
                            errorQuarters={errorQuarters}
                        />

                        <label htmlFor="show-archived-rock" className="flex items-center gap-2 text-sm font-medium cursor-pointer">
                            <Checkbox
                                id="show-archived-rock"
                                checked={showArchived}
                                onCheckedChange={(checked) => setShowArchived(Boolean(checked))}
                                disabled={isPending}
                            />
                            <span>Show archived</span>
                        </label>
                    </div>

                    <QueryState
                        isPending={isPending}
                        isError={isError}
                        isEmpty={isEmpty}
                        errorMessage={ERROR_MESSAGES.ROCK.LOAD_FAILED}
                        emptyMessage={emptyMessage}
                    >
                        <RocksList data={data} onUpdate={openUpdate} />
                    </QueryState>
                </>
            )}
        </div>
    );
}
