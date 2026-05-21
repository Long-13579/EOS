import { useEffect } from 'react';
import { useMyTeams } from '@/hooks/useMyTeams';
import { useTeamStore } from '@/stores/useTeamStore';
import type { Team } from '@/types/team';

const EMPTY_TEAMS: Team[] = [];

export function useGlobalTeamSelector() {
    const { activeTeam, setActiveTeam, resetActiveTeam } = useTeamStore();
    const { data: teamsData, isLoading, isError, isSuccess } = useMyTeams();

    const teams = teamsData ?? EMPTY_TEAMS;
    const activeTeamStillExists = activeTeam && teams.some((team) => team.id === activeTeam.id);

    useEffect(() => {
        if (teams.length > 0 && !activeTeamStillExists) {
            setActiveTeam(teams[0]);
        }
    }, [teams, setActiveTeam, activeTeamStillExists]);

    useEffect(() => {
        if (isSuccess && teams.length === 0) {
            resetActiveTeam();
        }
    }, [isSuccess, teams.length, resetActiveTeam]);

    const handleTeamChange = (teamId: string) => {
        const selectedTeam = teams.find((team) => team.id === teamId);
        if (selectedTeam) {
            setActiveTeam(selectedTeam);
        }
    };

    return { teams, activeTeam, isLoading, isError, handleTeamChange };
}
