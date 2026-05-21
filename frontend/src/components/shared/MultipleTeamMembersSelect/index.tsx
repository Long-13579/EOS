import { User } from 'lucide-react';
import { MultiSelect } from '@/components/ui/multi-select';
import { useTeamMembers } from '@/features/settings/hooks/useTeamMembers';
import { useActiveTeamId } from '@/hooks/useActiveTeamId';
import { cn } from '@/lib/utils';

interface MultipleTeamMembersSelectProps {
    value?: string[];
    onChange: (value: string[]) => void;
    disabled?: boolean;
    teamId?: string;
}

export function MultipleTeamMembersSelect({ value, onChange, disabled, teamId }: MultipleTeamMembersSelectProps) {
    const activeTeamId = useActiveTeamId();
    const effectiveTeamId = teamId ?? activeTeamId;
    const { data: members = [], isLoading } = useTeamMembers(effectiveTeamId);
    const options = members.map((member) => ({
        value: member.id,
        label: `${member.firstName} ${member.lastName}`,
    }));

    return (
        <div className="relative flex flex-col" onWheel={(e) => e.stopPropagation()}>
            <User className="absolute left-3 top-1/2 z-10 h-4 w-4 -translate-y-1/2 text-muted-foreground pointer-events-none" />{' '}
            <MultiSelect
                options={options}
                defaultValue={value ?? []}
                onValueChange={onChange}
                placeholder="Unassigned"
                disabled={disabled || isLoading}
                className={cn(
                    'font-normal pl-8',

                    '[&_div[role=button]]:flex',
                    '[&_div[role=button]]:items-center',
                    '[&_div[role=button]]:justify-between',

                    '[&_span.inline-flex>span]:max-w-[140px]',
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
        </div>
    );
}
