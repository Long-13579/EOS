import { Button } from '@/components/ui/button';
import { Spinner } from '@/components/ui/spinner';
import { useGoogleAuth } from '../hooks/useGoogleAuth';
import GoogleIcon from '@/assets/icons/google.svg?react';

export function GoogleButton() {
    const { login, isAuthenticating } = useGoogleAuth();

    return (
        <Button
            onClick={() => login()}
            variant="outline"
            type="button"
            className="w-full border-[var(--color-primary)] text-[var(--color-primary)] hover:bg-[var(--color-primary)] hover:text-white"
            disabled={isAuthenticating}
        >
            {isAuthenticating ? (
                <>
                    <Spinner aria-hidden="true" />
                    Logging in with Google...
                </>
            ) : (
                <>
                    <GoogleIcon className="w-5 h-5" />
                    Login with Google
                </>
            )}
        </Button>
    );
}
