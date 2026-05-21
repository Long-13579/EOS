import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Spinner } from '@/components/ui/spinner';

import { useGlobalTeamSelector } from './hooks/useGlobalTeamSelector';

const TEAM_LABEL = 'Team:';
const LOADING_MESSAGE = 'Loading teams...';
const ERROR_MESSAGE = 'Error loading teams';
const NO_TEAMS_MESSAGE = 'No teams available';

export function GlobalTeamSelector() {
    const { teams, activeTeam, isError, isLoading, handleTeamChange } = useGlobalTeamSelector();
    if (isError) {
        return (
            <div className="flex items-center gap-2" aria-live="polite" role="alert">
                <span className="text-sm font-medium">{TEAM_LABEL}</span>
                <span className="text-sm text-destructive">{ERROR_MESSAGE}</span>
            </div>
        );
    }

    if (!isLoading && teams.length === 0) {
        return (
            <div className="flex items-center gap-2" aria-live="polite">
                <span className="text-sm font-medium">{TEAM_LABEL}</span>
                <span className="text-sm text-muted-foreground">{NO_TEAMS_MESSAGE}</span>
            </div>
        );
    }

    const isDisabled = isLoading || isError || teams.length === 0;

    return (
        <div className="flex items-center gap-2">
            <label htmlFor="team-selector" className="text-sm font-medium">
                {TEAM_LABEL}
            </label>
            <Select value={activeTeam?.id || ''} onValueChange={handleTeamChange} disabled={isDisabled}>
                <SelectTrigger id="team-selector" className="h-9 w-48 border-muted text-sm" aria-label="Select a team">
                    {isLoading ? (
                        <div className="flex items-center gap-2 text-muted-foreground">
                            <Spinner className="size-3" />
                            <span>{LOADING_MESSAGE}</span>
                        </div>
                    ) : (
                        <SelectValue placeholder="Select a team..." />
                    )}
                </SelectTrigger>
                <SelectContent position="popper">
                    {teams.map((team) => (
                        <SelectItem key={team.id} value={team.id}>
                            <p className="max-w-[120px] truncate" title={team.name}>
                                {team.name}
                            </p>
                        </SelectItem>
                    ))}
                </SelectContent>
            </Select>
        </div>
    );
}
