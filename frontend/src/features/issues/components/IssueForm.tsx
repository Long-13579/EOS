import { useId } from 'react';
import { useForm, Controller, type SubmitHandler } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { FieldGroup, Field, FieldLabel, FieldError } from '@/components/ui/field';
import { Textarea } from '@/components/ui/textarea';
import { toast } from 'sonner';
import { normalizeApiError } from '@/utils/apiErrorNormalizer';
import { useFormError } from '@/hooks/useFormError';
import { issueFormSchema } from '../schemas/issueSchema';
import type { IssueFormValues } from '../types/issue';
import { IssueTypeSelect } from './IssueTypeSelect';

interface IssueFormProps {
    onSubmit: (data: IssueFormValues) => void | Promise<void>;
    onCancel?: () => void;
    initialData: IssueFormValues | null;
    mode?: 'create' | 'edit';
}

const defaultValues: IssueFormValues = {
    title: '',
    description: '',
    issueTypeId: null,
};

export function IssueForm({ onSubmit, onCancel, initialData, mode = 'create' }: Readonly<IssueFormProps>) {
    const formId = useId();

    const form = useForm<IssueFormValues>({
        resolver: zodResolver(issueFormSchema),
        defaultValues: { ...defaultValues, ...initialData },
        mode: 'onSubmit',
    });

    const { reset } = form;
    const handleFormError = useFormError(form);
    const isSubmitting = form.formState.isSubmitting;

    const handleSubmit: SubmitHandler<IssueFormValues> = async (data) => {
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

            throw error;
        }
    };

    return (
        <form
            onSubmit={form.handleSubmit(handleSubmit)}
            className="space-y-6"
            aria-busy={isSubmitting}
            aria-label={mode === 'edit' ? 'Edit issue form' : 'Create issue form'}
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
                                placeholder="Issue title"
                                aria-invalid={fieldState.invalid}
                                disabled={isSubmitting}
                            />

                            {fieldState.error && <FieldError errors={[fieldState.error]} />}
                        </Field>
                    )}
                />
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
                                placeholder="Add issue details..."
                                className="max-h-[120px] overflow-y-auto resize-none"
                                disabled={isSubmitting}
                                aria-invalid={fieldState.invalid || undefined}
                            />

                            {fieldState.error && <FieldError errors={[fieldState.error]} />}
                        </Field>
                    )}
                />

                <Controller
                    name="issueTypeId"
                    control={form.control}
                    render={({ field, fieldState }) => (
                        <IssueTypeSelect field={field} error={fieldState.error} disabled={isSubmitting} inputId={`${formId}-issueType`} />
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
                    {isSubmitting ? (mode === 'edit' ? 'Updating...' : 'Creating...') : mode === 'edit' ? 'Update Issue' : 'Create Issue'}
                </Button>
            </div>
        </form>
    );
}
