import { useState, useCallback, useMemo } from 'react';
import { useNavigate } from '@tanstack/react-router';
import { format, parse } from 'date-fns';
import { ArrowLeft } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { L10MeetingStatusBadge } from './L10MeetingStatusBadge';
import { AgendaSidebar } from './AgendaSidebar';
import { SegueSession } from './sessions/SegueSession';
import { ScorecardSession } from './sessions/ScorecardSession';
import { RockSession } from './sessions/RockSession';
import { HeadlinesSession } from './sessions/HeadlinesSession';
import { TodoSession } from './sessions/TodoSession';
import { IssuesSession } from './sessions/IssuesSession';
import { ConcludeSession } from './sessions/ConcludeSession';
import { useL10Meeting } from '../hooks/useL10Meeting';
import { useUserStore } from '@/stores/useUserStore';
import { formatUserName, isUserNameTruncated } from '../utils/userNameDisplay';
import type { AgendaSession } from '../types/l10Meeting';
import { AGENDA_SESSIONS } from '../types/l10Meeting';

interface MeetingFlowProps {
    meetingId: string;
}

export function MeetingFlow({ meetingId }: MeetingFlowProps) {
    const navigate = useNavigate();
    const currentUser = useUserStore((state) => state.user);
    const { data: meeting, isPending, isError } = useL10Meeting(meetingId);
    const [activeSession, setActiveSession] = useState<AgendaSession>('segue');

    const isFacilitatorOrScribe = useMemo(() => {
        if (!meeting || !currentUser) return false;
        return meeting.facilitator.id === currentUser.id || meeting.scribe.id === currentUser.id;
    }, [meeting, currentUser]);

    const handleBack = useCallback(() => {
        navigate({ to: '/l10-meetings' });
    }, [navigate]);

    const handleFinished = useCallback(() => {
        navigate({ to: '/l10-meetings/$meetingId', params: { meetingId } });
    }, [navigate, meetingId]);

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
                <p className="text-destructive">Failed to load meeting.</p>
            </div>
        );
    }

    const date = parse(meeting.meetingDate, 'yyyy-MM-dd', new Date());
    const formattedDate = format(date, 'MMM d, yyyy');
    const time = meeting.meetingTime.substring(0, 5);
    const currentIndex = AGENDA_SESSIONS.indexOf(activeSession);

    const renderSession = () => {
        switch (activeSession) {
            case 'segue':
                return <SegueSession />;
            case 'scorecard':
                return <ScorecardSession weekStartDate={meeting.weekStartDate} meetingStatus={meeting.status} />;
            case 'rocks':
                return <RockSession />;
            case 'headlines':
                return <HeadlinesSession />;
            case 'todos':
                return <TodoSession />;
            case 'issues':
                return <IssuesSession />;
            case 'conclude':
                return <ConcludeSession meeting={meeting} onFinished={handleFinished} canEdit={isFacilitatorOrScribe} />;
        }
    };

    return (
        <div className="flex h-screen flex-col">
            <div className="flex items-center justify-between border-b px-6 py-4">
                <div className="flex items-center gap-4">
                    <Button variant="ghost" size="icon" onClick={handleBack} aria-label="Back to L10 Meetings">
                        <ArrowLeft className="h-4 w-4" />
                    </Button>
                    <div className="flex items-center gap-3">
                        <h1 className="text-lg font-semibold">L10 Meeting — {formattedDate}</h1>
                        <span className="text-sm text-muted-foreground">{time}</span>
                        <L10MeetingStatusBadge status={meeting.status} />
                    </div>
                </div>
                <div className="flex items-center gap-2 text-sm text-muted-foreground">
                    <span title={isUserNameTruncated(meeting.facilitator.firstName, meeting.facilitator.lastName) ? `${meeting.facilitator.firstName} ${meeting.facilitator.lastName}` : undefined}>
                        Facilitator: {formatUserName(meeting.facilitator.firstName, meeting.facilitator.lastName)}
                    </span>
                    <span className="text-border">|</span>
                    <span title={isUserNameTruncated(meeting.scribe.firstName, meeting.scribe.lastName) ? `${meeting.scribe.firstName} ${meeting.scribe.lastName}` : undefined}>
                        Scribe: {formatUserName(meeting.scribe.firstName, meeting.scribe.lastName)}
                    </span>
                </div>
            </div>

            <div className="flex flex-1 overflow-hidden">
                <AgendaSidebar activeSession={activeSession} onSessionChange={setActiveSession} />

                <div className="flex-1 overflow-y-auto p-6">
                    {renderSession()}

                    <div className="mt-6 flex items-center justify-between border-t pt-4 text-sm text-muted-foreground">
                        <Button variant="ghost" size="sm" onClick={handleBack}>
                            <ArrowLeft className="mr-1 h-4 w-4" />
                            Back to L10 Meetings
                        </Button>
                        <span>
                            Session {currentIndex + 1} of {AGENDA_SESSIONS.length}
                        </span>
                    </div>
                </div>
            </div>
        </div>
    );
}
