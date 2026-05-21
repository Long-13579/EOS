import type { Rock } from '../types/rock';
import type { ReactNode } from 'react';

interface RocksGroupProps {
    title: string;
    rocks: Rock[];
    renderRock: (rock: Rock) => ReactNode;
}

export function RocksGroup({ title, rocks, renderRock }: RocksGroupProps) {
    if (rocks.length === 0) {
        return null;
    }

    return (
        <div className="space-y-4">
            <h2 className="text-lg font-semibold">{title}</h2>

            <div className="flex flex-col space-y-3">{rocks.map((rock) => renderRock(rock))}</div>
        </div>
    );
}
