import { User } from 'lucide-react';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { useTeamMembers } from '@/features/settings/hooks/useTeamMembers';
import { useActiveTeamId } from '@/hooks/useActiveTeamId';

interface TeamMemberSelectProps {
    value?: string;
    onChange: (value: string) => void;
    disabled?: boolean;
    teamId?: string;
}

export function TeamMemberSelect({ value, onChange, disabled, teamId }: TeamMemberSelectProps) {
    const activeTeamId = useActiveTeamId();

    const effectiveTeamId = teamId ?? activeTeamId;
    const { data: members = [], isLoading } = useTeamMembers(effectiveTeamId);

    return (
        <Select
            value={value ?? 'unassigned'}
            onValueChange={(val) => onChange(val === 'unassigned' ? '' : val)}
            disabled={disabled || isLoading || !effectiveTeamId}
        >
            <SelectTrigger className="relative h-9 w-full pl-9 ">
                <User className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
                <SelectValue placeholder="Unassigned" />
            </SelectTrigger>
            <SelectContent position="popper">
                <SelectItem value="unassigned">Unassigned</SelectItem>

                {members.map((member) => (
                    <SelectItem key={member.id} value={member.id}>
                        <p className="max-w-[400px] truncate">
                            {member.firstName} {member.lastName}
                        </p>
                    </SelectItem>
                ))}
            </SelectContent>
        </Select>
    );
}
