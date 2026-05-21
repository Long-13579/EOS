import { useId } from 'react';
import { Controller } from 'react-hook-form';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import { DatePicker } from '@/components/shared/DatePicker';
import { Field, FieldError, FieldGroup, FieldLabel } from '@/components/ui/field';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { ROCK_STATUS, type RockFormValues, type RockStatus } from '../types/rock';
import { useRockForm } from '../hooks/useRockForm';
import { YearPicker } from '@/components/shared/YearPicker';
import { TeamMemberSelect } from '@/components/shared/TeamMemberSelect';
import { createSafeDate, getQuarterEndYear } from '../utils/rockDateUtils';
import { QuarterSelector } from './QuarterSelector';

interface RockFormProps {
    onSubmit: (data: RockFormValues) => void | Promise<void>;
    onCancel?: () => void;
    initialData?: RockFormValues;
    mode?: 'create' | 'edit';
    teamId?: string;
}

const STATUS_OPTIONS: Array<{ value: RockStatus; label: string }> = [
    { value: ROCK_STATUS.ON_TRACK, label: 'On Track' },
    { value: ROCK_STATUS.OFF_TRACK, label: 'Off Track' },
    { value: ROCK_STATUS.COMPLETED, label: 'Completed' },
    { value: ROCK_STATUS.DEFERRED, label: 'Deferred' },
];

