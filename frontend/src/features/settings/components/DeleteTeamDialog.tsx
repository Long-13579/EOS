import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription } from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';

interface DeleteTeamDialogProps {
    isOpen: boolean;
    onOpenChange: (open: boolean) => void;
    onConfirm: () => void;
    teamName: string;
    isDeleting?: boolean;
}

export function DeleteTeamDialog({ isOpen, onOpenChange, onConfirm, teamName, isDeleting }: DeleteTeamDialogProps) {
    return (
        <Dialog
            open={isOpen}
            onOpenChange={(open) => {
                if (!isDeleting) {
                    onOpenChange(open);
                }
            }}
        >
            <DialogContent>
                <DialogHeader>
                    <DialogTitle>Delete Team</DialogTitle>
                    <DialogDescription className="space-y-2">
                        <p>Are you sure you want to delete <strong>{teamName}</strong>?</p>
                        <p>This will permanently delete all associated data including todos, issues, headlines, metrics, rocks, L10 meetings, and team member assignments. This action cannot be undone.</p>
                    </DialogDescription>
                </DialogHeader>

                <div className="flex justify-end gap-3">
                    <Button type="button" variant="outline" onClick={() => onOpenChange(false)} disabled={isDeleting}>
                        Cancel
                    </Button>
                    <Button type="button" variant="destructive" onClick={onConfirm} disabled={isDeleting}>
                        {isDeleting ? 'Deleting...' : 'Delete'}
                    </Button>
                </div>
            </DialogContent>
        </Dialog>
    );
}
