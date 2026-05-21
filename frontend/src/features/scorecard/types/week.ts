import type { BaseEntity } from '@/types/base';

export interface Week extends BaseEntity {
    id: string;
    startDate: string;
    endDate: string;
}
