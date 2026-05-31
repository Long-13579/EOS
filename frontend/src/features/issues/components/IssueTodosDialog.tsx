import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import type { Issue } from '../types/issue';
import { IssueTodosTab } from './IssueTodosTab';

interface IssueTodosDialogProps {
    isOpen: boolean;
    onOpenChange: (open: boolean) => void;
    issue: Issue | null;
}

export function IssueTodosDialog({ isOpen, onOpenChange, issue }: Readonly<IssueTodosDialogProps>) {
    return (
        <Dialog open={isOpen} onOpenChange={onOpenChange}>
            <DialogContent className="sm:max-w-2xl md:max-w-3xl lg:max-w-5xl">
                <DialogHeader>
                    <DialogTitle>Issue To-dos</DialogTitle>
                </DialogHeader>
                <IssueTodosTab issue={issue} />
            </DialogContent>
        </Dialog>
    );
}
