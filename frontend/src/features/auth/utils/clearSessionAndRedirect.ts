import { queryClient } from '@/lib/reactQuery';
import { useUserStore } from '@/stores/useUserStore';
import type { NavigateFn } from '@tanstack/react-router';

export async function clearSessionAndRedirect(navigate: NavigateFn) {
    queryClient.clear();
    useUserStore.getState().clearUser();
    await navigate({ to: '/login' });
}
