import { useCallback, useMemo } from 'react';
import { zodResolver } from '@hookform/resolvers/zod';
import { useForm, useWatch, type DefaultValues, type Resolver, type ResolverResult, type SubmitHandler } from 'react-hook-form';

import { useFormError } from '@/hooks/useFormError';
import { normalizeApiError } from '@/utils/apiErrorNormalizer';

import { rockFormSchema } from '../schemas/rockSchema';
import { ROCK_CATEGORY, type RockCategory, type RockFormValues } from '../types/rock';
import { formatQuarterRangeHint, getDueDateQuarterValidationMessage } from '../utils/rockDateUtils';
import { findQuarterById, getSelectedYearValue, resolveRockFormDefaults } from '../utils/rockFormUtils';
import { useYearQuarterDefaults } from './useYearQuarterDefaults';
import { useIsLeadershipTeam } from '@/hooks/useIsLeadershipTeam';

interface RockFormProps {
    onSubmit: (data: RockFormValues) => void | Promise<void>;
    initialData?: RockFormValues;
    mode?: 'create' | 'edit';
}

const getDueDateErrorMessage = (
    dueDate: Date | null | undefined,
    quarterId: string | undefined,
    year: string | number | null | undefined,
    quarters: ReturnType<typeof useYearQuarterDefaults>['quarters'],
) => {
    const quarter = findQuarterById(quarters, quarterId);

    if (!quarter || !year) {
        return null;
    }

    if (!dueDate) {
        return `Due date must be within ${formatQuarterRangeHint(quarter, String(year))}.`;
    }

    return getDueDateQuarterValidationMessage(dueDate, quarter, year);
};

const buildDueDateValidationErrorResult = (errors: ResolverResult<RockFormValues>['errors'], message: string): ResolverResult<RockFormValues> => ({
    values: {} as Record<string, never>,
    errors: {
        ...errors,
        dueDate: {
            type: 'validate',
            message,
        },
    },
});

const CATEGORY_OPTIONS: Array<{ value: RockCategory; label: string }> = [
    { value: ROCK_CATEGORY.COMPANY, label: 'Company Rock' },
    { value: ROCK_CATEGORY.DEPARTMENT, label: 'Department Rock' },
    { value: ROCK_CATEGORY.INDIVIDUAL, label: 'Individual Rock' },
];

const getAllowedRockCategoryOptions = (isLeadershipTeam: boolean | undefined) => {
    if (isLeadershipTeam) {
        return CATEGORY_OPTIONS;
    }

    return CATEGORY_OPTIONS.filter((option) => option.value !== ROCK_CATEGORY.COMPANY);
};

export function useRockForm({ onSubmit, initialData, mode = 'create' }: Readonly<RockFormProps>) {
    const { quarters, loadingQuarters, currentYear, currentQuarterId } = useYearQuarterDefaults();
    const isLeadershipTeam = useIsLeadershipTeam();
    const categoryOptions = getAllowedRockCategoryOptions(isLeadershipTeam);

    const currentDefaults = useMemo(() => {
        return {
            year: currentYear ? String(currentYear.year) : new Date().getFullYear().toString(),
            quarterId: currentQuarterId,
        };
    }, [currentYear, currentQuarterId]);

    const resolvedDefaultValues: DefaultValues<RockFormValues> = useMemo(
        () => resolveRockFormDefaults({ mode, initialData, currentDefaults }),
        [mode, initialData, currentDefaults],
    );

    const getFormDueDateErrorMessage = useCallback(
        (values: Pick<RockFormValues, 'dueDate' | 'quarterId' | 'year'>) =>
            getDueDateErrorMessage(values.dueDate, values.quarterId, values.year, quarters),
        [quarters],
    );

    const resolver: Resolver<RockFormValues> = useMemo(() => {
        const baseResolver = zodResolver(rockFormSchema);

        return async (values, context, options) => {
            const result = await baseResolver(values, context, options);
            const dueDateErrorMessage = getFormDueDateErrorMessage(values);

            if (!dueDateErrorMessage) {
                return result;
            }

            return buildDueDateValidationErrorResult(result.errors, dueDateErrorMessage);
        };
    }, [getFormDueDateErrorMessage]);

    const form = useForm<RockFormValues>({
        resolver,
        defaultValues: resolvedDefaultValues,
        mode: 'onChange',
    });

    const handleFormError = useFormError(form);

    const { isSubmitting } = form.formState;

    const watchedYear = useWatch({ control: form.control, name: 'year' });
    const watchedQuarterId = useWatch({ control: form.control, name: 'quarterId' });

    const selectedQuarter = useMemo(() => findQuarterById(quarters, watchedQuarterId), [quarters, watchedQuarterId]);
    const selectedYear = useMemo(() => getSelectedYearValue(watchedYear), [watchedYear]);

    const dueDateRangeHint = useMemo(() => {
        if (!selectedQuarter || !selectedYear) {
            return null;
        }

        return formatQuarterRangeHint(selectedQuarter, selectedYear.toString());
    }, [selectedQuarter, selectedYear]);

    const setManualError = useCallback(
        (field: 'category' | 'dueDate', message: string) => {
            form.setError(field, {
                type: 'manual',
                message,
            });
        },
        [form],
    );

    const shouldBlockSubmit = useCallback(
        (data: RockFormValues) => {
            const quarter = findQuarterById(quarters, data.quarterId);

            if (!quarter || !data.dueDate) {
                return true;
            }

            const dueDateErrorMessage = getFormDueDateErrorMessage(data);

            if (dueDateErrorMessage) {
                setManualError('dueDate', dueDateErrorMessage);
                return true;
            }

            if (data.category === ROCK_CATEGORY.COMPANY && !isLeadershipTeam) {
                setManualError('category', 'Only leadership team can create company rocks.');
                return true;
            }

            return false;
        },
        [getFormDueDateErrorMessage, quarters, setManualError, isLeadershipTeam],
    );

    const handleFormSubmit: SubmitHandler<RockFormValues> = async (data) => {
        try {
            if (shouldBlockSubmit(data)) {
                return;
            }

            await onSubmit(data);
        } catch (error) {
            const normalized = normalizeApiError(error);
            handleFormError(normalized);
        }
    };

    return {
        form,
        handleFormSubmit,
        isSubmitting,
        categoryOptions,
        currentYear,
        currentQuarterId,
        loadingQuarters,
        quarters,
        dueDateRangeHint,
        selectedYear,
        selectedQuarter,
    };
}
