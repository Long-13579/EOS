import { useId } from 'react';
import { Controller, type ControllerRenderProps, type SubmitHandler, useForm, useWatch } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';

import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Field, FieldError, FieldGroup, FieldLabel } from '@/components/ui/field';
import { useFormError } from '@/hooks/useFormError';
import { normalizeApiError } from '@/utils/apiErrorNormalizer';
import { METRIC_OPERATOR, METRIC_UNIT, type MetricFormValues } from '../types/metric';
import { metricFormSchema } from '../schemas/metricSchema';
import { TeamMemberSelect } from '@/components/shared/TeamMemberSelect';

interface MetricFormProps {
    onSubmit: (data: MetricFormValues) => void | Promise<void>;
    onCancel?: () => void;
    initialData?: MetricFormValues;
    teamId?: string;
    mode?: 'create' | 'edit';
}

const DEFAULT_VALUES: MetricFormValues = {
    name: '',
    goal: '0',
    unit: METRIC_UNIT.NUMBER,
    operator: METRIC_OPERATOR.GREATER_THAN_OR_EQUAL_TO,
    ownerId: '',
};

const UNIT_OPTIONS = [
    { value: METRIC_UNIT.NUMBER, label: 'Number' },
    { value: METRIC_UNIT.CURRENCY, label: 'Currency ($)' },
    { value: METRIC_UNIT.PERCENTAGE, label: 'Percentage (%)' },
    { value: METRIC_UNIT.YES_NO, label: 'Yes/No' },
    { value: METRIC_UNIT.RYG_STATUS, label: 'RYG (Red/Yellow/Green)' },
] as const;

const NUMERIC_OPERATOR_OPTIONS = [
    { value: METRIC_OPERATOR.GREATER_THAN, label: 'Greater than goal' },
    { value: METRIC_OPERATOR.GREATER_THAN_OR_EQUAL_TO, label: 'Greater than or equal to goal' },
    { value: METRIC_OPERATOR.LESS_THAN, label: 'Less than goal' },
    { value: METRIC_OPERATOR.LESS_THAN_OR_EQUAL_TO, label: 'Less than or equal to goal' },
    { value: METRIC_OPERATOR.EQUAL_TO, label: 'Equal to goal' },
] as const;

const YES_NO_OPTIONS = [
    { value: 'YES', label: 'Yes' },
    { value: 'NO', label: 'No' },
] as const;

const RYG_OPTIONS = [
    { value: 'RED', label: 'Red' },
    { value: 'YELLOW', label: 'Yellow' },
    { value: 'GREEN', label: 'Green' },
] as const;

const GOAL_OPTIONS_MAP: Partial<Record<MetricFormValues['unit'], readonly { value: string; label: string }[]>> = {
    [METRIC_UNIT.YES_NO]: YES_NO_OPTIONS,
    [METRIC_UNIT.RYG_STATUS]: RYG_OPTIONS,
};

function isCategoricalUnit(unit: MetricFormValues['unit']) {
    return unit === METRIC_UNIT.YES_NO || unit === METRIC_UNIT.RYG_STATUS;
}

function isNumericUnit(unit: MetricFormValues['unit']) {
    return unit === METRIC_UNIT.NUMBER || unit === METRIC_UNIT.CURRENCY || unit === METRIC_UNIT.PERCENTAGE;
}

function getGoalOptions(unit: MetricFormValues['unit']) {
    return GOAL_OPTIONS_MAP[unit] ?? [];
}

