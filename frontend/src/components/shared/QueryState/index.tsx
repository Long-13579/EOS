import { Loader2 } from 'lucide-react';

type QueryStateProps = {
    isPending: boolean;
    isError: boolean;
    isEmpty?: boolean;
    errorMessage?: string;
    emptyMessage?: string;
    loading?: React.ReactNode;
    error?: React.ReactNode;
    empty?: React.ReactNode;
    children: React.ReactNode;
};

export function QueryState({ isPending, isError, isEmpty, errorMessage, emptyMessage, loading, error, empty, children }: QueryStateProps) {
    if (isPending) {
        return (
            loading ?? (
                <div className="flex h-[400px] items-center justify-center">
                    <Loader2 className="h-8 w-8 animate-spin text-primary" />
                </div>
            )
        );
    }

    if (isError) {
        return error ?? <div className="py-10 text-center text-destructive">{errorMessage ?? 'Something went wrong.'}</div>;
    }

    if (isEmpty) {
        return empty ?? <div className="py-10 text-center text-muted-foreground">{emptyMessage ?? 'No data found.'}</div>;
    }

    return <>{children}</>;
}
