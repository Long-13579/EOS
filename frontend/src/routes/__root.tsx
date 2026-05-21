import { Outlet, createRootRoute, useSearch } from '@tanstack/react-router';
import { ReactQueryDevtools } from '@tanstack/react-query-devtools';
import { NotFoundPage } from '@/pages/NotFound';

export const Route = createRootRoute({
    component: RootComponent,
    notFoundComponent: NotFoundPage,
});

function RootComponent() {
    const { debug } = useSearch({ strict: false });

    return (
        <>
            <Outlet />
            {debug && <ReactQueryDevtools />}
        </>
    );
}
