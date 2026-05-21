export function getUserFullName(user: { firstName: string; lastName: string }): string {
    return `${user.firstName} ${user.lastName}`.trim();
}

export function getUserInitials(user: { firstName: string; lastName: string }): string {
    const firstInitial = user.firstName ? user.firstName.charAt(0).toUpperCase() : '';
    const lastInitial = user.lastName ? user.lastName.charAt(0).toUpperCase() : '';
    return `${firstInitial}${lastInitial}`;
}
