import { useQuery } from '@tanstack/react-query';
import { getIssueTypes } from '../services/issueService';
import { issueKeys } from '../types/issueKeys';

interface UseIssueTypesParams {
    enabled?: boolean;
}

export const useIssueTypes = ({ enabled = true }: UseIssueTypesParams = {}) =>
    useQuery({
        queryKey: issueKeys.issueTypes(),
        queryFn: () => getIssueTypes(),
        enabled,
        retry: false,
    });
