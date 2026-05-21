import type { Rock } from '../types/rock';
import { RockListItem } from './RockListItem';

interface MyRockListProps {
    data?: Rock[];
    onUpdate: (rock: Rock) => void;
}

export function MyRocksList({ data, onUpdate }: MyRockListProps) {
    if (!data || data.length === 0) {
        return null;
    }

    return (
        <div className="space-y-4">
            {data.map((rock) => (
                <RockListItem key={rock.id} rock={rock} onUpdate={onUpdate} showTeam={true} />
            ))}
        </div>
    );
}
