import { PageHeader } from '@/components/shared/PageHeader';
import { MyRocksWidget } from '@/features/rocks';
import { MyScorecardsWidget } from '@/features/scorecard';
import { MyTodosWidget } from '@/features/todos';

export function Dashboard() {
    return (
        <div className="flex flex-col gap-6">
            <PageHeader title="Dashboard" description="See what needs your attention." />
            <MyRocksWidget />
            <MyScorecardsWidget />
            <MyTodosWidget />
        </div>
    );
}
