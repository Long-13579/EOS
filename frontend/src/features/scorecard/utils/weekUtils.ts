export const checkCurrentWeek = (week: { startDate: string; endDate: string }) => {
    const now = new Date();
    const endOfWeek = new Date(week.endDate);
    endOfWeek.setHours(23, 59, 59, 999);
    return now >= new Date(week.startDate) && now <= endOfWeek;
};
