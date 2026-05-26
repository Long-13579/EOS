import { useQuery } from '@tanstack/react-query';
import { l10MeetingKeys } from '../types/l10MeetingKeys';
import { getL10Meeting } from '../services/l10MeetingService';

export const useL10Meeting = (meetingId: string) => {
    return useQuery({
        queryKey: l10MeetingKeys.detail(meetingId),
        queryFn: () => getL10Meeting(meetingId),
        enabled: !!meetingId,
    });
};
