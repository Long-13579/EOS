import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Field, FieldError } from '@/components/ui/field';
import { Plus } from 'lucide-react';
import { useForm, useWatch } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { createTeamSchema } from '../schemas/teamSchema';
import type { CreateTeam } from '@/types/team';
import { useFormError } from '@/hooks/useFormError';
import { normalizeApiError } from '@/utils/apiErrorNormalizer';
import { toast } from 'sonner';

interface CreateTeamFormProps {
    onCreate: (data: CreateTeam) => Promise<void>;
    isCreating: boolean;
}

export function CreateTeamForm({ onCreate, isCreating }: Readonly<CreateTeamFormProps>) {
    const form = useForm<CreateTeam>({
        resolver: zodResolver(createTeamSchema),
        defaultValues: { name: '' },
    });

    const {
        register,
        handleSubmit,
        formState: { errors },
        reset,
    } = form;

    const handleFormError = useFormError(form);

    const teamName = useWatch({
        control: form.control,
        name: 'name',
    });

    const onSubmit = async (data: CreateTeam) => {
        try {
            await onCreate(data);
            reset();
        } catch (error) {
            const normalized = normalizeApiError(error);
            const isHandledByForm = handleFormError(normalized);

            if (!isHandledByForm) {
                toast.error(normalized.message);
            }
        }
    };

    const nameField = register('name');

    return (
        <form onSubmit={handleSubmit(onSubmit)} className="flex space-x-2 w-100">
            <Field className="flex-1 w-full" data-invalid={!!errors.name}>
                <Input
                    className="w-full"
                    id="team-name"
                    placeholder="Team name..."
                    {...nameField}
                    aria-invalid={!!errors.name}
                    onBlur={(e) => {
                        nameField.onBlur(e);
                        form.clearErrors('name');
                    }}
                />
                <div className="min-h-5 w-full">{errors.name && <FieldError errors={[errors.name]} />}</div>
            </Field>

            <Button type="submit" disabled={isCreating || !teamName?.trim()}>
                <Plus className="mr-2 h-4 w-4" />
                {isCreating ? 'Creating...' : 'Create Team'}
            </Button>
        </form>
    );
}
