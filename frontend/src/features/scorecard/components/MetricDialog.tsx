import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { MetricForm } from './MetricForm';
import type { Metric, MetricFormValues } from '../types/metric';
import { mapOperatorToApi } from '../mappers/mapMetric';

interface MetricDialogProps {
    isOpen: boolean;
    onOpenChange: (open: boolean) => void;
    onSubmit: (data: MetricFormValues) => void | Promise<void>;
    editingMetric?: Metric;
}

export function MetricDialog({ isOpen, onOpenChange, editingMetric, onSubmit }: Readonly<MetricDialogProps>) {
    const mode = editingMetric ? 'edit' : 'create';

    const initialData: MetricFormValues | undefined = editingMetric
        ? {
              name: editingMetric.name,
              goal: editingMetric.goal,
              unit: editingMetric.unit,
              operator: mapOperatorToApi(editingMetric.operator),
              ownerId: editingMetric.owner.id,
          }
        : undefined;

    return (
        <Dialog open={isOpen} onOpenChange={onOpenChange}>
            <DialogContent>
                <DialogHeader>
                    <DialogTitle>{mode === 'edit' ? 'Edit Metric' : 'Add New Metric'}</DialogTitle>
                </DialogHeader>

                <MetricForm
                    key={mode}
                    onSubmit={onSubmit}
                    onCancel={() => onOpenChange(false)}
                    mode={mode}
                    initialData={initialData}
                    teamId={editingMetric?.team.id}
                />
            </DialogContent>
        </Dialog>
    );
}
