import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import type { Team } from '@/types/team';

interface TeamStore {
    activeTeam: Team | null;
    setActiveTeam: (team: Team | null) => void;
    resetActiveTeam: () => void;
}

export const useTeamStore = create<TeamStore>()(
    persist(
        (set) => ({
            activeTeam: null,

            setActiveTeam: (team) =>
                set(() => ({
                    activeTeam: team,
                })),

            resetActiveTeam: () => {
                set(() => ({ activeTeam: null }));
                useTeamStore.persist.clearStorage();
            },
        }),
        {
            name: 'active-team-selection',
        },
    ),
);
