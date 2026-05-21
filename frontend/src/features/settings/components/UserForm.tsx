import { useId } from 'react';

import { toast } from 'sonner';
import { useForm, Controller, type SubmitHandler } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Button } from '@/components/ui/button';
import { DialogFooter } from '@/components/ui/dialog';
import { Input } from '@/components/ui/input';
import { Spinner } from '@/components/ui/spinner';
import { FieldGroup, Field, FieldLabel, FieldError } from '@/components/ui/field';
import { Select, SelectTrigger, SelectValue, SelectContent, SelectGroup, SelectLabel, SelectItem } from '@/components/ui/select';

import { updateUserSchema, createUserSchema } from '@/features/settings/schemas/userSchema';

import { useTeams } from '@/features/settings/hooks/useTeams';
import { TeamSelection } from '@/features/settings/components/TeamSelection';

import { mapToOptions } from '@/utils/mapToOptions';
import { normalizeApiError } from '@/utils/apiErrorNormalizer';
import { useFormError } from '@/hooks/useFormError';

import type { CreateUser } from '@/types/user';
import { useUserStore } from '@/stores/useUserStore';
import { cn } from '@/lib/utils';

type UserFormBaseProps = {
    onCancel?: () => void;
};

type UserFormProps = UserFormBaseProps & {
    mode: 'create' | 'update';
    initialValues?: Partial<CreateUser>;
    onSubmit: (data: CreateUser) => void | Promise<void>;
    submitLabel?: string;
    submittingLabel?: string;
};

const defaultValues: Partial<CreateUser> = {
    firstName: '',
    lastName: '',
    email: '',
    teamIds: [],
    role: 'USER',
};

