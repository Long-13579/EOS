export const weekKeys = {
    all: ['weeks'] as const,
    list: () => [...weekKeys.all, 'list'] as const,
};
