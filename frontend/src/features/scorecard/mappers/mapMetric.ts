import type { Metric, MetricOperator, MetricResponseItem } from '../types/metric';

export const mapOperatorFromApi = (operator: string): Metric['operator'] => {
    switch (operator) {
        case 'LESS_THAN':
            return '<';
        case 'LESS_THAN_OR_EQUAL_TO':
            return '≤';
        case 'GREATER_THAN':
            return '>';
        case 'GREATER_THAN_OR_EQUAL_TO':
            return '≥';
        case 'EQUAL_TO':
            return '=';
        default:
            return null;
    }
};

export const mapOperatorToApi = (operator: Metric['operator']): MetricOperator | undefined => {
    switch (operator) {
        case '<':
            return 'LESS_THAN';
        case '≤':
            return 'LESS_THAN_OR_EQUAL_TO';
        case '>':
            return 'GREATER_THAN';
        case '≥':
            return 'GREATER_THAN_OR_EQUAL_TO';
        case '=':
            return 'EQUAL_TO';
        default:
            return undefined;
    }
};

export const mapMetric = (item: MetricResponseItem): Metric => ({
    id: item.id,
    name: item.name,
    operator: (item.operator ? mapOperatorFromApi(item.operator) : null) as Metric['operator'],
    goal: item.goal,
    currentValue: item.currentValue,
    pastValue: item.lastValue ?? null,
    team: item.team,
    owner: item.owner,
    unit: item.unit,
    isGoalMet: item.currentValue?.isGoalMet,
});
