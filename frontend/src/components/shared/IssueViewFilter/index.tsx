import { RadioGroup, RadioGroupItem } from '@/components/ui/radio-group';
import { ISSUE_VIEW, type IssueView } from '@/features/issues';

const VIEW_OPTIONS: { label: string; value: IssueView }[] = [
    { label: 'Issues', value: ISSUE_VIEW.ISSUES },
    { label: 'Long Term', value: ISSUE_VIEW.LONG_TERM },
    { label: 'Archived', value: ISSUE_VIEW.ARCHIVED },
];

type IssueViewFilterProps = {
    value: IssueView;
    onChange: (value: IssueView) => void;
    disabled?: boolean;
};

export function IssueViewFilter({ value, onChange, disabled }: IssueViewFilterProps) {
    return (
        <div className="flex justify-end gap-6 rounded-lg border bg-muted/30 p-4">
            <span id="issue-view-filter-label" className="text-sm font-medium">
                View:
            </span>

            <RadioGroup
                aria-labelledby="issue-view-filter-label"
                value={value}
                onValueChange={(v) => onChange(v as IssueView)}
                className="flex items-center gap-6"
            >
                {VIEW_OPTIONS.map((option) => (
                    <label
                        key={option.value}
                        className={`flex items-center gap-2 text-sm font-medium ${
                            disabled ? 'cursor-not-allowed text-muted-foreground' : 'cursor-pointer'
                        }`}
                    >
                        <RadioGroupItem value={option.value} disabled={disabled} />
                        {option.label}
                    </label>
                ))}
            </RadioGroup>
        </div>
    );
}
