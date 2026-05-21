import { type KeyboardEvent } from 'react';
import type { Team, UpdateTeam } from '@/types/team';
import { TableCell } from '@/components/ui/table';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Save, X } from 'lucide-react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { updateTeamSchema } from '../schemas/teamSchema';
import { Field, FieldError } from '@/components/ui/field';
import { useFormError } from '@/hooks/useFormError';
import { normalizeApiError } from '@/utils/apiErrorNormalizer';
import { toast } from 'sonner';

interface TeamEditCellProps {
    team: Team;
    onCancelEdit: () => void;
    onUpdate: (id: string, data: UpdateTeam) => Promise<void>;
}

export function TeamEditCell({ team, onCancelEdit, onUpdate }: TeamEditCellProps) {
    const form = useForm<UpdateTeam>({
        resolver: zodResolver(updateTeamSchema),
        defaultValues: { name: team.name },
    });

    const {
        register,
        handleSubmit,
        formState: { errors, isSubmitting },
    } = form;
    const handleFormError = useFormError(form);

    const onSubmit = async (data: UpdateTeam) => {
        try {
            await onUpdate(team.id, data);
            onCancelEdit();
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
            onCancelEdit();
        }
    };

    return (
        <>
            <TableCell>
                <form onSubmit={handleSubmit(onSubmit)}>
                    <Field className="pl-3" data-invalid={!!errors.name}>
                        <Input {...register('name')} onKeyDown={handleKeyDown} aria-invalid={!!errors.name} className="h-8 max-w-[300px]" autoFocus />
                        {errors.name && <FieldError errors={[errors.name]} />}
                    </Field>
                </form>
            </TableCell>

            <TableCell className="text-right pr-4.5">
                <div className="flex items-center justify-end gap-2">
                    <Button size="icon-sm" onClick={handleSubmit(onSubmit)} aria-label="Save team name" disabled={isSubmitting}>
                        <Save className="h-4 w-4" />
                    </Button>

                    <Button
                        size="icon-sm"
                        variant="ghost"
                        onClick={onCancelEdit}
                        className="text-muted-foreground hover:text-red-600"
                        aria-label="Cancel editing"
                    >
                        <X className="h-4 w-4" />
                    </Button>
                </div>
            </TableCell>
        </>
    );
}
