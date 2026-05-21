interface FormatDateOptions {
    fallback?: string;
    locale?: string;
}

export function formatDate(date?: string, options: FormatDateOptions = {}) {
    const { fallback = '', locale = 'en-GB' } = options;

    if (!date) {
        return fallback;
    }

    return new Date(date).toLocaleDateString(locale);
}

export function formatShortDate(date?: string, locale = 'en-US') {
    if (!date) {
        return '';
    }

    return new Date(date).toLocaleDateString(locale, {
        month: 'short',
        day: 'numeric',
    });
}

export function toUtcStartOfDayISOString(date: Date): string {
    return new Date(Date.UTC(date.getFullYear(), date.getMonth(), date.getDate(), 0, 0, 0, 0)).toISOString();
}
