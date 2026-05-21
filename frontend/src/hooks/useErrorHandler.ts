import { useEffect } from 'react';
import { useNavigate, useSearch } from '@tanstack/react-router';
import { toast } from 'sonner';

interface SearchParams {
    error?: string;
    [key: string]: unknown;
}

export function useErrorHandler(from: string): void {
    const { error } = useSearch({ from: from });
    const navigate = useNavigate();

    useEffect(() => {
        if (!error) {
            return;
        }

        if (error === 'forbidden') {
            toast.error('Access Denied', {
                id: 'auth-error',
                description: 'You do not have permission to access the settings page.',
            });
        }

        if (error === 'unauthenticated') {
            toast.error('Sign-in required', {
                id: 'auth-unauthenticated',
                description: 'Please sign in to continue.',
            });
        }

        if (error === 'session_expired') {
            toast.error('Session Expired', {
                id: 'auth-session-expired',
                description: 'Your session has expired. Please sign in again.',
            });
        }

        if (error === 'already_authenticated') {
            toast.error('Already Signed In', {
                id: 'auth-already-authenticated',
                description: 'You are already signed in.',
            });
        }

        // Clear the error from the URL after showing the toast
        navigate({
            to: '.',
            search: (prev: SearchParams): Omit<SearchParams, 'error'> => {
                // eslint-disable-next-line @typescript-eslint/no-unused-vars
                const { error: _, ...rest } = prev;
                return rest;
            },
            replace: true,
        });
    }, [error, navigate]);
}
