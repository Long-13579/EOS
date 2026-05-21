import type { GetRocksResponse, Rock } from '../types/rock';
import { RocksGroup } from './RocksGroup';
import { useArchiveRock } from '../hooks/useArchiveRock';
import { RockListItem } from './RockListItem';

interface RocksListProps {
    data?: GetRocksResponse;
    onUpdate: (rock: Rock) => void;
}

export function RocksList({ data, onUpdate }: RocksListProps) {
    const { handleArchive, isPending: isArchiving } = useArchiveRock();

    if (!data) {
        return null;
    }
    const { companyRocks, departmentRocks, individualRocks } = data;

    const renderRock = (rock: Rock) => (
        <RockListItem key={rock.id} rock={rock} onArchive={handleArchive} onUpdate={onUpdate} isArchiving={isArchiving} />
    );

    return (
        <div className="space-y-8">
            {companyRocks && <RocksGroup title="Company Rocks" rocks={companyRocks} renderRock={renderRock} />}
            <RocksGroup title="Department Rocks" rocks={departmentRocks} renderRock={renderRock} />
            <RocksGroup title="Individual Rocks" rocks={individualRocks} renderRock={renderRock} />
        </div>
    );
}
