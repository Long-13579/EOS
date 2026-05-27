import { useCallback, useMemo } from 'react';
import { useNavigate } from '@tanstack/react-router';
import { format, parse } from 'date-fns';
import { ArrowLeft } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { L10MeetingStatusBadge } from './L10MeetingStatusBadge';
import { useL10Meeting } from '../hooks/useL10Meeting';
import { useTeamMembers } from '@/features/settings/hooks/useTeamMembers';
import { formatUserName, isUserNameTruncated } from '../utils/userNameDisplay';
import type { L10MeetingRatingValue } from '../types/l10Meeting';

const RATING_LABEL: Record<L10MeetingRatingValue, string> = {
    ABSENT: 'Absent',
    ONE: '1/10',
    TWO: '2/10',
    THREE: '3/10',
    FOUR: '4/10',
    FIVE: '5/10',
    SIX: '6/10',
    SEVEN: '7/10',
    EIGHT: '8/10',
    NINE: '9/10',
    TEN: '10/10',
};

interface MeetingSummaryProps {
    meetingId: string;
}

export function MeetingSummary({ meetingId }: MeetingSummaryProps) {
    const navigate = useNavigate();
    const { data: meeting, isPending, isError } = useL10Meeting(meetingId);
    const { data: members } = useTeamMembers(meeting?.team.id);

    const ratingsByMemberId = useMemo(() => {
        if (!meeting?.ratings) return {};
        const map: Record<string, L10MeetingRatingValue> = {};
        for (const r of meeting.ratings) {
            map[r.member.id] = r.rating;
        }
        return map;
    }, [meeting?.ratings]);

    const ratingsDisplay = useMemo(() => {
        if (!members) return [];
        return members
            .map((m) => ({
                member: m,
                rating: ratingsByMemberId[m.id] ?? null,
            }))
            .sort((a, b) => {
                const cmp = a.member.lastName.localeCompare(b.member.lastName);
                if (cmp !== 0) return cmp;
                return a.member.firstName.localeCompare(b.member.firstName);
            });
    }, [members, ratingsByMemberId]);

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
                <span title={isUserNameTruncated(meeting.facilitator.firstName, meeting.facilitator.lastName) ? `${meeting.facilitator.firstName} ${meeting.facilitator.lastName}` : undefined}>
                    Facilitator: {formatUserName(meeting.facilitator.firstName, meeting.facilitator.lastName)}
                </span>
                <span title={isUserNameTruncated(meeting.scribe.firstName, meeting.scribe.lastName) ? `${meeting.scribe.firstName} ${meeting.scribe.lastName}` : undefined}>
                    Scribe: {formatUserName(meeting.scribe.firstName, meeting.scribe.lastName)}
                </span>
            </div>

            <Card>
                <CardHeader>
                    <CardTitle>Member Ratings</CardTitle>
                </CardHeader>
                <CardContent>
                    {ratingsDisplay.length === 0 ? (
                        <p className="text-sm text-muted-foreground">No ratings available.</p>
                    ) : (
                        <div className="space-y-2">
                            {ratingsDisplay.map(({ member, rating }) => (
                                <div
                                    key={member.id}
                                    className="flex items-center justify-between rounded-md border px-4 py-2.5"
                                >
                                    <span
                                        className="text-sm font-medium"
                                        title={isUserNameTruncated(member.firstName, member.lastName) ? `${member.firstName} ${member.lastName}` : undefined}
                                    >
                                        {formatUserName(member.firstName, member.lastName)}
                                    </span>
                                    <span className="text-sm tabular-nums">
                                        {rating ? RATING_LABEL[rating] : <span className="text-muted-foreground italic">Not rated</span>}
                                    </span>
                                </div>
                            ))}
                        </div>
                    )}
                </CardContent>
            </Card>

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
