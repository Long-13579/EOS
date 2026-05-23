import { format, parse } from 'date-fns';
import { Card, CardContent } from '@/components/ui/card';
import { L10MeetingStatusBadge } from './L10MeetingStatusBadge';
import type { L10Meeting } from '../types/l10Meeting';

interface L10MeetingCardProps {
    meeting: L10Meeting;
}

export function L10MeetingCard({ meeting }: L10MeetingCardProps) {
    const date = parse(meeting.meetingDate, 'yyyy-MM-dd', new Date());
    const formattedDate = format(date, 'MMM d, yyyy');

    const time = meeting.meetingTime.substring(0, 5);

    return (
        <Card className="py-4">
            <CardContent className="flex items-center justify-between gap-4 px-6">
                <div className="flex items-center gap-6">
                    <div className="text-left">
                        <p className="text-sm font-semibold text-foreground">{formattedDate}</p>
                        <p className="text-sm text-muted-foreground">{time}</p>
                    </div>
                    <div className="text-sm text-muted-foreground">
                        <span className="font-medium text-foreground">Facilitator:</span>{' '}
                        {meeting.facilitator.firstName} {meeting.facilitator.lastName}
                    </div>
                </div>
                <L10MeetingStatusBadge status={meeting.status} />
            </CardContent>
        </Card>
    );
}
