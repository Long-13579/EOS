import { useQuery, keepPreviousData } from '@tanstack/react-query';
import { l10MeetingKeys } from '../types/l10MeetingKeys';
import { getL10Meetings } from '../services/l10MeetingService';
import type { GetL10MeetingsParams } from '../types/l10Meeting';

export const useL10Meetings = (params: GetL10MeetingsParams) => {
    return useQuery({
        queryKey: l10MeetingKeys.list(params),
        queryFn: () => getL10Meetings(params),
        enabled: !!params.teamId,
        placeholderData: keepPreviousData,
    });
};
