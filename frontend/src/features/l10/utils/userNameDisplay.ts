const DEFAULT_MAX_LENGTH = 26;

export function formatUserName(firstName: string, lastName: string, maxLength = DEFAULT_MAX_LENGTH): string {
    const full = `${firstName} ${lastName}`;
    if (full.length <= maxLength) {
        return full;
    }
    return full.substring(0, maxLength).trimEnd() + '…';
}

export function isUserNameTruncated(firstName: string, lastName: string, maxLength = DEFAULT_MAX_LENGTH): boolean {
    return `${firstName} ${lastName}`.length > maxLength;
}
