import { PlusCircle } from 'lucide-react';
import type { ControllerFieldState, ControllerRenderProps } from 'react-hook-form';
import { MultiSelect } from '@/components/ui/multi-select';
import { Spinner } from '@/components/ui/spinner';
import { FieldError } from '@/components/ui/field';
import type { CreateUser, UpdateUser } from '@/types/user';
import { cn } from '@/lib/utils';

type TeamSelectionProps = {
    field: ControllerRenderProps<CreateUser | UpdateUser, 'teamIds'>;
    fieldState: ControllerFieldState;
    teamOptions: Array<{ label: string; value: string }>;
    isTeamsLoading: boolean;
    isTeamsError: boolean;
    isSubmitting: boolean;
    htmlForId: string;
};

export function TeamSelection({ field, fieldState, teamOptions, isTeamsLoading, isTeamsError, isSubmitting, htmlForId }: TeamSelectionProps) {
    const id = htmlForId;
    if (isTeamsLoading) {
        return (
            <div className="flex items-center" role="status" aria-label="Loading teams list">
                <Spinner className="mr-2 h-4 w-4" aria-hidden="true" /> Loading teams...
            </div>
        );
    }

    if (isTeamsError) {
        return (
            <div className="text-destructive text-sm" role="alert" aria-label="Error loading teams">
                Failed to load teams. Please refresh the page.
            </div>
        );
    }

    if (teamOptions.length === 0) {
        return (
            <div className="flex items-center gap-2 text-sm text-muted-foreground italic" aria-label="No teams available">
                <PlusCircle className="h-3 w-3" aria-hidden="true" /> No teams found.
            </div>
        );
    }

    return (
        <>
            <MultiSelect
                id={id}
                options={teamOptions}
                defaultValue={field.value ?? []}
                onValueChange={field.onChange}
                placeholder="Choose teams"
                disabled={isSubmitting}
                aria-label="Select teams to assign to user"
                modalPopover={true}
                variant="secondary"
                className={cn(
                    'font-normal',

                    '[&_div[role=button]]:flex',
                    '[&_div[role=button]]:items-center',
                    '[&_div[role=button]]:justify-between',

                    '[&_span.inline-flex>span]:max-w-[38px]',
                    '[&_span.inline-flex>span]:truncate',
                )}
                popoverClassName={cn(
                    // 1. Ensure the popover width matches the trigger width for better UX
                    'w-[var(--radix-popover-trigger-width)] [&_.bg-primary_svg]:!text-white',

                    // 2. Make the FIRST group ("Select All") sticky at the top
                    '[&_[cmdk-group]:first-of-type]:sticky [&_[cmdk-group]:first-of-type]:top-0',
                    '[&_[cmdk-group]:first-of-type]:z-10 [&_[cmdk-group]:first-of-type]:bg-popover',
                    '[&_[cmdk-group]:first-of-type]:border-b',

                    // 3. Make the LAST group ("Clear" / "Close") sticky at the bottom
                    '[&_[cmdk-group]:last-of-type]:sticky [&_[cmdk-group]:last-of-type]:bottom-0',
                    '[&_[cmdk-group]:last-of-type]:z-10 [&_[cmdk-group]:last-of-type]:bg-popover',
                    '[&_[cmdk-group]:last-of-type]:border-t',
                )}
            />
            {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
        </>
    );
}
