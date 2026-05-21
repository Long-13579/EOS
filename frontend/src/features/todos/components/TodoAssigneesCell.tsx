import { Badge } from '@/components/ui/badge';
import { Tooltip, TooltipContent, TooltipTrigger } from '@/components/ui/tooltip';
import { Avatar, AvatarFallback } from '@/components/ui/avatar';
import type { TeamMember } from '@/types/user';
import { getUserFullName, getUserInitials } from '@/utils/user';

interface TodoAssigneesCellProps {
    assignees?: TeamMember[];
}

export function TodoAssigneesCell({ assignees }: TodoAssigneesCellProps) {
    if (!assignees?.length) {
        return <span className="text-muted-foreground text-sm italic">No assignees</span>;
    }

    if (assignees.length <= 2) {
        return (
            <div className="flex flex-wrap gap-1">
                {assignees.map((assignee) => (
                    <Tooltip key={assignee.id}>
                        <TooltipTrigger asChild>
                            <Badge variant="secondary" className="font-normal max-w-25 truncate block">
                                {getUserFullName(assignee)}
                            </Badge>
                        </TooltipTrigger>
                        <TooltipContent>
                            <p className="max-w-62.5 wrap-break-word">{getUserFullName(assignee)}</p>
                        </TooltipContent>
                    </Tooltip>
                ))}
            </div>
        );
    }

    return (
        <div className="flex items-center gap-1">
            {assignees.slice(0, 3).map((assignee) => (
                <Tooltip key={assignee.id}>
                    <TooltipTrigger>
                        <Avatar className="h-7 w-7 text-xs">
                            <AvatarFallback>{getUserInitials(assignee)}</AvatarFallback>
                        </Avatar>
                    </TooltipTrigger>

                    <TooltipContent>
                        <p>{getUserFullName(assignee)}</p>
                    </TooltipContent>
                </Tooltip>
            ))}

            {assignees.length > 3 && (
                <Tooltip>
                    <TooltipTrigger>
                        <div className="flex h-7 w-7 items-center justify-center rounded-full bg-muted text-xs">+{assignees.length - 3}</div>
                    </TooltipTrigger>

                    <TooltipContent>
                        <div className="flex flex-col gap-1">
                            {assignees.slice(3).map((assignee) => (
                                <span key={assignee.id}>{getUserFullName(assignee)}</span>
                            ))}
                        </div>
                    </TooltipContent>
                </Tooltip>
            )}
        </div>
    );
}
