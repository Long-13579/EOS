import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { IssueForm } from './IssueForm';
import type { IssueFormValues, Issue } from '../types/issue';

interface IssueDialogProps {
    isOpen: boolean;
    onOpenChange: (open: boolean) => void;
    onSubmit: (data: IssueFormValues) => void | Promise<void>;
    issue: Issue | null;
}

export function IssueDialog({ isOpen, onOpenChange, onSubmit, issue }: Readonly<IssueDialogProps>) {
    const mode = issue ? 'edit' : 'create';

    const initialData = issue && {
        title: issue.title,
        description: issue.description,
        issueTypeId: issue.issueType?.id ?? null,
    };

    return (
        <Dialog open={isOpen} onOpenChange={onOpenChange}>
            <DialogContent>
                <DialogHeader>
                    <DialogTitle>{mode === 'edit' ? 'Edit Issue' : 'Create Issue'}</DialogTitle>
                </DialogHeader>

                <IssueForm key={mode} initialData={initialData} mode={mode} onSubmit={onSubmit} onCancel={() => onOpenChange(false)} />
            </DialogContent>
        </Dialog>
    );
}
