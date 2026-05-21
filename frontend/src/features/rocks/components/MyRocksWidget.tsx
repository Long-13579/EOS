import { useState } from 'react';
import { QueryState } from '@/components/shared/QueryState';
import { ERROR_MESSAGES } from '@/constants/messages';
import { useRockDialog } from '../hooks/useRockDialog';
import { YearQuarterSelector } from './YearQuarterSelector';
import { MyRocksList } from './MyRocksList';
import { RockDialog } from './RockDialog';
import { useMyRocks } from '../hooks/useMyRocks';
import { useYearQuarter } from '../hooks/useYearQuarter';

export function MyRocksWidget() {
    const [yearId, setYearId] = useState<string>();
    const [quarterId, setQuarterId] = useState<string>();

    const { isRockDialogOpen, setRockDialogOpen, editingRock, openUpdate, handleSubmit } = useRockDialog();

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

    const {
        data: myRocksData,
        isPending,
        isError,
    } = useMyRocks({
        yearId: effectiveYearId,
        quarterId: effectiveQuarterId,
    });

    const isEmpty = !isPending && !isError && !myRocksData.data.length;

    return (
        <div className="rounded-xl border bg-card shadow-sm p-5 space-y-4">
            <h2 className="text-lg font-semibold">My Rocks</h2>

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
            </div>

            <QueryState
                isPending={isPending}
                isError={isError}
                isEmpty={isEmpty}
                errorMessage={ERROR_MESSAGES.ROCK.LOAD_FAILED}
                emptyMessage={ERROR_MESSAGES.ROCK.NOT_FOUND}
            >
                <MyRocksList data={myRocksData?.data} onUpdate={openUpdate} />
            </QueryState>

            <RockDialog editingRock={editingRock} isOpen={isRockDialogOpen} onOpenChange={setRockDialogOpen} onSubmit={handleSubmit} />
        </div>
    );
}
