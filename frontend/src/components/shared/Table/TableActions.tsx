import { Button } from '@/components/ui/button';
import type { LucideIcon } from 'lucide-react';
import { cn } from '@/lib/utils';
import { Tooltip, TooltipContent, TooltipTrigger } from '@/components/ui/tooltip';

export interface ActionItem {
    label: string;
    icon: LucideIcon;
    onClick?: () => void;
    variant?: 'default' | 'ghost' | 'destructive';
}

interface TableActionsProps {
    actions: ActionItem[];
}

export function TableActions({ actions }: Readonly<TableActionsProps>) {
    return (
        <div className="flex items-center justify-end gap-2">
            {actions.map((action, index) => {
                const Icon = action.icon;
                const isDestructive = action.variant === 'destructive';

                return (
                    <Tooltip key={index}>
                        <TooltipTrigger asChild>
                            <Button
                                size="icon-sm"
                                variant="ghost"
                                onClick={action.onClick}
                                aria-label={action.label}
                                className={cn(
                                    'transition-colors',
                                    isDestructive && 'text-destructive hover:bg-destructive/10 hover:text-destructive',
                                )}
                            >
                                <Icon className="h-4 w-4" />
                            </Button>
                        </TooltipTrigger>
                        <TooltipContent className="bg-popover text-popover-foreground border-popover shadow-md">
                            <p>{action.label}</p>
                        </TooltipContent>
                    </Tooltip>
                );
            })}
        </div>
    );
}
