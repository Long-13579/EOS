import type { ReactNode } from 'react';
import { PageHeader } from '@/components/shared/PageHeader';
import { GlobalTeamSelector } from '@/components/shared/GlobalTeamSelector';

interface PageHeaderGroupProps {
    title: string;
    description?: string;
    children?: ReactNode;
}

export function PageHeaderGroup({ title, description, children }: PageHeaderGroupProps) {
    return (
        <PageHeader title={title} description={description}>
            <div className="flex items-center gap-4">
                <GlobalTeamSelector />
                {children}
            </div>
        </PageHeader>
    );
}
