import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { TodoForm } from './TodoForm';
import type { TodoFormValues } from '../types/todo';
import type { Todo } from '../types/todo';

interface TodoDialogProps {
    isOpen: boolean;
    onOpenChange: (open: boolean) => void;
    onSubmit: (data: TodoFormValues) => void | Promise<void>;
    editingTodo: Todo | null;
    teamId?: string;
}

export function TodoDialog({ isOpen, onOpenChange, onSubmit, editingTodo, teamId }: Readonly<TodoDialogProps>) {
    const mode = editingTodo ? 'edit' : 'create';

    const initialData: TodoFormValues | undefined = editingTodo
        ? {
              title: editingTodo.title,
              description: editingTodo.description,
              status: editingTodo.status,
              dueDate: editingTodo.dueDate ? new Date(editingTodo.dueDate) : undefined,
              assigneeIds: editingTodo.assignees?.map((assignee) => assignee.id),
          }
        : undefined;

    return (
        <Dialog open={isOpen} onOpenChange={onOpenChange}>
            <DialogContent>
                <DialogHeader>
                    <DialogTitle>{mode === 'edit' ? 'Edit To-do' : 'Add New To-do'}</DialogTitle>
                </DialogHeader>

                <TodoForm
                    key={mode}
                    initialData={initialData}
                    mode={mode}
                    onSubmit={onSubmit}
                    onCancel={() => onOpenChange(false)}
                    teamId={teamId ?? editingTodo?.team.id}
                />
            </DialogContent>
        </Dialog>
    );
}
