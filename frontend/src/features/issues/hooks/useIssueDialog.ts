import { useState } from 'react';
import type { Issue, IssueFormValues } from '../types/issue';
import { useCreateIssue } from './useCreateIssue';
import { useUpdateIssue } from './useUpdateIssue';

type UseIssueDialogParams = {
    activeTeamId?: string;
    onCreated?: () => void;
};

export function useIssueDialog({ activeTeamId, onCreated }: UseIssueDialogParams) {
    const [isIssueDialogOpen, setIssueDialogOpen] = useState(false);
    const [editingIssue, setEditingIssue] = useState<Issue | null>(null);

    const { createIssue } = useCreateIssue();
    const { updateIssue } = useUpdateIssue();

    const openCreate = () => {
        setEditingIssue(null);
        setIssueDialogOpen(true);
    };

    const openEdit = (issue: Issue) => {
        setEditingIssue(issue);
        setIssueDialogOpen(true);
    };

    const handleSubmit = async (data: IssueFormValues) => {
        if (!activeTeamId) {
            return;
        }

        if (editingIssue) {
            await updateIssue({
                id: editingIssue.id,
                data,
            });
        } else {
            await createIssue({
                ...data,
                teamId: activeTeamId,
            });

            onCreated?.();
        }

        setIssueDialogOpen(false);
    };

    return {
        isIssueDialogOpen,
        setIssueDialogOpen,
        editingIssue,
        openCreate,
        openEdit,
        handleSubmit,
    };
}
