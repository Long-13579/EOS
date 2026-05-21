import { apiGet } from '@/utils/apiRequest';
import type { Week } from '../types/week';

export const getWeeks = (): Promise<Week[]> => apiGet('/weeks');
