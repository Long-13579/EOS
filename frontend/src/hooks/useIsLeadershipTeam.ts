import { useTeamStore } from '@/stores/useTeamStore';

export function useIsLeadershipTeam() {
    return useTeamStore((state) => state.activeTeam?.isLeadership);
}
