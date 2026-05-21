import { Loader2 } from 'lucide-react';
import { TableCell, TableRow } from '@/components/ui/table';
import { QueryState } from '@/components/shared/QueryState';

type TableQueryStateProps = {
    isPending: boolean;
    isError: boolean;
    isEmpty?: boolean;
    colSpan: number;
    errorMessage?: string;
    emptyMessage?: string;
    children: React.ReactNode;
};

export function TableQueryState({ isPending, isError, isEmpty, colSpan, errorMessage, emptyMessage, children }: TableQueryStateProps) {
    return (
        <QueryState
            isPending={isPending}
            isError={isError}
            isEmpty={isEmpty}
            errorMessage={errorMessage}
            emptyMessage={emptyMessage}
            loading={
                <TableRow>
                    <TableCell colSpan={colSpan} className="h-24 text-center">
                        <Loader2 className="mx-auto h-6 w-6 animate-spin text-primary" />
                    </TableCell>
                </TableRow>
            }
            error={
                <TableRow>
                    <TableCell colSpan={colSpan} className="h-24 text-center text-destructive">
                        {errorMessage ?? 'Something went wrong.'}
                    </TableCell>
                </TableRow>
            }
            empty={
                <TableRow>
                    <TableCell colSpan={colSpan} className="h-24 text-center text-muted-foreground">
                        {emptyMessage ?? 'No data found.'}
                    </TableCell>
                </TableRow>
            }
        >
            {children}
        </QueryState>
    );
}
