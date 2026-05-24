import { useState } from 'react';
import { Plus } from 'lucide-react';
import { toast } from 'sonner';
import { PageHeaderGroup } from '@/components/shared/PageHeaderGroup';
import { CustomPagination } from '@/components/shared/CustomPagination';
import { QueryState } from '@/components/shared/QueryState';
import { EmptyTeamState } from '@/components/shared/EmptyTeamState';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Button } from '@/components/ui/button';
import { DEFAULT_LIMIT } from '@/types/pagination';
import { useActiveTeamId } from '@/hooks/useActiveTeamId';
import {
    useL10Meetings,
    useStartL10Meeting,
    L10MeetingCard,
    ScheduleL10MeetingDialog,
} from '@/features/l10';
import { ERROR_MESSAGES } from '@/constants/messages';

type L10Tab = 'upcoming' | 'finished';

export function L10Meetings() {
    const activeTeamId = useActiveTeamId();
    const [page, setPage] = useState(1);
    const [activeTab, setActiveTab] = useState<L10Tab>('upcoming');
    const [isScheduleDialogOpen, setScheduleDialogOpen] = useState(false);

    const { startMeeting } = useStartL10Meeting();

    const status = activeTab === 'upcoming' ? 'SCHEDULED' : 'FINISHED';

    const handleStart = (meetingId: string) => {
        startMeeting(meetingId);
    };

    const handleResume = () => {
        toast.info('Resume meeting - feature coming soon');
    };

    const handleSummary = () => {
        toast.info('View meeting summary - feature coming soon');
    };

    const {
        data: meetingsResponse,
        isPending,
        isError,
        isPlaceholderData,
    } = useL10Meetings({
        page,
        limit: DEFAULT_LIMIT,
        teamId: activeTeamId!,
        status,
    });

    const meetings = meetingsResponse?.data ?? [];
    const totalPages = meetingsResponse?.pagination.totalPages ?? 0;

    const handleTabChange = (val: string) => {
        setActiveTab(val as L10Tab);
        setPage(1);
    };

    const emptyMessage =
        activeTab === 'upcoming'
            ? 'No upcoming L10 meetings. Schedule one to get started.'
            : 'No finished L10 meetings.';

    const scheduleButton = (
        <Button onClick={() => setScheduleDialogOpen(true)} disabled={!activeTeamId}>
            <Plus className="mr-2 h-4 w-4" aria-hidden="true" />
            Schedule L10
        </Button>
    );

    return (
        <div className="flex flex-col gap-6">
            <PageHeaderGroup title="L10 Meetings" description="Schedule and run productive Level 10 meetings.">
                {scheduleButton}
            </PageHeaderGroup>

            <ScheduleL10MeetingDialog isOpen={isScheduleDialogOpen} onOpenChange={setScheduleDialogOpen} />

            {!activeTeamId ? (
                <EmptyTeamState />
            ) : (
                <Tabs value={activeTab} onValueChange={handleTabChange} className="flex flex-col gap-6">
                    <div>
                        <TabsList className="grid w-[300px] grid-cols-2">
                            <TabsTrigger value="upcoming">Upcoming</TabsTrigger>
                            <TabsTrigger value="finished">Finished</TabsTrigger>
                        </TabsList>
                    </div>

                    <TabsContent value={activeTab} className="m-0 flex flex-col gap-4">
                        <QueryState
                            key={`${activeTab}-${page}`}
                            isPending={isPending && !isPlaceholderData}
                            isError={isError}
                            isEmpty={meetings.length === 0}
                            errorMessage={ERROR_MESSAGES.L10_MEETING.LOAD_FAILED}
                            emptyMessage={emptyMessage}
                        >
                            <div className={isPlaceholderData ? 'opacity-50 transition-opacity' : 'flex flex-col gap-3'}>
                                {meetings.map((meeting) => (
                                    <L10MeetingCard
                                        key={meeting.id}
                                        meeting={meeting}
                                        onStart={() => handleStart(meeting.id)}
                                        onResume={handleResume}
                                        onSummary={handleSummary}
                                    />
                                ))}

                                <CustomPagination currentPage={page} totalPages={totalPages} onPageChange={setPage} />
                            </div>
                        </QueryState>
                    </TabsContent>
                </Tabs>
            )}
        </div>
    );
}
