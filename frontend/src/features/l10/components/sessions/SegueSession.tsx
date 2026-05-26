import { Smile } from 'lucide-react';

export function SegueSession() {
    return (
        <div className="flex flex-col items-center justify-center py-16 text-center">
            <Smile className="mb-6 h-16 w-16 text-primary" />
            <h2 className="mb-2 text-2xl font-semibold">Welcome to the L10 Meeting</h2>
            <p className="max-w-md text-muted-foreground">
                Start by sharing good news or personal updates with the team. This session is display-only and helps set a
                positive tone for the meeting.
            </p>
        </div>
    );
}
