import { Badge } from '@/components/ui/badge';
import { Tooltip, TooltipContent, TooltipTrigger } from '@/components/ui/tooltip';
import { Avatar, AvatarFallback } from '@/components/ui/avatar';
import type { User } from '@/types/user';

interface UserTeamsCellProps {
    teams: User['teams'];
}

export function UserTeamsCell({ teams }: UserTeamsCellProps) {
    if (!teams?.length) {
        return <span className="text-muted-foreground text-sm italic">No teams</span>;
    }

    if (teams.length <= 2) {
        return (
            <div className="flex flex-wrap gap-1">
                {teams.map((team) => (
                    <Tooltip key={team.id}>
                        <TooltipTrigger asChild>
                            <Badge variant="secondary" className="font-normal max-w-[100px] truncate block">
                                {team.name}
                            </Badge>
                        </TooltipTrigger>
                        <TooltipContent>
                            <p className="max-w-[250px] break-words">{team.name}</p>
                        </TooltipContent>
                    </Tooltip>
                ))}
            </div>
        );
    }

    return (
        <div className="flex items-center gap-1">
            {teams.slice(0, 3).map((team) => (
                <Tooltip key={team.id}>
                    <TooltipTrigger>
                        <Avatar className="h-7 w-7 text-xs">
                            <AvatarFallback>{team.name.slice(0, 2).toUpperCase()}</AvatarFallback>
                        </Avatar>
                    </TooltipTrigger>

                    <TooltipContent>
                        <p>{team.name}</p>
                    </TooltipContent>
                </Tooltip>
            ))}

            {teams.length > 3 && (
                <Tooltip>
                    <TooltipTrigger>
                        <div className="h-7 w-7 rounded-full bg-muted flex items-center justify-center text-xs">+{teams.length - 3}</div>
                    </TooltipTrigger>

                    <TooltipContent>
                        <div className="flex flex-col gap-1">
                            {teams.slice(3).map((team) => (
                                <span key={team.id}>{team.name}</span>
                            ))}
                        </div>
                    </TooltipContent>
                </Tooltip>
            )}
        </div>
    );
}
