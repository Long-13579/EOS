import { useState } from 'react';
import { ChevronDown, ChevronUp, Pencil, CheckSquare, Calendar } from 'lucide-react';
import { formatDate } from '@/utils/date';
import type { Todo } from '../types/todo';
import { cn } from '@/lib/utils';
import { StatusBadge } from './StatusBadge';
import { TableActions } from '@/components/shared/Table';

interface TodoListItemProps {
    todo: Todo;
    onUpdate?: (todo: Todo) => void;
}

const getTodoActions = (todo: Todo, onUpdate?: (todo: Todo) => void) => {
    const actions = [];

    if (!todo.isArchived) {
        actions.push({
            label: 'Edit Todo',
            icon: Pencil,
            onClick: () => onUpdate?.(todo),
        });
    }

    return actions;
};

export function TodoListItem({ todo, onUpdate }: TodoListItemProps) {
    const [isExpanded, setIsExpanded] = useState(false);

    return (
        <div className="border border-border rounded-xl bg-card text-card-foreground overflow-hidden shadow-sm transition-all">
            <div
                className="flex items-center justify-between p-4 cursor-pointer hover:bg-accent hover:text-accent-foreground transition-colors"
                onClick={() => setIsExpanded(!isExpanded)}
            >
                <div className="flex items-center gap-3">
                    <CheckSquare className="w-5 h-5 text-primary" />
                    <div className="flex flex-col gap-1">
                        <span className="font-semibold leading-none">{todo.title}</span>
                        <div className="flex items-center gap-1.5 text-xs text-muted-foreground mt-1">
                            <Calendar className="w-3.5 h-3.5" />
                            {`Due: ${formatDate(todo.dueDate, { fallback: 'No due date' })}`}
                        </div>
                    </div>
                </div>

                <div className="flex items-center gap-3">
                    {todo.team && <span className="text-xs px-2 py-1 rounded-md bg-muted text-muted-foreground border">{todo.team.name}</span>}
                    <StatusBadge status={todo.status} />
                    {isExpanded ? <ChevronUp className="w-5 h-5 text-muted-foreground" /> : <ChevronDown className="w-5 h-5 text-muted-foreground" />}
                </div>
            </div>

            {isExpanded && (
                <div className="p-5 border-t border-border bg-muted/20">
                    <div className="mb-6">
                        <p className="text-sm text-muted-foreground mb-1">Description</p>
                        <p
                            className={cn('text-sm break-words line-clamp-3', !todo.description && 'text-muted-foreground italic')}
                            title={todo.description}
                        >
                            {todo.description || 'No description provided.'}
                        </p>
                    </div>

                    <div className="flex flex-col sm:flex-row items-center justify-end pt-4 border-t border-border gap-4">
                        <div className="flex items-center gap-2 w-full sm:w-auto">
                            <TableActions actions={getTodoActions(todo, onUpdate)} />
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}
