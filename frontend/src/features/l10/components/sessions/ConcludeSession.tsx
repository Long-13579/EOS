import { useState, useCallback } from 'react';
import { useActiveTeamId } from '@/hooks/useActiveTeamId';
import { useTeamMembers } from '@/features/settings/hooks/useTeamMembers';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { toast } from 'sonner';
import { normalizeApiError } from '@/utils/apiErrorNormalizer';
import { useUpdateConclude } from '../../hooks/useUpdateConclude';
import { useUpsertRatings } from '../../hooks/useUpsertRatings';
import { useFinishL10Meeting } from '../../hooks/useFinishL10Meeting';
import type { L10Meeting, L10MeetingRatingValue } from '../../types/l10Meeting';

const RATING_OPTIONS: { value: L10MeetingRatingValue; label: string }[] = [
    { value: 'ABSENT', label: 'Absent' },
    { value: 'ONE', label: '1' },
    { value: 'TWO', label: '2' },
    { value: 'THREE', label: '3' },
    { value: 'FOUR', label: '4' },
    { value: 'FIVE', label: '5' },
    { value: 'SIX', label: '6' },
    { value: 'SEVEN', label: '7' },
    { value: 'EIGHT', label: '8' },
    { value: 'NINE', label: '9' },
    { value: 'TEN', label: '10' },
];

interface ConcludeSessionProps {
    meeting: L10Meeting;
    onFinished: () => void;
    canEdit: boolean;
}

export function ConcludeSession({ meeting, onFinished, canEdit }: ConcludeSessionProps) {
    const teamId = useActiveTeamId();
    const { data: members } = useTeamMembers(teamId);
    const { saveConclude } = useUpdateConclude();
    const { saveRatings } = useUpsertRatings();
    const { finishMeeting, isFinishing } = useFinishL10Meeting();

    const [keyDecisions, setKeyDecisions] = useState(meeting.concludeKeyDecisions ?? '');
    const [cascadingMessage, setCascadingMessage] = useState(meeting.concludeCascadingMessage ?? '');
    const [ratings, setRatings] = useState<Record<string, L10MeetingRatingValue>>({});
    const [isSaving, setIsSaving] = useState(false);

    const handleRatingChange = useCallback((memberId: string, value: L10MeetingRatingValue) => {
        setRatings((prev) => ({ ...prev, [memberId]: value }));
    }, []);

    const handleFinish = useCallback(async () => {
        if (!members) return;

        setIsSaving(true);
        try {
            const ratingsPayload = members.map((m) => ({
                memberId: m.id,
                rating: ratings[m.id] || 'ABSENT',
            }));

            await saveConclude({
                meetingId: meeting.id,
                payload: { keyDecisions, cascadingMessage },
            });

            await saveRatings({
                meetingId: meeting.id,
                payload: { ratings: ratingsPayload },
            });

            await finishMeeting(meeting.id);

            toast.success('L10 meeting finished successfully!');
            onFinished();
        } catch (error) {
            const normalized = normalizeApiError(error);
            toast.error(normalized.message);
        } finally {
            setIsSaving(false);
        }
    }, [members, ratings, keyDecisions, cascadingMessage, saveConclude, saveRatings, finishMeeting, meeting.id, onFinished]);

    const isSubmitting = isSaving || isFinishing;

    return (
        <Card>
            <CardHeader>
                <CardTitle>Conclude</CardTitle>
            </CardHeader>
            <CardContent className="space-y-6">
                <div className="space-y-2">
                    <Label htmlFor="keyDecisions">Key Decisions</Label>
                    <Textarea
                        id="keyDecisions"
                        value={keyDecisions}
                        onChange={(e) => setKeyDecisions(e.target.value)}
                        placeholder="Enter key decisions made during this meeting..."
                        rows={4}
                        disabled={isSubmitting || !canEdit}
                    />
                </div>

                <div className="space-y-2">
                    <Label htmlFor="cascadingMessage">Cascading Message</Label>
                    <Textarea
                        id="cascadingMessage"
                        value={cascadingMessage}
                        onChange={(e) => setCascadingMessage(e.target.value)}
                        placeholder="Enter the cascading message for team members who were absent..."
                        rows={4}
                        disabled={isSubmitting}
                    />
                </div>

                {members && members.length > 0 && (
                    <div className="space-y-3">
                        <Label>Team Member Ratings (1-10 or Absent)</Label>
                        {members.map((member) => (
                            <div key={member.id} className="flex items-center gap-3 rounded-md border p-3">
                                <span className="min-w-0 flex-1 text-sm font-medium truncate">
                                    {member.firstName} {member.lastName}
                                </span>
                                <Select
                                    value={ratings[member.id] || 'ABSENT'}
                                    onValueChange={(val) => handleRatingChange(member.id, val as L10MeetingRatingValue)}
                                    disabled={isSubmitting}
                                >
                                    <SelectTrigger className="w-28">
                                        <SelectValue placeholder="Rating" />
                                    </SelectTrigger>
                                    <SelectContent>
                                        {RATING_OPTIONS.map((opt) => (
                                            <SelectItem key={opt.value} value={opt.value}>
                                                {opt.label}
                                            </SelectItem>
                                        ))}
                                    </SelectContent>
                                </Select>
                            </div>
                        ))}
                    </div>
                )}

                {canEdit && (
                    <div className="flex justify-end gap-3 pt-4">
                        <Button onClick={handleFinish} disabled={isSubmitting} size="lg">
                            {isSubmitting ? 'Finishing Meeting...' : 'Finish Meeting'}
                        </Button>
                    </div>
                )}
            </CardContent>
        </Card>
    );
}
