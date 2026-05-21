import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, LabelList, Rectangle } from 'recharts';
import type { ChartData } from '../mappers/mapTrendMetric';
import type { MetricUnit } from '../types/metric';
import { formatMetricDisplay, formatShortMetric } from '../utils/metricTrend';
import { getTrendColor } from '../utils/metricColor';

type TrendMetricChartProps = {
    data: ChartData[];
    unit: MetricUnit;
};

interface CustomTooltipProps {
    active?: boolean;
    payload?: { payload: ChartData }[];
    label?: string;
    unit: MetricUnit;
}

const CustomTooltip = ({ active, payload, label, unit }: CustomTooltipProps) => {
    if (!active || !payload?.length) {
        return null;
    }

    const data = payload[0].payload as ChartData;
    const displayLabel = unit === 'RYG_STATUS' ? 'Status' : 'Value';
    const displayValue = formatMetricDisplay(data, unit);

    return (
        <div className="bg-popover text-popover-foreground border border-border rounded-lg p-3 text-[13px] shadow-sm min-w-[120px]">
            <div className="font-semibold text-foreground mb-1.5">{label}</div>

            <div className="flex justify-between gap-4">
                <span className="text-muted-foreground">{displayLabel}:</span>
                <span className="font-medium">{displayValue}</span>
            </div>
        </div>
    );
};

export function TrendMetricChart({ data, unit }: TrendMetricChartProps) {
    const processedData = data.map((item) => ({
        ...item,
        safeValue: item.value == null ? 0 : item.value,
        realValue: item.value,
    }));

    return (
        <ResponsiveContainer width="100%" height={220}>
            <BarChart data={processedData} margin={{ top: 25, right: 0, left: 0, bottom: 0 }} barCategoryGap="4%">
                <XAxis dataKey="weekLabel" axisLine={false} tickLine={false} tick={{ fontSize: 12, fill: 'var(--muted-foreground)' }} dy={10} />

                <YAxis hide domain={[0, 'auto']} />

                <Tooltip cursor={{ fill: 'var(--muted)', opacity: 0.1 }} content={<CustomTooltip unit={unit} />} />

                <Bar
                    dataKey="safeValue"
                    shape={(props) => {
                        const color = getTrendColor(props.payload, unit);
                        return <Rectangle {...props} fill={color} radius={[6, 6, 0, 0]} />;
                    }}
                >
                    <LabelList
                        content={(props) => {
                            const { x, y, width, index } = props;
                            const item = processedData[Number(index)];
                            if (!item) {
                                return null;
                            }

                            let text = formatShortMetric(item, unit);

                            const numX = Number(x) || 0;
                            const numY = Number(y) || 0;
                            const numWidth = Number(width) || 0;

                            const maxChars = Math.floor(numWidth / 7);

                            if (text.length > maxChars) {
                                text = text.slice(0, maxChars) + '…';
                            }

                            if (numWidth < 30) {
                                return null;
                            }

                            return (
                                <text
                                    x={numX + numWidth / 2}
                                    y={item.value == null ? numY - 12 : numY - 8}
                                    fill={text === 'N/A' ? 'var(--muted-foreground)' : 'var(--foreground)'}
                                    textAnchor="middle"
                                    fontSize={12}
                                    fontWeight={500}
                                >
                                    {text}
                                </text>
                            );
                        }}
                    />
                </Bar>
            </BarChart>
        </ResponsiveContainer>
    );
}
