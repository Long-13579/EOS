import type { ControllerRenderProps, FieldError } from 'react-hook-form';
import { LayoutList } from 'lucide-react';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Field, FieldLabel, FieldError as FormFieldError } from '@/components/ui/field';
import { Spinner } from '@/components/ui/spinner';
import { useIssueTypes } from '../hooks/useIssueTypes';
import type { IssueFormValues } from '../types/issue';

interface IssueTypeSelectProps {
    field: ControllerRenderProps<IssueFormValues, 'issueTypeId'>;
    error?: FieldError;
    disabled?: boolean;
    inputId: string;
}

export function IssueTypeSelect({ field, error, disabled, inputId }: Readonly<IssueTypeSelectProps>) {
    const { data, isPending, isError } = useIssueTypes();
    const issueTypes = data ?? [];

    const isDisabled = disabled || isPending || isError;

    return (
        <Field data-invalid={!!error || isError}>
            <FieldLabel htmlFor={inputId}>Issue Type</FieldLabel>

            <Select value={field.value ?? 'none'} onValueChange={(val) => field.onChange(val === 'none' ? null : val)} disabled={isDisabled}>
                <SelectTrigger id={inputId} className="relative h-9 w-full pl-9" aria-label="Select issue type">
                    <LayoutList
                        aria-hidden="true"
                        className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground"
                    />

                    {isPending ? (
                        <div className="flex items-center gap-2 text-muted-foreground">
                            <Spinner className="size-3" />
                            <span>Loading issue types...</span>
                        </div>
                    ) : (
                        <SelectValue placeholder="Select issue type" />
                    )}
                </SelectTrigger>

                <SelectContent position="popper">
                    {isPending && <div className="p-4 text-sm text-muted-foreground text-center">Loading issue types...</div>}

                    {!isPending && !isError && issueTypes.length > 0 && (
                        <>
                            <SelectItem value="none">-</SelectItem>
                            {issueTypes.map((type) => (
                                <SelectItem key={type.id} value={type.id}>
                                    {type.name}
                                </SelectItem>
                            ))}
                        </>
                    )}

                    {!isPending && !isError && issueTypes.length === 0 && (
                        <div className="p-4 text-sm text-muted-foreground text-center">No issue types available</div>
                    )}
                </SelectContent>
            </Select>

            {error && <FormFieldError errors={[error]} />}

            {!error && isError && <FormFieldError errors={[{ message: 'Failed to load issue types' } as FieldError]} />}
        </Field>
    );
}
