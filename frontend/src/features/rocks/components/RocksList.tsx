import type { GetRocksResponse, Rock } from '../types/rock';
import { RocksGroup } from './RocksGroup';
import { useArchiveRock } from '../hooks/useArchiveRock';
import { RockListItem } from './RockListItem';

interface RocksListProps {
    data?: GetRocksResponse;
    onUpdate: (rock: Rock) => void;
    onDelete?: (rock: Rock) => void;
}

export function RocksList({ data, onUpdate, onDelete }: RocksListProps) {
    const { handleArchive, isPending: isArchiving } = useArchiveRock();

    if (!data) {
        return null;
    }
    const { companyRocks, departmentRocks, individualRocks } = data;

    const renderRock = (rock: Rock) => (
        <RockListItem key={rock.id} rock={rock} onArchive={handleArchive} onUpdate={onUpdate} onDelete={onDelete} isArchiving={isArchiving} />
    );

    const hasAnyRocks = companyRocks?.length || departmentRocks.length || individualRocks.length;

    if (!hasAnyRocks) {
        return <div className="py-10 text-center text-muted-foreground">No rocks.</div>;
    }

    return (
        <div className="space-y-8">
            {companyRocks && <RocksGroup title="Company Rocks" rocks={companyRocks} renderRock={renderRock} />}
            <RocksGroup title="Department Rocks" rocks={departmentRocks} renderRock={renderRock} />
            <RocksGroup title="Individual Rocks" rocks={individualRocks} renderRock={renderRock} />
        </div>
    );
}
