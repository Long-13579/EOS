import { useCallback } from 'react';
import { useNavigate } from '@tanstack/react-router';
import { format, parse } from 'date-fns';
import { ArrowLeft } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { L10MeetingStatusBadge } from './L10MeetingStatusBadge';
import { useL10Meeting } from '../hooks/useL10Meeting';

interface MeetingSummaryProps {
    meetingId: string;
}

export function MeetingSummary({ meetingId }: MeetingSummaryProps) {
    const navigate = useNavigate();
    const { data: meeting, isPending, isError } = useL10Meeting(meetingId);

    const handleBack = useCallback(() => {
        navigate({ to: '/l10-meetings' });
    }, [navigate]);

    if (isPending) {
        return (
            <div className="flex items-center justify-center py-16">
                <p className="text-muted-foreground">Loading meeting summary...</p>
            </div>
        );
    }

    if (isError || !meeting) {
        return (
            <div className="flex items-center justify-center py-16">
                <p className="text-destructive">Failed to load meeting summary.</p>
            </div>
        );
    }

    const date = parse(meeting.meetingDate, 'yyyy-MM-dd', new Date());
    const formattedDate = format(date, 'MMM d, yyyy');
    const time = meeting.meetingTime.substring(0, 5);

    return (
        <div className="mx-auto max-w-3xl space-y-6 py-8">
            <div className="flex items-center gap-4">
                <Button variant="ghost" size="icon" onClick={handleBack} aria-label="Back to L10 Meetings">
                    <ArrowLeft className="h-4 w-4" />
                </Button>
                <div className="flex items-center gap-3">
                    <h1 className="text-xl font-semibold">L10 Meeting Summary — {formattedDate}</h1>
                    <span className="text-sm text-muted-foreground">{time}</span>
                    <L10MeetingStatusBadge status={meeting.status} />
                </div>
            </div>

            <div className="flex gap-6 text-sm text-muted-foreground">
                <span>
                    Facilitator: {meeting.facilitator.firstName} {meeting.facilitator.lastName}
                </span>
                <span>
                    Scribe: {meeting.scribe.firstName} {meeting.scribe.lastName}
                </span>
            </div>

            <Card>
                <CardHeader>
                    <CardTitle>Key Decisions</CardTitle>
                </CardHeader>
                <CardContent>
                    <p className="whitespace-pre-wrap text-sm">{meeting.concludeKeyDecisions || 'No key decisions recorded.'}</p>
                </CardContent>
            </Card>

            <Card>
                <CardHeader>
                    <CardTitle>Cascading Message</CardTitle>
                </CardHeader>
                <CardContent>
                    <p className="whitespace-pre-wrap text-sm">{meeting.concludeCascadingMessage || 'No cascading message recorded.'}</p>
                </CardContent>
            </Card>
        </div>
    );
}
