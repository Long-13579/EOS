import { isRedirect, redirect } from '@tanstack/react-router';

import { useUserStore } from '@/stores/useUserStore';
import type { UserRole } from '@/types/user';
import { getCurrentUserInfo } from '../services/userService';

export const UserAuthActions = {
    getCurrentUser: () => useUserStore.getState().user,

    requireAuth: (location: string) => {
        // Redirect to login immediately if no user info in local store
        const user = UserAuthActions.getCurrentUser();
        if (!user) {
            throw redirect({
                to: '/login',
                search: { redirect: location },
            });
        }
        return user;
    },

    requireAuthWithoutRedirect: () => {
        const user = UserAuthActions.getCurrentUser();
        if (!user) {
            throw redirect({
                to: '/login',
            });
        }
        return user;
    },

    requireRoles: async (allowedRoles: UserRole[], location: string) => {
        // Short-circuit if no user in local store
        UserAuthActions.requireAuth(location);

        try {
            const response = await getCurrentUserInfo();
            const verifiedUser = response;

            useUserStore.getState().setUser(verifiedUser);

            if (!allowedRoles.includes(verifiedUser.role)) {
                throw redirect({
                    to: '/',
                    search: { error: 'forbidden' },
                });
            }
            return verifiedUser;
        } catch (error) {
            if (isRedirect(error)) {
                throw error;
            }

            useUserStore.getState().clearUser();
            throw redirect({
                to: '/login',
                search: { redirect: location, error: 'session_expired' },
            });
        }
    },

    requireAdmin: (location: string) => UserAuthActions.requireRoles(['ADMIN'], location),

    redirectIfAuthenticated: async () => {
        const user = UserAuthActions.getCurrentUser();
        if (user) {
            throw redirect({ to: '/', search: { error: 'already_authenticated' } });
        }
    },
};
