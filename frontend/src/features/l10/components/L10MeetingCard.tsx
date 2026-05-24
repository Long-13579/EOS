import { format, parse } from 'date-fns';
import { Card, CardContent } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { L10MeetingStatusBadge } from './L10MeetingStatusBadge';
import type { L10Meeting, L10MeetingStatus } from '../types/l10Meeting';

interface L10MeetingCardProps {
    meeting: L10Meeting;
    onStart?: () => void;
    onResume?: () => void;
    onSummary?: () => void;
}

const MEETING_ACTION_CONFIG: Record<L10MeetingStatus, { label: string; variant: 'default' | 'destructive' | 'outline' | 'secondary' | 'ghost' | 'link' }> = {
    SCHEDULED: { label: 'Start', variant: 'default' },
    STARTED: { label: 'Resume', variant: 'outline' },
    FINISHED: { label: 'Summary', variant: 'outline' },
};

export function L10MeetingCard({ meeting, onStart, onResume, onSummary }: L10MeetingCardProps) {
    const date = parse(meeting.meetingDate, 'yyyy-MM-dd', new Date());
    const formattedDate = format(date, 'MMM d, yyyy');

    const time = meeting.meetingTime.substring(0, 5);

    const actionConfig = MEETING_ACTION_CONFIG[meeting.status];

    const handleAction = () => {
        switch (meeting.status) {
            case 'SCHEDULED':
                onStart?.();
                break;
            case 'STARTED':
                onResume?.();
                break;
            case 'FINISHED':
                onSummary?.();
                break;
        }
    };

    return (
        <Card className="py-4">
            <CardContent className="flex items-center justify-between gap-4 px-6">
                <div className="flex flex-col gap-1">
                    <div className="flex items-center gap-3">
                        <p className="text-sm font-semibold text-foreground">{formattedDate}</p>
                        <p className="text-sm text-muted-foreground">{time}</p>
                        <L10MeetingStatusBadge status={meeting.status} />
                    </div>
                    <div className="flex items-center gap-4 text-sm text-muted-foreground">
                        <span>
                            <span className="font-medium text-foreground">Facilitator:</span>{' '}
                            {meeting.facilitator.firstName} {meeting.facilitator.lastName}
                        </span>
                        <span>
                            <span className="font-medium text-foreground">Scribe:</span>{' '}
                            {meeting.scribe.firstName} {meeting.scribe.lastName}
                        </span>
                    </div>
                </div>
                <Button variant={actionConfig.variant} onClick={handleAction}>
                    {actionConfig.label}
                </Button>
            </CardContent>
        </Card>
    );
}
