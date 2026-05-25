import { useId, useCallback } from 'react';
import { useForm, Controller, type SubmitHandler } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { format } from 'date-fns';
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Field, FieldLabel, FieldError, FieldGroup } from '@/components/ui/field';
import { DatePicker } from '@/components/shared/DatePicker';
import { TeamMemberSelect } from '@/components/shared/TeamMemberSelect';
import { toast } from 'sonner';
import { normalizeApiError } from '@/utils/apiErrorNormalizer';
import { useFormError } from '@/hooks/useFormError';
import { useUpdateL10Meeting } from '../hooks/useUpdateL10Meeting';
import { useActiveTeamId } from '@/hooks/useActiveTeamId';
import { scheduleL10MeetingSchema, type ScheduleL10MeetingFormValues } from '../schemas/l10MeetingSchema';
import type { L10Meeting } from '../types/l10Meeting';

interface EditL10MeetingDialogProps {
    isOpen: boolean;
    onOpenChange: (open: boolean) => void;
    meeting: L10Meeting;
}

export function EditL10MeetingDialog({ isOpen, onOpenChange, meeting }: EditL10MeetingDialogProps) {
    const formId = useId();
    const activeTeamId = useActiveTeamId();
    const { updateMeeting, isUpdating } = useUpdateL10Meeting();

    const form = useForm<ScheduleL10MeetingFormValues>({
        resolver: zodResolver(scheduleL10MeetingSchema),
        defaultValues: {
            meetingDate: new Date(meeting.meetingDate + 'T00:00:00'),
            meetingTime: meeting.meetingTime.substring(0, 5),
            facilitatorId: meeting.facilitator.id,
            scribeId: meeting.scribe.id,
        },
        mode: 'onSubmit',
    });

    const { reset, control } = form;
    const handleFormError = useFormError(form);

    const handleSubmit: SubmitHandler<ScheduleL10MeetingFormValues> = useCallback(
        async (data) => {
            if (!activeTeamId) {
                return;
            }

            try {
                await updateMeeting({
                    meetingId: meeting.id,
                    payload: {
                        meetingDate: format(data.meetingDate, 'yyyy-MM-dd'),
                        meetingTime: data.meetingTime,
                        facilitatorId: data.facilitatorId,
                        scribeId: data.scribeId,
                    },
                });

                onOpenChange(false);
            } catch (error) {
                const normalized = normalizeApiError(error);
                const isHandledByForm = handleFormError(normalized);

                if (!isHandledByForm) {
                    toast.error(normalized.message);
                }
            }
        },
        [activeTeamId, updateMeeting, meeting.id, onOpenChange, handleFormError],
    );

    const handleOpenChange = (open: boolean) => {
        if (!isUpdating) {
            if (!open) {
                reset();
            }
            onOpenChange(open);
        }
    };

    const isSubmitting = isUpdating;

    return (
        <Dialog open={isOpen} onOpenChange={handleOpenChange}>
            <DialogContent>
                <DialogHeader>
                    <DialogTitle>Edit L10 Meeting</DialogTitle>
                </DialogHeader>

                <form
                    id={formId}
                    onSubmit={form.handleSubmit(handleSubmit)}
                    className="space-y-6"
                    aria-busy={isSubmitting}
                    aria-label="Edit L10 meeting form"
                >
                    <FieldGroup className="gap-5">
                        <Controller
                            name="meetingDate"
                            control={control}
                            render={({ field, fieldState }) => (
                                <Field data-invalid={fieldState.invalid}>
                                    <FieldLabel htmlFor={`${formId}-date`}>
                                        Meeting Date <span className="text-destructive">*</span>
                                    </FieldLabel>
                                    <DatePicker
                                        id={`${formId}-date`}
                                        value={field.value}
                                        onChange={(val) => field.onChange(val ?? undefined)}
                                        isDisabled={isSubmitting}
                                    />
                                    {fieldState.error && <FieldError errors={[fieldState.error]} />}
                                </Field>
                            )}
                        />

                        <Controller
                            name="meetingTime"
                            control={control}
                            render={({ field, fieldState }) => (
                                <Field data-invalid={fieldState.invalid}>
                                    <FieldLabel htmlFor={`${formId}-time`}>
                                        Meeting Time <span className="text-destructive">*</span>
                                    </FieldLabel>
                                    <Input {...field} id={`${formId}-time`} type="time" aria-invalid={fieldState.invalid} disabled={isSubmitting} />
                                    {fieldState.error && <FieldError errors={[fieldState.error]} />}
                                </Field>
                            )}
                        />

                        <Controller
                            name="facilitatorId"
                            control={control}
                            render={({ field, fieldState }) => (
                                <Field data-invalid={fieldState.invalid}>
                                    <FieldLabel htmlFor={`${formId}-facilitator`}>
                                        Facilitator <span className="text-destructive">*</span>
                                    </FieldLabel>
                                    <TeamMemberSelect value={field.value} onChange={field.onChange} disabled={isSubmitting} teamId={activeTeamId} />
                                    {fieldState.error && <FieldError errors={[fieldState.error]} />}
                                </Field>
                            )}
                        />

                        <Controller
                            name="scribeId"
                            control={control}
                            render={({ field, fieldState }) => (
                                <Field data-invalid={fieldState.invalid}>
                                    <FieldLabel htmlFor={`${formId}-scribe`}>
                                        Scribe <span className="text-destructive">*</span>
                                    </FieldLabel>
                                    <TeamMemberSelect value={field.value} onChange={field.onChange} disabled={isSubmitting} teamId={activeTeamId} />
                                    {fieldState.error && <FieldError errors={[fieldState.error]} />}
                                </Field>
                            )}
                        />
                    </FieldGroup>

                    <div className="flex justify-end gap-3">
                        <Button
                            type="button"
                            variant="outline"
                            onClick={() => {
                                reset();
                                onOpenChange(false);
                            }}
                            disabled={isSubmitting}
                        >
                            Cancel
                        </Button>
                        <Button type="submit" disabled={isSubmitting}>
                            {isSubmitting ? 'Updating...' : 'Update Meeting'}
                        </Button>
                    </div>
                </form>
            </DialogContent>
        </Dialog>
    );
}
