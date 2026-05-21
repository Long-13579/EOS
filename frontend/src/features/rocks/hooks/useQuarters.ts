import { useQuery } from '@tanstack/react-query';
import { getQuarters } from '../services/rockService';
import { rockKeys } from '../types/rockKeys';

export const useQuarters = () => {
    return useQuery({
        queryKey: rockKeys.quarters(),
        queryFn: getQuarters,
    });
};
