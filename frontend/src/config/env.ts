interface EnvironmentConfig {
    GOOGLE_CLIENT_ID: string;
    API_BASE_URL: string;
    GOOGLE_REDIRECT_URI: string;
}

export const env: EnvironmentConfig = {
    GOOGLE_CLIENT_ID: import.meta.env.VITE_GOOGLE_CLIENT_ID as string,
    API_BASE_URL: import.meta.env.VITE_API_BASE_URL as string,
    GOOGLE_REDIRECT_URI: window.location.origin,
};

if (!env.GOOGLE_CLIENT_ID) {
    throw new Error('Missing VITE_GOOGLE_CLIENT_ID environment variable.');
}

if (!env.API_BASE_URL) {
    throw new Error('Missing VITE_API_BASE_URL environment variable.');
}
