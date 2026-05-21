import { useGoogleLogin } from '@react-oauth/google';
import { useNavigate, useSearch } from '@tanstack/react-router';
import { useMutation } from '@tanstack/react-query';
import type { AxiosError } from 'axios';
import { loginWithGoogle } from '../services/authService';
import { env } from '@/config';
import type { ApiErrorResponse } from '@/types/api';
import { normalizeApiError } from '@/utils/apiErrorNormalizer';
import { toast } from 'sonner';
import { useUserStore } from '@/stores/useUserStore';
import { getCurrentUserInfo } from '../services/userService';
import { getSafeInternalPath } from '../utils/url';
import { useEffect, useState } from 'react';

export function useGoogleAuth() {
    const navigate = useNavigate();
    const { redirect } = useSearch({ from: '/(auth)/login' });

    const setUser = useUserStore((state) => state.setUser);

    const [isAuthorizing, setIsAuthorizing] = useState(false);

    const { mutate: loginWithGoogleMutation, isPending: isExchangingCode } = useMutation({
        mutationFn: async (credentials: { code: string; redirectUri: string }) => {
            await loginWithGoogle(credentials);
            return await getCurrentUserInfo();
        },
        onSuccess: (userInfo) => {
            setUser(userInfo);
            const safeRedirect = getSafeInternalPath(redirect);
            navigate({ to: safeRedirect });
        },
        onError: (error: AxiosError<ApiErrorResponse<null>>) => {
            const normalized = normalizeApiError(error);
            toast.error(normalized.message);
        },
        onSettled: () => {
            setIsAuthorizing(false);
        },
    });

    const googleLogin = useGoogleLogin({
        flow: 'auth-code',
        onSuccess: (codeResponse) => {
            loginWithGoogleMutation({
                code: codeResponse.code,
                redirectUri: env.GOOGLE_REDIRECT_URI,
            });
        },
        onError: (error) => {
            setIsAuthorizing(false);
            const normalized = normalizeApiError(error);
            toast.error(normalized.message);
        },
    });

    const isAuthenticating = isAuthorizing || isExchangingCode;

    useEffect(() => {
        const handleFocus = () => {
            if (isAuthorizing && !isExchangingCode) {
                setIsAuthorizing(false);
            }
        };

        window.addEventListener('focus', handleFocus);
        return () => window.removeEventListener('focus', handleFocus);
    }, [isAuthorizing, isExchangingCode]);

    const login = () => {
        if (isAuthenticating) {
            return;
        }

        setIsAuthorizing(true);
        googleLogin();
    };

    return { login, isAuthenticating };
}
