import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import '@/index.css';
import { initTheme } from '@/utils/theme.ts';

import { RouterProvider, createRouter } from '@tanstack/react-router';
import { routeTree } from '@/routeTree.gen';
import { GoogleOAuthProvider } from '@react-oauth/google';
import { env } from '@/config';
import { QueryClientProvider } from '@tanstack/react-query';
import { queryClient } from '@/lib/reactQuery';
import { Toaster } from 'sonner';
import { registerAuthHandlers } from '@/config/axios';
import { clearSessionAndRedirect, refreshToken } from '@/features/auth';

initTheme();

const router = createRouter({
    routeTree,
    context: {
        queryClient,
    },
});

registerAuthHandlers({
    refresh: refreshToken,
    onAuthFailure: async () => await clearSessionAndRedirect(router.navigate),
});

createRoot(document.getElementById('root')!).render(
    <StrictMode>
        <QueryClientProvider client={queryClient}>
            <GoogleOAuthProvider clientId={env.GOOGLE_CLIENT_ID}>
                <RouterProvider router={router} />
            </GoogleOAuthProvider>
        </QueryClientProvider>
        <Toaster
            closeButton
            richColors
            toastOptions={{
                classNames: {
                    toast: 'relative',
                    closeButton: '!left-auto !right-3 !top-1/2 !border-none hover:!bg-gray-200 !scale-125',
                },
            }}
        />
    </StrictMode>,
);
