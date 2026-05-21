import { type KeyboardEvent } from 'react';
import type { Headline, UpdateHeadline } from '../types/headline';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Save, X } from 'lucide-react';
import { useForm } from 'react-hook-form';
import { Field, FieldError } from '@/components/ui/field';
import { useFormError } from '@/hooks/useFormError';
import { normalizeApiError } from '@/utils/apiErrorNormalizer';
import { toast } from 'sonner';
import { zodResolver } from '@hookform/resolvers/zod';
import { headlineSchema } from '../schemas/headlineSchema';

interface HeadlineEditCellProps {
    headline: Headline;
    onCancelEdit: () => void;
    onUpdate: (id: string, data: UpdateHeadline) => Promise<Headline>;
}

export function HeadlineEditCell({ headline, onCancelEdit, onUpdate }: HeadlineEditCellProps) {
    const form = useForm<UpdateHeadline>({
        defaultValues: { title: headline.title },
        resolver: zodResolver(headlineSchema),
        mode: 'onChange',
    });

    const {
        register,
        handleSubmit,
        formState: { errors, isSubmitting, isValid },
    } = form;

    const handleFormError = useFormError(form);

    const onSubmit = async (data: UpdateHeadline) => {
        try {
            await onUpdate(headline.id, {
                ...data,
            });
        } catch (error) {
            const normalized = normalizeApiError(error);
            const isHandledByForm = handleFormError(normalized);

            if (!isHandledByForm) {
                toast.error(normalized.message);
            }
        }
    };

    const handleKeyDown = (e: KeyboardEvent<HTMLInputElement>) => {
        if (e.key === 'Escape') {
            e.stopPropagation();
            onCancelEdit();
        }

        if (e.key === 'Enter') {
            e.preventDefault();
            handleSubmit(onSubmit)();
        }
    };

    return (
        <div className="flex items-center gap-2 w-full">
            <div className="flex-1 min-w-0">
                <form onSubmit={handleSubmit(onSubmit)}>
                    <Field data-invalid={!!errors.title}>
                        <Input {...register('title')} onKeyDown={handleKeyDown} aria-invalid={!!errors.title} className="h-8 w-full" autoFocus />
                        {errors.title && <FieldError errors={[errors.title]} />}
                    </Field>
                </form>
            </div>

            <div className="flex items-center gap-2 shrink-0 ml-3">
                <Button size="icon-sm" onClick={handleSubmit(onSubmit)} disabled={isSubmitting || !isValid} aria-label="Save headline">
                    <Save className="h-4 w-4" />
                </Button>

                <Button size="icon-sm" variant="ghost" onClick={onCancelEdit} aria-label="Cancel editing headline">
                    <X className="h-4 w-4" />
                </Button>
            </div>
        </div>
    );
}
