export const getSafeInternalPath = (path: unknown, fallback = '/'): string => {
    if (typeof path !== 'string') {
        return fallback;
    }

    const isSafe = path.startsWith('/') && !path.startsWith('//');

    return isSafe ? path : fallback;
};