export function MetricForm({ onSubmit, onCancel, initialData, mode = 'create', teamId }: Readonly<MetricFormProps>) {
    const formId = useId();
    const form = useForm<MetricFormValues>({
        resolver: zodResolver(metricFormSchema),
        defaultValues: {
            ...DEFAULT_VALUES,
            ...initialData,
        },
        mode: 'onChange',
    });

    const { control, formState, setValue } = form;
    const handleFormError = useFormError(form);
    const isSubmitting = formState.isSubmitting;
    const unit = useWatch({ control, name: 'unit' });
    const isEditMode = mode === 'edit';

    const handleUnitChange = (field: ControllerRenderProps<MetricFormValues, 'unit'>, newUnit: MetricFormValues['unit']) => {
        field.onChange(newUnit);
        setValue('goal', '', {
            shouldDirty: true,
        });

        if (isCategoricalUnit(newUnit)) {
            setValue('operator', undefined, {
                shouldValidate: true,
                shouldDirty: true,
            });
        }
    };

    const goalOptions = getGoalOptions(unit);

    const handleSubmit: SubmitHandler<MetricFormValues> = async (data) => {
        try {
            await onSubmit({
                ...data,
                name: data.name.trim(),
                goal: data.goal.trim(),
            });
        } catch (error) {
            const normalized = normalizeApiError(error);
            handleFormError(normalized);
        }
    };

    return (
        <form
            onSubmit={form.handleSubmit(handleSubmit)}
            className="space-y-6"
            aria-busy={isSubmitting}
            aria-label={isEditMode ? 'Edit Metric form' : 'Create Metric form'}
        >
            <FieldGroup className="gap-5">
                <Controller
                    name="name"
                    control={control}
                    render={({ field, fieldState }) => (
                        <Field data-invalid={fieldState.invalid}>
                            <FieldLabel htmlFor={`${formId}-name`}>
                                Metric Name<span className="text-destructive">*</span>
                            </FieldLabel>

                            <Input
                                {...field}
                                id={`${formId}-name`}
                                placeholder="e.g. New Customers"
                                disabled={isSubmitting}
                                aria-invalid={fieldState.invalid}
                                aria-required="true"
                            />

                            {fieldState.error && <FieldError errors={[fieldState.error]} />}
                        </Field>
                    )}
                />

                <div className="grid gap-4 lg:grid-cols-2">
                    <Controller
                        name="unit"
                        control={control}
                        render={({ field, fieldState }) => (
                            <Field data-invalid={fieldState.invalid}>
                                <FieldLabel htmlFor={`${formId}-unit`}>
                                    Unit<span className="text-destructive">*</span>
                                </FieldLabel>

                                <Select
                                    value={field.value}
                                    onValueChange={(newUnit: MetricFormValues['unit']) => handleUnitChange(field, newUnit)}
                                    disabled={isSubmitting || isEditMode}
                                    aria-required="true"
                                >
                                    <SelectTrigger id={`${formId}-unit`} className="w-full">
                                        <SelectValue placeholder="Select a unit" />
                                    </SelectTrigger>

                                    <SelectContent position="popper">
                                        {UNIT_OPTIONS.map((option) => (
                                            <SelectItem key={option.value} value={option.value}>
                                                {option.label}
                                            </SelectItem>
                                        ))}
                                    </SelectContent>
                                </Select>

                                {fieldState.error && <FieldError errors={[fieldState.error]} />}
                            </Field>
                        )}
                    />

                    <Controller
                        name="goal"
                        control={control}
                        render={({ field, fieldState }) => (
                            <Field data-invalid={fieldState.invalid}>
                                <FieldLabel htmlFor={`${formId}-goal`}>
                                    Goal<span className="text-destructive">*</span>
                                </FieldLabel>

                                {goalOptions.length > 0 ? (
                                    <Select value={field.value} onValueChange={field.onChange} disabled={isSubmitting} aria-required="true">
                                        <SelectTrigger id={`${formId}-goal`} className="w-full">
                                            <SelectValue placeholder="Select a goal" />
                                        </SelectTrigger>

                                        <SelectContent position="popper">
                                            {goalOptions.map((option) => (
                                                <SelectItem key={option.value} value={option.value}>
                                                    {option.label}
                                                </SelectItem>
                                            ))}
                                        </SelectContent>
                                    </Select>
                                ) : (
                                    <Input
                                        {...field}
                                        id={`${formId}-goal`}
                                        placeholder="0"
                                        type="number"
                                        inputMode="numeric"
                                        step={isNumericUnit(unit) && (unit === METRIC_UNIT.CURRENCY || unit === METRIC_UNIT.PERCENTAGE) ? 0.01 : 1}
                                        disabled={isSubmitting}
                                        aria-invalid={fieldState.invalid}
                                    />
                                )}

                                {fieldState.error && <FieldError errors={[fieldState.error]} />}
                            </Field>
                        )}
                    />
                </div>

                {!isCategoricalUnit(unit) && (
                    <Controller
                        name="operator"
                        control={control}
                        render={({ field, fieldState }) => (
                            <Field data-invalid={fieldState.invalid}>
                                <FieldLabel htmlFor={`${formId}-operator`}>
                                    Operator<span className="text-destructive">*</span>
                                </FieldLabel>

                                <Select value={field.value} onValueChange={field.onChange} disabled={isSubmitting}>
                                    <SelectTrigger id={`${formId}-operator`} className="w-full">
                                        <SelectValue placeholder="Select an operator" />
                                    </SelectTrigger>

                                    <SelectContent position="popper">
                                        {NUMERIC_OPERATOR_OPTIONS.map((option) => (
                                            <SelectItem key={option.value} value={option.value}>
                                                {option.label}
                                            </SelectItem>
                                        ))}
                                    </SelectContent>
                                </Select>

                                {fieldState.error && <FieldError errors={[fieldState.error]} />}
                            </Field>
                        )}
                    />
                )}

                <Controller
                    name="ownerId"
                    control={control}
                    render={({ field, fieldState }) => (
                        <Field data-invalid={fieldState.invalid}>
                            <FieldLabel htmlFor={`${formId}-owner`}>
                                Owner<span className="text-destructive">*</span>
                            </FieldLabel>

                            <div className="relative">
                                <TeamMemberSelect value={field.value} onChange={field.onChange} disabled={isSubmitting} teamId={teamId} />
                            </div>

                            {fieldState.error && <FieldError errors={[fieldState.error]} />}
                        </Field>
                    )}
                />
            </FieldGroup>

            <div className="flex justify-end gap-3">
                {onCancel && (
                    <Button
                        type="button"
                        variant="outline"
                        onClick={() => {
                            onCancel();
                        }}
                        disabled={isSubmitting}
                    >
                        Cancel
                    </Button>
                )}

                <Button type="submit" disabled={!form.formState.isValid || isSubmitting}>
                    {isSubmitting ? 'Saving...' : isEditMode ? 'Update Metric' : 'Create Metric'}
                </Button>
            </div>
        </form>
    );
}
