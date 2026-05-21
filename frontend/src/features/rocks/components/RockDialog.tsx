import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { RockForm } from './RockForm';
import type { Rock, RockFormValues } from '../types/rock';

interface RockDialogProps {
    isOpen: boolean;
    onOpenChange: (open: boolean) => void;
    editingRock?: Rock | null;
    onSubmit: (data: RockFormValues) => void | Promise<void>;
}

export function RockDialog({ isOpen, onOpenChange, editingRock, onSubmit }: Readonly<RockDialogProps>) {
    const mode = editingRock ? 'edit' : 'create';

    const initialData: RockFormValues | undefined = editingRock
        ? {
              title: editingRock.title,
              category: editingRock.category,
              ownerId: editingRock.owner.id,
              year: String(editingRock.year.year),
              status: editingRock.status,
              quarterId: editingRock.quarter.id,
              dueDate: new Date(editingRock.dueDate),
              description: editingRock.description,
          }
        : undefined;

    return (
        <Dialog open={isOpen} onOpenChange={onOpenChange}>
            <DialogContent>
                <DialogHeader>
                    <DialogTitle>{mode === 'edit' ? 'Update Rock' : 'Add New Rock'}</DialogTitle>
                </DialogHeader>

                <RockForm
                    key={mode}
                    initialData={initialData}
                    mode={mode}
                    onSubmit={onSubmit}
                    onCancel={() => onOpenChange(false)}
                    teamId={editingRock?.team.id}
                />
            </DialogContent>
        </Dialog>
    );
}
