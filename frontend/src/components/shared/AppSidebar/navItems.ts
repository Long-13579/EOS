import { LayoutDashboard, Calendar, ChartNoAxesCombined, ClipboardList, Newspaper, SquareCheckBig, BadgeAlert } from 'lucide-react';

export const navItems = [
    { title: 'Dashboard', to: '/', icon: LayoutDashboard },
    { title: 'L10 Meetings', to: '/l10-meetings', icon: Calendar },
    { title: 'Scorecards', to: '/scorecards', icon: ChartNoAxesCombined },
    { title: 'Rocks', to: '/rocks', icon: ClipboardList },
    { title: 'Headlines', to: '/headlines', icon: Newspaper },
    { title: 'To-dos', to: '/todos', icon: SquareCheckBig },
    { title: 'Issues', to: '/issues', icon: BadgeAlert },
];