export function RockForm({ onSubmit, onCancel, initialData, mode = 'create', teamId }: Readonly<RockFormProps>) {
    const formId = useId();
    const {
        form,
        handleFormSubmit,
        isSubmitting,
        quarters,
        categoryOptions,
        currentQuarterId,
        loadingQuarters,
        currentYear,
        dueDateRangeHint,
        selectedYear,
        selectedQuarter,
    } = useRockForm({
        onSubmit,
        initialData,
        mode,
    });

    const getHintMessage = () => {
        if (!selectedYear) {
            return 'Select a year first';
        }
        if (!selectedQuarter) {
            return 'Select a quarter first';
        }
        if (dueDateRangeHint) {
            return `Due date must be within ${dueDateRangeHint}.`;
        }
        return null;
    };

    const hint = getHintMessage();
    const isEditMode = mode === 'edit';

    const hasQuarters = !loadingQuarters && quarters.length > 0;

    const dueDateDisabledMatchers =
        selectedQuarter && selectedYear
            ? [
                  {
                      before: createSafeDate(selectedQuarter.startDate, String(selectedYear)),
                  },
                  {
                      after: createSafeDate(selectedQuarter.endDate, getQuarterEndYear(selectedQuarter, String(selectedYear))),
                  },
              ]
            : undefined;

    return (
        <form
            onSubmit={form.handleSubmit(handleFormSubmit)}
            className="space-y-6"
            aria-busy={isSubmitting}
            aria-label={isEditMode ? 'Edit Rock form' : 'Create Rock form'}
        >
            <FieldGroup className="gap-5">
                <Controller
                    name="title"
                    control={form.control}
                    render={({ field, fieldState }) => (
                        <Field data-invalid={fieldState.invalid}>
                            <FieldLabel htmlFor={`${formId}-title`}>
                                Title<span className="text-destructive">*</span>
                            </FieldLabel>

                            <Input
                                {...field}
                                id={`${formId}-title`}
                                placeholder="What is the 90-day priority?"
                                aria-invalid={fieldState.invalid}
                                disabled={isSubmitting}
                            />

                            {fieldState.error && <FieldError errors={[fieldState.error]} />}
                        </Field>
                    )}
                />

                <div className="grid gap-4 sm:grid-cols-2">
                    <div className="sm:col-span-1">
                        <Controller
                            name="category"
                            control={form.control}
                            render={({ field, fieldState }) => (
                                <Field data-invalid={fieldState.invalid}>
                                    <FieldLabel htmlFor={`${formId}-category`}>
                                        Category<span className="text-destructive">*</span>
                                    </FieldLabel>

                                    <Select value={field.value} onValueChange={field.onChange} disabled={isSubmitting}>
                                        <SelectTrigger id={`${formId}-category`}>
                                            <SelectValue placeholder="Select category" />
                                        </SelectTrigger>

                                        <SelectContent position="popper">
                                            {categoryOptions.map((category) => (
                                                <SelectItem key={category.value} value={category.value}>
                                                    {category.label}
                                                </SelectItem>
                                            ))}
                                        </SelectContent>
                                    </Select>

                                    {fieldState.error && <FieldError errors={[fieldState.error]} />}
                                </Field>
                            )}
                        />
                    </div>

                    <div className="sm:col-span-1">
                        <Controller
                            name="ownerId"
                            control={form.control}
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
                    </div>
                </div>

                <div className="grid gap-4 sm:grid-cols-3">
                    <div className="sm:col-span-1">
                        <Controller
                            name="year"
                            control={form.control}
                            render={({ field, fieldState }) => (
                                <Field data-invalid={fieldState.invalid}>
                                    <FieldLabel aria-label="Year">
                                        Year<span className="text-destructive">*</span>
                                    </FieldLabel>

                                    <YearPicker
                                        value={field.value ? Number(field.value) : undefined}
                                        onChange={(year) => {
                                            field.onChange(String(year));
                                            form.setValue('dueDate', null, { shouldValidate: true });
                                        }}
                                        disabled={isSubmitting}
                                    />

                                    {fieldState.error && <FieldError errors={[fieldState.error]} />}
                                </Field>
                            )}
                        />
                    </div>

                    <div className="sm:col-span-2">
                        <Controller
                            name="quarterId"
                            control={form.control}
                            render={({ field, fieldState }) => (
                                <Field data-invalid={fieldState.invalid}>
                                    <FieldLabel htmlFor={`${formId}-quarter`}>
                                        Quarter<span className="text-destructive">*</span>
                                    </FieldLabel>

                                    <Select
                                        value={field.value}
                                        onValueChange={(value) => {
                                            field.onChange(value);
                                            form.setValue('dueDate', null, { shouldValidate: true });
                                        }}
                                        disabled={isSubmitting || loadingQuarters || !selectedYear}
                                    >
                                        <SelectTrigger id={`${formId}-quarter`} disabled={!selectedYear || !hasQuarters}>
                                            <SelectValue
                                                placeholder={
                                                    selectedYear ? (hasQuarters ? 'Select quarter' : 'No quarters available') : 'Select year first'
                                                }
                                            />
                                        </SelectTrigger>

                                        <SelectContent position="popper">
                                            {hasQuarters &&
                                                quarters.map((quarter) => {
                                                    const isCurrent = selectedYear === currentYear?.year && quarter.id === currentQuarterId;
                                                    return <QuarterSelector key={quarter.id} quarter={quarter} isCurrent={isCurrent} />;
                                                })}
                                        </SelectContent>
                                    </Select>

                                    {fieldState.error && <FieldError errors={[fieldState.error]} />}
                                </Field>
                            )}
                        />
                    </div>
                </div>
                <div className="grid gap-4 sm:grid-cols-3">
                    <div className="sm:col-span-1">
                        <Controller
                            name="status"
                            control={form.control}
                            render={({ field, fieldState }) => (
                                <Field data-invalid={fieldState.invalid}>
                                    <FieldLabel htmlFor={`${formId}-status`}>
                                        Status<span className="text-destructive">*</span>
                                    </FieldLabel>

                                    <Select value={field.value} onValueChange={field.onChange} disabled={isSubmitting}>
                                        <SelectTrigger id={`${formId}-status`}>
                                            <SelectValue placeholder="Select status" />
                                        </SelectTrigger>

                                        <SelectContent position="popper">
                                            {STATUS_OPTIONS.map((status) => (
                                                <SelectItem key={status.value} value={status.value}>
                                                    {status.label}
                                                </SelectItem>
                                            ))}
                                        </SelectContent>
                                    </Select>

                                    {fieldState.error && <FieldError errors={[fieldState.error]} />}
                                </Field>
                            )}
                        />
                    </div>

                    <div className="sm:col-span-2">
                        <Controller
                            name="dueDate"
                            control={form.control}
                            render={({ field, fieldState }) => (
                                <Field data-invalid={fieldState.invalid}>
                                    <FieldLabel htmlFor={`${formId}-dueDate`}>
                                        Due Date<span className="text-destructive">*</span>
                                    </FieldLabel>

                                    <DatePicker
                                        id={`${formId}-dueDate`}
                                        value={field.value}
                                        onChange={(value) => field.onChange(value ?? null)}
                                        isDisabled={!selectedYear || !selectedQuarter || isSubmitting}
                                        disabledDays={dueDateDisabledMatchers}
                                    />
                                    {!fieldState.error ? (
                                        hint && <p className="text-xs text-muted-foreground">{hint}</p>
                                    ) : (
                                        <FieldError errors={[fieldState.error]} className="text-xs text-destructive" />
                                    )}
                                </Field>
                            )}
                        />
                    </div>
                </div>

                <Controller
                    name="description"
                    control={form.control}
                    render={({ field, fieldState }) => (
                        <Field data-invalid={fieldState.invalid}>
                            <FieldLabel htmlFor={`${formId}-description`}>
                                Description<span className="text-destructive">*</span>
                            </FieldLabel>

                            <Textarea
                                {...field}
                                id={`${formId}-description`}
                                rows={4}
                                className="max-h-[120px] overflow-y-auto resize-none"
                                placeholder="Notes, context, or collaborators..."
                                disabled={isSubmitting}
                                aria-invalid={fieldState.invalid || undefined}
                            />

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
                            form.reset();
                            onCancel();
                        }}
                        disabled={isSubmitting}
                    >
                        Cancel
                    </Button>
                )}

                <Button type="submit" disabled={isSubmitting || !form.formState.isValid}>
                    {isSubmitting ? 'Saving...' : isEditMode ? 'Update Rock' : 'Create Rock'}
                </Button>
            </div>
        </form>
    );
}
