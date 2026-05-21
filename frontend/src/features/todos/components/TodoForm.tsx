import { useId } from 'react';
import { useForm, Controller, type SubmitHandler } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { LayoutList } from 'lucide-react';

import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { FieldGroup, Field, FieldLabel, FieldError } from '@/components/ui/field';
import { DatePicker } from '@/components/shared/DatePicker';

import { todoFormSchema } from '@/features/todos/schemas/todoSchema';
import type { TodoFormValues } from '../types/todo';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Textarea } from '@/components/ui/textarea';
import { toast } from 'sonner';
import { normalizeApiError } from '@/utils/apiErrorNormalizer';
import { useFormError } from '@/hooks/useFormError';
import { MultipleTeamMembersSelect } from '@/components/shared/MultipleTeamMembersSelect';

interface TodoFormProps {
    onSubmit: (data: TodoFormValues) => void | Promise<void>;
    onCancel?: () => void;
    initialData?: TodoFormValues;
    mode?: 'create' | 'edit';
    teamId?: string;
}

const defaultValues: TodoFormValues = {
    title: '',
    status: 'NOT_STARTED',
    assigneeIds: [],
};

const STATUS_OPTIONS = [
    { value: 'NOT_STARTED', label: 'Not Started' },
    { value: 'IN_PROGRESS', label: 'In Progress' },
    { value: 'COMPLETED', label: 'Completed' },
];

export function TodoForm({ onSubmit, onCancel, initialData, mode = 'create', teamId }: Readonly<TodoFormProps>) {
    const formId = useId();

    const form = useForm<TodoFormValues>({
        resolver: zodResolver(todoFormSchema),
        defaultValues: { ...defaultValues, ...initialData },
        mode: 'onSubmit',
    });

    const { reset } = form;
    const handleFormError = useFormError(form);
    const isSubmitting = form.formState.isSubmitting;

    const handleSubmit: SubmitHandler<TodoFormValues> = async (data) => {
        try {
            await onSubmit(data);

            if (mode === 'create') {
                reset(defaultValues);
            }
        } catch (error) {
            const normalized = normalizeApiError(error);
            const isHandledByForm = handleFormError(normalized);

            if (!isHandledByForm) {
                toast.error(normalized.message);
            }
        }
    };

    return (
        <form
            onSubmit={form.handleSubmit(handleSubmit)}
            className="space-y-6"
            aria-busy={isSubmitting}
            aria-label={mode === 'edit' ? 'Edit To-do form' : 'Create To-do form'}
        >
            <FieldGroup className="gap-5">
                <Controller
                    name="title"
                    control={form.control}
                    render={({ field, fieldState }) => (
                        <Field data-invalid={fieldState.invalid}>
                            <FieldLabel htmlFor={`${formId}-title`}>
                                Title <span className="text-destructive">*</span>
                            </FieldLabel>

                            <Input
                                {...field}
                                id={`${formId}-title`}
                                placeholder="What needs to be done?"
                                aria-invalid={fieldState.invalid}
                                disabled={isSubmitting}
                            />

                            {fieldState.error && <FieldError errors={[fieldState.error]} />}
                        </Field>
                    )}
                />

                <div className="grid gap-4 sm:grid-cols-2">
                    <Controller
                        name="status"
                        control={form.control}
                        render={({ field, fieldState }) => (
                            <Field data-invalid={fieldState.invalid}>
                                <FieldLabel htmlFor={`${formId}-status`}>Status</FieldLabel>

                                <Select value={field.value} onValueChange={field.onChange} disabled={isSubmitting}>
                                    <SelectTrigger id={`${formId}-status`} className="relative h-9 w-full pl-9">
                                        <LayoutList className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
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

                    <Controller
                        name="dueDate"
                        control={form.control}
                        render={({ field, fieldState }) => (
                            <Field data-invalid={fieldState.invalid}>
                                <FieldLabel htmlFor={`${formId}-dueDate`}>Due Date</FieldLabel>

                                <DatePicker id={`${formId}-dueDate`} value={field.value} onChange={(val) => field.onChange(val ?? undefined)} />

                                {fieldState.error && <FieldError errors={[fieldState.error]} />}
                            </Field>
                        )}
                    />
                </div>

                <Controller
                    name="description"
                    control={form.control}
                    render={({ field, fieldState }) => (
                        <Field data-invalid={fieldState.invalid}>
                            <FieldLabel htmlFor={`${formId}-description`}>Description</FieldLabel>

                            <Textarea
                                {...field}
                                id={`${formId}-description`}
                                rows={4}
                                className="max-h-[120px] overflow-y-auto resize-none"
                                placeholder="Add details..."
                                disabled={isSubmitting}
                                aria-invalid={fieldState.invalid || undefined}
                            />

                            {fieldState.error && <FieldError errors={[fieldState.error]} />}
                        </Field>
                    )}
                />

                <Controller
                    name="assigneeIds"
                    control={form.control}
                    render={({ field, fieldState }) => (
                        <Field data-invalid={fieldState.invalid}>
                            <FieldLabel htmlFor={`${formId}-assignee`}>Assignees</FieldLabel>

                            <MultipleTeamMembersSelect value={field.value} onChange={field.onChange} disabled={isSubmitting} teamId={teamId} />

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
                            reset({ ...defaultValues, ...initialData });
                            onCancel();
                        }}
                        disabled={isSubmitting}
                    >
                        Cancel
                    </Button>
                )}

                <Button type="submit" disabled={isSubmitting}>
                    {isSubmitting ? (mode === 'edit' ? 'Updating...' : 'Creating...') : mode === 'edit' ? 'Update To-do' : 'Create To-do'}
                </Button>
            </div>
        </form>
    );
}
