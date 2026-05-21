import { create } from 'zustand';
import { persist } from 'zustand/middleware';

import type { CurrentUser } from '@/types/user';

export interface UserStore {
    user: CurrentUser | null;
    setUser: (user: CurrentUser) => void;
    clearUser: () => void;
}

export const useUserStore = create<UserStore>()(
    persist(
        (set) => ({
            user: null,
            setUser: (user) => set({ user }),
            clearUser: () => set({ user: null }),
        }),
        {
            name: 'user-storage',
        },
    ),
);