export function UserForm(props: UserFormProps) {
    const { initialValues, onCancel, submitLabel, submittingLabel, mode } = props;
    const isUpdateMode = mode === 'update';
    const currentUserEmail = useUserStore((state) => state.user?.email);
    const isCurrentUser = initialValues?.email === currentUserEmail;

    const formId = useId();
    const resolvedSchema = isUpdateMode ? updateUserSchema : createUserSchema;

    const form = useForm<CreateUser>({
        resolver: zodResolver(resolvedSchema),
        defaultValues: { ...defaultValues, ...initialValues },
        mode: 'onSubmit',
    });

    const { trigger } = form;

    const { data: teamResponse, isPending: isTeamsLoading, isError: isTeamsError } = useTeams();

    const isSubmitting = form.formState.isSubmitting;

    const handleFormError = useFormError(form);

    const handleSubmit: SubmitHandler<CreateUser> = async (data) => {
        try {
            await props.onSubmit(data);
        } catch (error) {
            const normalized = normalizeApiError(error);
            const isHandledByForm = handleFormError(normalized);
            if (!isHandledByForm) {
                toast.error(normalized.message);
            }
        }
    };

    const submitText = submitLabel ?? (isUpdateMode ? 'Save Changes' : 'Add User');
    const submittingText = submittingLabel ?? (isUpdateMode ? 'Saving...' : 'Adding...');

    const teamOptions = mapToOptions(teamResponse?.data || [], 'name', 'id');
    return (
        <>
            <form
                id={formId}
                onSubmit={form.handleSubmit(handleSubmit)}
                className="space-y-6"
                aria-busy={isSubmitting}
                aria-label={isUpdateMode ? 'Edit user information form' : 'Create new user form'}
            >
                <FieldGroup className="gap-5">
                    <div className="grid gap-3 sm:grid-cols-2">
                        <Controller
                            name="firstName"
                            control={form.control}
                            render={({ field, fieldState }) => (
                                <Field data-invalid={fieldState.invalid}>
                                    <FieldLabel htmlFor={`${formId}-first-name`}>
                                        <span>
                                            First Name <span className="text-destructive">*</span>
                                        </span>
                                    </FieldLabel>
                                    <Input
                                        {...field}
                                        id={`${formId}-first-name`}
                                        aria-invalid={fieldState.invalid}
                                        placeholder="First name"
                                        autoComplete="off"
                                        disabled={isSubmitting}
                                        onChange={(e) => {
                                            field.onChange(e);
                                            trigger('firstName');
                                        }}
                                    />
                                    {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                                </Field>
                            )}
                        />

                        <Controller
                            name="lastName"
                            control={form.control}
                            render={({ field, fieldState }) => (
                                <Field data-invalid={fieldState.invalid}>
                                    <FieldLabel htmlFor={`${formId}-last-name`}>
                                        <span>
                                            Last Name <span className="text-destructive">*</span>
                                        </span>
                                    </FieldLabel>
                                    <Input
                                        {...field}
                                        id={`${formId}-last-name`}
                                        aria-invalid={fieldState.invalid}
                                        placeholder="Last name"
                                        autoComplete="off"
                                        disabled={isSubmitting}
                                        onChange={(e) => {
                                            field.onChange(e);
                                            trigger('lastName');
                                        }}
                                    />
                                    {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                                </Field>
                            )}
                        />
                    </div>

                    <Controller
                        name="email"
                        control={form.control}
                        render={({ field, fieldState }) => (
                            <Field data-invalid={fieldState.invalid}>
                                <FieldLabel htmlFor={`${formId}-email`}>
                                    <span>
                                        Email <span className="text-destructive">*</span>
                                    </span>
                                </FieldLabel>
                                <Input
                                    {...field}
                                    id={`${formId}-email`}
                                    disabled={isUpdateMode || isSubmitting}
                                    aria-invalid={fieldState.invalid}
                                    autoComplete="off"
                                    placeholder="example@domain.com"
                                    className={cn((isUpdateMode || isSubmitting) && 'bg-gray-100 cursor-not-allowed')}
                                    onBlur={() => {
                                        field.onBlur();
                                        trigger('email');
                                    }}
                                />
                                {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                            </Field>
                        )}
                    />

                    <Controller
                        name="role"
                        control={form.control}
                        render={({ field, fieldState }) => (
                            <Field data-invalid={fieldState.invalid}>
                                <FieldLabel htmlFor={`${formId}-role`}>
                                    <span>
                                        Role <span className="text-destructive">*</span>
                                    </span>
                                </FieldLabel>
                                <Select value={field.value ?? ''} onValueChange={field.onChange} disabled={isCurrentUser || isSubmitting}>
                                    <SelectTrigger
                                        id={`${formId}-role`}
                                        className={cn(
                                            'h-9 w-full rounded-md border border-input bg-transparent px-3 text-sm shadow-xs transition-all outline-none focus-visible:border-ring focus-visible:ring-[3px] focus-visible:ring-ring/50',
                                            isCurrentUser && 'bg-gray-100 disabled:pointer-events-none',
                                        )}
                                    >
                                        <SelectValue placeholder="Select a role" />
                                    </SelectTrigger>
                                    <SelectContent position="popper">
                                        <SelectGroup>
                                            <SelectLabel>Roles</SelectLabel>
                                            <SelectItem value="USER">User</SelectItem>
                                            <SelectItem value="ADMIN">Admin</SelectItem>
                                        </SelectGroup>
                                    </SelectContent>
                                </Select>
                                {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                            </Field>
                        )}
                    />

                    <Controller
                        name="teamIds"
                        control={form.control}
                        render={({ field, fieldState }) => (
                            <Field data-invalid={fieldState.invalid}>
                                <FieldLabel htmlFor={`${formId}-teams`}>
                                    <span>
                                        Team <em>(Optional)</em>
                                    </span>
                                </FieldLabel>

                                <TeamSelection
                                    field={field}
                                    fieldState={fieldState}
                                    teamOptions={teamOptions}
                                    isTeamsLoading={isTeamsLoading}
                                    isTeamsError={isTeamsError}
                                    isSubmitting={isSubmitting}
                                    htmlForId={`${formId}-teams`}
                                />
                            </Field>
                        )}
                    />
                </FieldGroup>
            </form>
            <DialogFooter className="flex justify-end gap-3 pt-4">
                <Button type="button" variant="outline" onClick={onCancel} disabled={isSubmitting}>
                    Cancel
                </Button>
                <Button type="submit" form={formId} disabled={isSubmitting}>
                    {isSubmitting ? (
                        <>
                            <Spinner /> <span>{submittingText}</span>
                        </>
                    ) : (
                        submitText
                    )}
                </Button>
            </DialogFooter>
        </>
    );
}
