import { useState } from 'react';
import { logout } from '../services/authService';
import { useNavigate } from '@tanstack/react-router';
import { clearSessionAndRedirect } from '../utils/clearSessionAndRedirect';
import { normalizeApiError } from '@/utils/apiErrorNormalizer';
import { toast } from 'sonner';

export function useLogout() {
    const [isLoggingOut, setIsLoggingOut] = useState(false);
    const navigate = useNavigate();

    const handleLogout = async () => {
        if (isLoggingOut) {
            return;
        }

        setIsLoggingOut(true);

        try {
            await logout();
            clearSessionAndRedirect(navigate);
        } catch (error) {
            setIsLoggingOut(false);
            const normalized = normalizeApiError(error);
            toast.error(normalized.message || 'An error occurred while logging out. Please try again.');
        }
    };

    return { logout: handleLogout, isLoggingOut };
}
