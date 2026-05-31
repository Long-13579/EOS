import { useState } from 'react';
import type { Issue } from '../types/issue';

export function useIssueTodosDialog() {
    const [isIssueTodosDialogOpen, setIssueTodosDialogOpen] = useState(false);
    const [issue, setIssue] = useState<Issue | null>(null);

    const openIssueTodos = (selectedIssue: Issue) => {
        setIssue(selectedIssue);
        setIssueTodosDialogOpen(true);
    };

    const closeIssueTodos = () => {
        setIssueTodosDialogOpen(false);
        setIssue(null);
    };

    return {
        isIssueTodosDialogOpen,
        setIssueTodosDialogOpen,
        issue,
        openIssueTodos,
        closeIssueTodos,
    };
}
