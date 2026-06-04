import { useState, useId, useCallback } from 'react';
import { useForm, Controller, type SubmitHandler } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { useActiveTeamId } from '@/hooks/useActiveTeamId';
import { useTeamMembers } from '@/features/settings/hooks/useTeamMembers';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { Field, FieldLabel, FieldError } from '@/components/ui/field';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { toast } from 'sonner';
import { normalizeApiError } from '@/utils/apiErrorNormalizer';
import { useFormError } from '@/hooks/useFormError';
import { formatUserName, isUserNameTruncated } from '../../utils/userNameDisplay';
import { useUpdateConclude } from '../../hooks/useUpdateConclude';
import { useUpsertRatings } from '../../hooks/useUpsertRatings';
import { useFinishL10Meeting } from '../../hooks/useFinishL10Meeting';
import { concludeSessionSchema, type ConcludeSessionFormValues } from '../../schemas/l10MeetingSchema';
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
    const formId = useId();
    const teamId = useActiveTeamId();
    const { data: members } = useTeamMembers(teamId);
    const { saveConclude } = useUpdateConclude();
    const { saveRatings } = useUpsertRatings();
    const { finishMeeting, isFinishing } = useFinishL10Meeting();

    const [ratings, setRatings] = useState<Record<string, L10MeetingRatingValue>>({});

    const form = useForm<ConcludeSessionFormValues>({
        resolver: zodResolver(concludeSessionSchema),
        defaultValues: {
            keyDecisions: meeting.concludeKeyDecisions ?? '',
            cascadingMessage: meeting.concludeCascadingMessage ?? '',
        },
        mode: 'onSubmit',
    });

    const { control, handleSubmit, formState: { isSubmitting: formSubmitting } } = form;
    const handleFormError = useFormError(form);

    const isSubmitting = formSubmitting || isFinishing;

    const handleRatingChange = useCallback((memberId: string, value: L10MeetingRatingValue) => {
        setRatings((prev) => ({ ...prev, [memberId]: value }));
    }, []);

    const onSubmit: SubmitHandler<ConcludeSessionFormValues> = useCallback(
        async (data) => {
            if (!members) return;

            try {
                const ratingsPayload = members.map((m) => ({
                    memberId: m.id,
                    rating: ratings[m.id] || 'ABSENT',
                }));

                await saveConclude({
                    meetingId: meeting.id,
                    payload: { keyDecisions: data.keyDecisions, cascadingMessage: data.cascadingMessage },
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
                handleFormError(normalized);
                if (normalized.message) {
                    toast.error(normalized.message);
                }
            }
        },
        [members, ratings, saveConclude, saveRatings, finishMeeting, meeting.id, onFinished, handleFormError],
    );

    return (
        <Card>
            <CardHeader>
                <CardTitle>Conclude</CardTitle>
            </CardHeader>
            <CardContent className="space-y-6">
                <div className="space-y-2">
                    <Controller
                        name="keyDecisions"
                        control={control}
                        render={({ field, fieldState }) => (
                            <Field data-invalid={fieldState.invalid}>
                                <FieldLabel htmlFor={`${formId}-keyDecisions`}>
                                    Key Decisions <span className="text-destructive">*</span>
                                </FieldLabel>
                                <Textarea
                                    {...field}
                                    id={`${formId}-keyDecisions`}
                                    placeholder="Enter key decisions made during this meeting..."
                                    rows={4}
                                    disabled={isSubmitting || !canEdit}
                                    aria-invalid={fieldState.invalid}
                                />
                                {fieldState.error && <FieldError errors={[fieldState.error]} />}
                            </Field>
                        )}
                    />
                </div>

                <div className="space-y-2">
                    <Controller
                        name="cascadingMessage"
                        control={control}
                        render={({ field, fieldState }) => (
                            <Field data-invalid={fieldState.invalid}>
                                <FieldLabel htmlFor={`${formId}-cascadingMessage`}>
                                    Cascading Message <span className="text-destructive">*</span>
                                </FieldLabel>
                                <Textarea
                                    {...field}
                                    id={`${formId}-cascadingMessage`}
                                    placeholder="Enter the cascading message for team members who were absent..."
                                    rows={4}
                                    disabled={isSubmitting}
                                    aria-invalid={fieldState.invalid}
                                />
                                {fieldState.error && <FieldError errors={[fieldState.error]} />}
                            </Field>
                        )}
                    />
                </div>

                {members && members.length > 0 && (
                    <div className="space-y-3">
                        <Label>Team Member Ratings (1-10 or Absent)</Label>
                        {members.map((member) => (
                            <div key={member.id} className="flex items-center gap-3 rounded-md border p-3">
                                <span
                                    className="min-w-0 flex-1 text-sm font-medium truncate"
                                    title={isUserNameTruncated(member.firstName, member.lastName) ? `${member.firstName} ${member.lastName}` : undefined}
                                >
                                    {formatUserName(member.firstName, member.lastName)}
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
                        <Button onClick={handleSubmit(onSubmit)} disabled={isSubmitting} size="lg">
                            {isSubmitting ? 'Finishing Meeting...' : 'Finish Meeting'}
                        </Button>
                    </div>
                )}
            </CardContent>
        </Card>
    );
}
