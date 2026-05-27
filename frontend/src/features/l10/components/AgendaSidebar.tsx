import { Smile, BarChart3, Mountain, Newspaper, ListChecks, AlertTriangle, Flag } from 'lucide-react';
import { cn } from '@/lib/utils';
import type { AgendaSession } from '../types/l10Meeting';
import { AGENDA_SESSIONS } from '../types/l10Meeting';

const SESSION_ICONS: Record<AgendaSession, React.ComponentType<{ className?: string }>> = {
    segue: Smile,
    scorecard: BarChart3,
    rocks: Mountain,
    headlines: Newspaper,
    todos: ListChecks,
    issues: AlertTriangle,
    conclude: Flag,
};

const SESSION_LABELS: Record<AgendaSession, string> = {
    segue: 'Segue',
    scorecard: 'Scorecard',
    rocks: 'Rocks',
    headlines: 'Headlines',
    todos: 'To-Do',
    issues: 'Issues',
    conclude: 'Conclude',
};

interface AgendaSidebarProps {
    activeSession: AgendaSession;
    onSessionChange: (session: AgendaSession) => void;
}

export function AgendaSidebar({ activeSession, onSessionChange }: AgendaSidebarProps) {
    return (
        <nav className="flex w-56 flex-col gap-1 border-r p-4">
            {AGENDA_SESSIONS.map((session) => {
                const Icon = SESSION_ICONS[session];
                const isActive = session === activeSession;
                return (
                    <button
                        key={session}
                        type="button"
                        onClick={() => onSessionChange(session)}
                        className={cn(
                            'flex items-center gap-3 rounded-md px-3 py-2.5 text-sm font-medium transition-colors',
                            isActive ? 'bg-primary text-primary-foreground' : 'text-muted-foreground hover:bg-accent hover:text-accent-foreground',
                        )}
                    >
                        <Icon className="h-4 w-4" />
                        {SESSION_LABELS[session]}
                    </button>
                );
            })}
        </nav>
    );
}
