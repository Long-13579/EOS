import { apiGet, apiPost, apiPut, apiDelete, apiPatch } from '@/utils/apiRequest';
import type { CreateTodo, GetTodosParams, Todo, UpdateTodo, ArchiveTodo } from '../types/todo';
import type { PaginatedResponse, PaginationParams } from '@/types/pagination';

export const getTodos = (params: GetTodosParams): Promise<PaginatedResponse<Todo>> => apiGet<PaginatedResponse<Todo>>('/todos', { params });

export const createTodo = (payload: CreateTodo): Promise<Todo> => apiPost<Todo>('/todos', payload);

export const updateTodo = (id: string, payload: UpdateTodo): Promise<Todo> => apiPut<Todo>(`/todos/${id}`, payload);

export const archiveTodo = (id: string, payload: ArchiveTodo): Promise<Todo> => apiPatch<Todo>(`/todos/${id}`, payload);

export const deleteTodo = (id: string): Promise<void> => apiDelete<void>(`/todos/${id}`);

export const getMyTodos = (params: PaginationParams): Promise<PaginatedResponse<Todo>> => apiGet<PaginatedResponse<Todo>>('/todos/me', { params });
