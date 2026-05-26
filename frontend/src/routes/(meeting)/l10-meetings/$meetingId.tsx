import { useMemo } from 'react';
import { createFileRoute, notFound } from '@tanstack/react-router';
import { MeetingFlow, MeetingSummary, useL10Meeting } from '@/features/l10';

function MeetingDetail() {
    const { meetingId } = Route.useParams();
    const { data: meeting, isPending, isError } = useL10Meeting(meetingId);

    const isFinished = useMemo(() => {
        return meeting?.status === 'FINISHED';
    }, [meeting]);

    if (isPending) {
        return (
            <div className="flex items-center justify-center py-16">
                <p className="text-muted-foreground">Loading meeting...</p>
            </div>
        );
    }

    if (isError || !meeting) {
        return (
            <div className="flex items-center justify-center py-16">
                <p className="text-destructive">Meeting not found.</p>
            </div>
        );
    }

    if (isFinished) {
        return <MeetingSummary meetingId={meetingId} />;
    }

    return <MeetingFlow meetingId={meetingId} />;
}

export const Route = createFileRoute('/(meeting)/l10-meetings/$meetingId')({
    component: MeetingDetail,
    beforeLoad: ({ params }) => {
        if (!params.meetingId) {
            throw notFound();
        }
    },
});
