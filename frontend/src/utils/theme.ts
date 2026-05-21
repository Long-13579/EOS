export const initTheme = () => {
    const savedTheme = localStorage.getItem('theme') || 'light';

    if (savedTheme === 'dark') {
        document.documentElement.classList.add('dark');
    } else {
        document.documentElement.classList.remove('dark');
    }
};

export const toggleTheme = () => {
    const isDark = document.documentElement.classList.toggle('dark');

    localStorage.setItem('theme', isDark ? 'dark' : 'light');

    return isDark ? 'dark' : 'light';
};

export const getTheme = () => {
    return localStorage.getItem('theme') || 'light';
};
