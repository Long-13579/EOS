import { useState, useRef, type KeyboardEvent } from 'react';
import { useForm, useWatch } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Input } from '@/components/ui/input';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Field, FieldError } from '@/components/ui/field';
import type { Metric } from '../types/metric';
import { getMetricValueSchema } from '../schemas/updateMetricValueSchema';
import { formatMetricValue } from '../utils/metricFormat';
import { normalizeDecimal } from '@/utils/number';
import { Pencil } from 'lucide-react';
import { cn } from '@/lib/utils';
import { toast } from 'sonner';
import { useFormError } from '@/hooks/useFormError';
import { normalizeApiError } from '@/utils/apiErrorNormalizer';
import { z } from 'zod';
import { Button } from '@/components/ui/button';
import { Controller } from 'react-hook-form';

const METRIC_CONFIG = {
    NUMBER: { type: 'input' },
    PERCENTAGE: { type: 'input' },
    CURRENCY: { type: 'input' },
    TIME: { type: 'input' },
    YES_NO: {
        type: 'select',
        options: [
            { value: 'YES', label: 'Yes' },
            { value: 'NO', label: 'No' },
        ],
    },
    RYG_STATUS: {
        type: 'select',
        options: [
            { value: 'RED', label: 'Red' },
            { value: 'YELLOW', label: 'Yellow' },
            { value: 'GREEN', label: 'Green' },
        ],
    },
} as const;

interface MetricValueCellProps {
    metric: Metric;
    onUpdate: (metricId: string, value: string | null) => Promise<Metric>;
    isUpdating?: boolean;
    isEditable?: boolean;
}

export function MetricValueCell({ metric, onUpdate, isUpdating, isEditable }: MetricValueCellProps) {
    const [isEditing, setIsEditing] = useState(false);

    const ignoreBlurRef = useRef(false);

    const schema = getMetricValueSchema(metric.unit);
    type FormValues = z.infer<typeof schema>;

    const form = useForm<FormValues>({
        resolver: zodResolver(schema),
        defaultValues: {
            value: normalizeDecimal(metric.currentValue?.value),
        },
    });

    const {
        handleSubmit,
        setValue,
        reset,
        formState: { errors, isSubmitting },
    } = form;

    const handleFormError = useFormError(form);
    const value = useWatch({ control: form.control, name: 'value' });
    const config = METRIC_CONFIG[metric.unit];

    const handleStartEdit = () => {
        const val = normalizeDecimal(metric.currentValue?.value);

        setIsEditing(true);

        setTimeout(() => {
            setValue('value', val, { shouldValidate: false });
        }, 0);
    };

    const handleCancel = () => {
        setIsEditing(false);
        reset({ value: normalizeDecimal(metric.currentValue?.value) });
    };

    const onSubmit = async (data: FormValues) => {
        try {
            const parsedNew = normalizeDecimal(data.value as string);
            const parsedOld = metric.currentValue?.value ? metric.currentValue.value.trim().toUpperCase() : null;

            if (parsedNew === parsedOld) {
                setIsEditing(false);
                return;
            }

            if (!metric.currentValue?.id) {
                toast.error('No value exists for this period');
                setIsEditing(false);
                return;
            }

            await onUpdate(metric.id, parsedNew);
        } catch (error) {
            const normalized = normalizeApiError(error);
            const isHandledByForm = handleFormError(normalized);

            if (!isHandledByForm) {
                toast.error(normalized.message);
            }
        } finally {
            setIsEditing(false);
            ignoreBlurRef.current = false;
        }
    };

    const handleKeyDown = (e: KeyboardEvent<HTMLInputElement>) => {
        if (e.key === 'Enter') {
            ignoreBlurRef.current = true;
        }

        if (e.key === 'Escape') {
            e.preventDefault();
            ignoreBlurRef.current = true;
            handleCancel();
        }
    };

    if (!isEditing) {
        const formattedValue = formatMetricValue(metric.currentValue?.value || null, metric.unit);
        return (
            <Button
                type="button"
                variant="ghost"
                size="sm"
                onClick={handleStartEdit}
                disabled={isUpdating || !isEditable}
                className={cn(
                    'group flex items-center justify-end gap-2 h-8 px-2 w-full',
                    isUpdating || !isEditable ? 'opacity-50 cursor-not-allowed' : 'hover:bg-accent/30',
                )}
                aria-label="Edit metric value"
            >
                <span className="font-medium truncate" title={formattedValue}>
                    {formattedValue}
                </span>
                <Pencil className="h-3 w-3 opacity-30 text-accent-foreground transition-opacity" />
            </Button>
        );
    }

    if (config.type === 'select') {
        return (
            <form onSubmit={handleSubmit(onSubmit)}>
                <Field data-invalid={!!errors.value}>
                    <Select
                        value={value ?? ''}
                        onValueChange={(val) => {
                            setValue('value', val, { shouldValidate: true });
                            handleSubmit(onSubmit)();
                        }}
                        onOpenChange={(open) => {
                            if (!open) {
                                setTimeout(() => {
                                    setIsEditing(false);
                                }, 150);
                            }
                        }}
                        defaultOpen
                        disabled={isSubmitting || isUpdating || !isEditable}
                    >
                        <SelectTrigger className="h-8 w-full bg-accent/50">
                            <SelectValue placeholder="-" />
                        </SelectTrigger>

                        <SelectContent position="popper" className="min-w-[--radix-select-trigger-width]" align="end">
                            {config.options.map((opt) => (
                                <SelectItem key={opt.value} value={opt.value}>
                                    {opt.label}
                                </SelectItem>
                            ))}
                        </SelectContent>
                    </Select>

                    {errors.value && <FieldError errors={[errors.value]} />}
                </Field>
            </form>
        );
    }

    return (
        <form onSubmit={handleSubmit(onSubmit)}>
            <Field data-invalid={!!errors.value}>
                <Controller
                    control={form.control}
                    name="value"
                    defaultValue={normalizeDecimal(metric.currentValue?.value)}
                    render={({ field }) => (
                        <Input
                            {...field}
                            value={field.value ?? ''}
                            onChange={(e) => {
                                const raw = e.target.value;

                                if (['NUMBER', 'PERCENTAGE', 'CURRENCY'].includes(metric.unit)) {
                                    field.onChange(normalizeDecimal(raw));
                                } else {
                                    field.onChange(raw);
                                }
                            }}
                            onKeyDown={(e) => {
                                handleKeyDown(e);

                                if (e.key === 'Enter') {
                                    e.preventDefault();
                                    handleSubmit(onSubmit)();
                                }
                            }}
                            onBlur={() => {
                                if (ignoreBlurRef.current) {
                                    ignoreBlurRef.current = false;
                                    return;
                                }
                                handleSubmit(onSubmit)();
                            }}
                            className="h-8 w-full text-right bg-accent/50"
                            disabled={isSubmitting || isUpdating || !isEditable}
                            autoFocus
                        />
                    )}
                />

                {errors.value && <FieldError errors={[errors.value]} />}
            </Field>
        </form>
    );
}
