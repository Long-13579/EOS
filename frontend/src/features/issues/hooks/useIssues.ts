import { useQuery, keepPreviousData } from '@tanstack/react-query';
import { issueKeys } from '../types/issueKeys';
import { getIssues } from '../services/issueService';
import { LONG_TERM_ISSUE, ISSUE_VIEW, type GetIssuesParams, type IssueView } from '../types/issue';
import { useIssueTypes } from './useIssueTypes';

type UseIssuesParams = Omit<GetIssuesParams, 'isArchived' | 'issueTypeId'> & {
    view?: IssueView;
};

export const useIssues = ({ view, ...params }: UseIssuesParams) => {
    const isLongTermView = view === ISSUE_VIEW.LONG_TERM;
    const isArchivedView = view === ISSUE_VIEW.ARCHIVED;
    const {
        data: issueTypes,
        isPending: isIssueTypesLoading,
        isError: isIssueTypesError,
    } = useIssueTypes({ enabled: isLongTermView && !!params.teamId });
    const longTermIssueType = issueTypes?.find((type) => type.name === LONG_TERM_ISSUE);
    const isMissingLongTermIssueType = isLongTermView && !isIssueTypesLoading && !longTermIssueType;

    const queryParams: GetIssuesParams = {
        ...params,
        isArchived: isArchivedView ? true : undefined,
        issueTypeId: isLongTermView ? longTermIssueType?.id : undefined,
    };

    const query = useQuery({
        queryKey: issueKeys.list({ ...queryParams, view }),
        queryFn: () => getIssues(queryParams),
        enabled: !!params.teamId && (!isLongTermView || !!longTermIssueType?.id),
        placeholderData: isLongTermView ? undefined : keepPreviousData,
    });

    return {
        ...query,
        isLongTermView,
        isArchivedView,
        isIssueTypesError,
        isMissingLongTermIssueType,
    };
};
