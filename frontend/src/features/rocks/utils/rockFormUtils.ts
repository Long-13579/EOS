import { ROCK_CATEGORY, ROCK_STATUS, type Quarter, type RockFormValues } from '../types/rock';

type RockFormMode = 'create' | 'edit';

export const ROCK_FORM_BASE_DEFAULTS: Omit<RockFormValues, 'ownerId' | 'quarterId' | 'year'> & {
    ownerId: string;
    quarterId: string;
    year: string;
} = {
    title: '',
    category: ROCK_CATEGORY.DEPARTMENT,
    ownerId: '',
    year: String(new Date().getFullYear()),
    quarterId: '',
    description: '',
    status: ROCK_STATUS.ON_TRACK,
    dueDate: new Date(),
};

interface ResolveRockFormDefaultsParams {
    mode: RockFormMode;
    initialData?: RockFormValues;
    currentDefaults: Pick<RockFormValues, 'year'> & { quarterId?: string };
}

export const resolveRockFormDefaults = ({ mode, initialData, currentDefaults }: ResolveRockFormDefaultsParams): RockFormValues => {
    const isCreateMode = mode === 'create' && !initialData;
    const normalizedCurrentDefaults: Pick<RockFormValues, 'year' | 'quarterId'> = {
        year: currentDefaults.year,
        quarterId: currentDefaults.quarterId ?? '',
    };

    return {
        ...ROCK_FORM_BASE_DEFAULTS,
        ...(isCreateMode ? normalizedCurrentDefaults : {}),
        ...initialData,
    };
};

export const findQuarterById = (quarters: Quarter[], quarterId?: string): Quarter | undefined => {
    if (!quarterId) {
        return undefined;
    }

    return quarters.find((quarter) => quarter.id === quarterId);
};

export const getSelectedYearValue = (year?: string): number | null => {
    return year ? Number(year) : null;
};
