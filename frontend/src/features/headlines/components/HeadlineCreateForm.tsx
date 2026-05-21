import { useId } from 'react';
import { useForm, Controller, type SubmitHandler } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Field, FieldError } from '@/components/ui/field';
import { headlineSchema } from '../schemas/headlineSchema';
import type { HeadlineFormValues } from '../types/headline';
import { toast } from 'sonner';
import { normalizeApiError } from '@/utils/apiErrorNormalizer';
import { useFormError } from '@/hooks/useFormError';
import { Plus } from 'lucide-react';

interface HeadlineCreateFormProps {
    onSubmit: (title: string) => Promise<void>;
}

export function HeadlineCreateForm({ onSubmit }: Readonly<HeadlineCreateFormProps>) {
    const formId = useId();

    const form = useForm<HeadlineFormValues>({
        resolver: zodResolver(headlineSchema),
        defaultValues: { title: '' },
    });

    const handleFormError = useFormError(form);
    const isSubmitting = form.formState.isSubmitting;

    const handleSubmit: SubmitHandler<HeadlineFormValues> = async (data) => {
        try {
            await onSubmit(data.title);
            form.reset();
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
        <form onSubmit={form.handleSubmit(handleSubmit)} className="flex gap-3 items-start" aria-busy={isSubmitting}>
            <Controller
                name="title"
                control={form.control}
                render={({ field, fieldState }) => (
                    <Field className="flex-1" data-invalid={fieldState.invalid}>
                        <Input
                            {...field}
                            id={`${formId}-title`}
                            placeholder="Add a new headline..."
                            disabled={isSubmitting}
                            aria-invalid={fieldState.invalid}
                            onBlur={() => {
                                field.onBlur();
                                form.clearErrors('title');
                            }}
                        />

                        <div className="min-h-5 w-full">{fieldState.error && <FieldError errors={[fieldState.error]} />}</div>
                    </Field>
                )}
            />

            <Button type="submit" disabled={isSubmitting}>
                <Plus className="mr-2 h-4 w-4" aria-hidden="true" />
                {isSubmitting ? 'Adding...' : 'Add'}
            </Button>
        </form>
    );
}
