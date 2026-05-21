import { useQuery } from '@tanstack/react-query';
import { getYears } from '../services/rockService';
import { rockKeys } from '../types/rockKeys';

export const useYears = () => {
    return useQuery({
        queryKey: rockKeys.years(),
        queryFn: getYears,
    });
};
