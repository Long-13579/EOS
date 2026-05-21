import { useTeamStore } from '@/stores/useTeamStore';

export function useActiveTeamId() {
    return useTeamStore((state) => state.activeTeam?.id);
}
