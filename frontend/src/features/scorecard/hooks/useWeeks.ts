import { useQuery } from '@tanstack/react-query';
import { getWeeks } from '../services/weekService';
import { weekKeys } from '../types/weekKeys';

export const useWeeks = () => {
    return useQuery({
        queryKey: weekKeys.list(),
        queryFn: getWeeks,
    });
};
