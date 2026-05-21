import { useMutation, useMutationState, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';

import { ERROR_MESSAGES, SUCCESS_MESSAGES } from '@/constants/messages';

import { headlineKeys } from '../types/headlineKeys';
import type { Headline } from '../types/headline';
import { archiveHeadline } from '../services/headlineService';

interface ArchiveHeadlineParams {
    id: string;
    isArchived: boolean;
}

export const useArchiveHeadline = () => {
    const queryClient = useQueryClient();

    const { mutate } = useMutation({
        mutationKey: headlineKeys.archiveHeadline(),
        mutationFn: ({ id, isArchived }: ArchiveHeadlineParams) => archiveHeadline(id, { isArchived }),
        onError: (_error, { isArchived }) => {
            toast.error(isArchived ? ERROR_MESSAGES.HEADLINE.ARCHIVE_FAILED : ERROR_MESSAGES.HEADLINE.UNARCHIVE_FAILED);
        },

        onSuccess: (serverHeadline, { id, isArchived }) => {
            queryClient.setQueryData(headlineKeys.detail(id), serverHeadline);
            toast.success(isArchived ? SUCCESS_MESSAGES.HEADLINE.ARCHIVED : SUCCESS_MESSAGES.HEADLINE.UNARCHIVED);
        },

        onSettled: () => {
            if (queryClient.isMutating({ mutationKey: headlineKeys.archiveHeadline() }) <= 1) {
                queryClient.invalidateQueries({
                    queryKey: headlineKeys.lists(),
                });
            }
        },
    });

    const archivingHeadlineIds = useMutationState({
        filters: { mutationKey: headlineKeys.archiveHeadline(), status: 'pending' },
        select: (mutation) => (mutation.state.variables as ArchiveHeadlineParams).id,
    });

    const handleArchiveToggle = (headline: Headline) => {
        mutate({ id: headline.id, isArchived: !headline.isArchived });
    };

    return {
        handleArchiveToggle,
        archivingHeadlineIds,
    };
};
