import type { Todo, TodoStatus } from '../types/todo';

const STATUS_PRIORITY: Record<TodoStatus, number> = {
    NOT_STARTED: 1,
    IN_PROGRESS: 1,
    COMPLETED: 2,
};

export const sortTodos = (firstTodo: Todo, secondTodo: Todo) => {
    const firstTodoStatus = STATUS_PRIORITY[firstTodo.status];
    const secondTodoStatus = STATUS_PRIORITY[secondTodo.status];

    if (firstTodoStatus !== secondTodoStatus) {
        return firstTodoStatus - secondTodoStatus;
    }

    if (!firstTodo.dueDate && !secondTodo.dueDate) {
        return 0;
    }
    if (!firstTodo.dueDate) {
        return 1;
    }
    if (!secondTodo.dueDate) {
        return -1;
    }

    return new Date(firstTodo.dueDate).getTime() - new Date(secondTodo.dueDate).getTime();
};

export const matchesStatusFilter = (todo: Todo, status?: Todo['status']) => !status || todo.status === status;
