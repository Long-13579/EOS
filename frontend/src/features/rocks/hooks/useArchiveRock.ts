import { useMutation, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { archiveRock } from '../services/rockService';
import type { Rock } from '../types/rock';
import { ERROR_MESSAGES, SUCCESS_MESSAGES } from '@/constants/messages';
import { rockKeys } from '../types/rockKeys';

interface ArchiveRockParams {
    id: string;
    isArchived: boolean;
}

export const useArchiveRock = () => {
    const queryClient = useQueryClient();

    const mutation = useMutation({
        mutationFn: ({ id, isArchived }: ArchiveRockParams) => archiveRock(id, isArchived),

        onError: (_err, variables) => {
            toast.error(variables.isArchived ? ERROR_MESSAGES.ROCK.ARCHIVE_FAILED : ERROR_MESSAGES.ROCK.UNARCHIVE_FAILED);
        },

        onSuccess: (_data, variables) => {
            toast.success(variables.isArchived ? SUCCESS_MESSAGES.ROCK.ARCHIVED : SUCCESS_MESSAGES.ROCK.UNARCHIVED);
        },
        onSettled: () => {
            queryClient.invalidateQueries({
                queryKey: rockKeys.lists(),
            });
            queryClient.invalidateQueries({
                queryKey: rockKeys.myRocks(),
            });
        },
    });

    const handleArchive = (rock: Rock) => {
        mutation.mutate({
            id: rock.id,
            isArchived: !rock.isArchived,
        });
    };

    return {
        handleArchive,
        isPending: mutation.isPending,
    };
};
