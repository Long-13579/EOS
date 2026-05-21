import * as React from 'react';
import { useMatches, Link } from '@tanstack/react-router';
import { SidebarProvider, SidebarInset, SidebarTrigger } from '@/components/ui/sidebar';
import { AppSidebar } from '@/components/shared/AppSidebar';
import { Separator } from '@/components/ui/separator';

import { Breadcrumb, BreadcrumbItem, BreadcrumbLink, BreadcrumbList, BreadcrumbPage, BreadcrumbSeparator } from '@/components/ui/breadcrumb';
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuLabel, DropdownMenuTrigger } from '@/components/ui/dropdown-menu';
import { Button } from '@/components/ui/button';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import { SunMoon } from 'lucide-react';
import { toggleTheme } from '@/utils/theme';
import { useLogout } from '@/features/auth';
import { Spinner } from '@/components/ui/spinner';
import { getUserFullName, getUserInitials } from '@/utils/user';
import { useUserStore } from '@/stores/useUserStore';

export const DashboardLayout = ({ children }: { children: React.ReactNode }) => {
    const matches = useMatches();

    const user = useUserStore((state) => state.user);
    const fullName = user ? getUserFullName({ firstName: user.firstName, lastName: user.lastName }) : '';
    const userInitials = user ? getUserInitials({ firstName: user.firstName, lastName: user.lastName }) : 'U';
    const email = user?.email || '';

    const crumbs = matches
        .filter((match) => match.staticData?.breadcrumb)
        .map((match) => ({
            label: match.staticData.breadcrumb,
            path: match.pathname,
        }));

    const { logout, isLoggingOut } = useLogout();

    if (isLoggingOut) {
        return (
            <div className="fixed inset-0 z-50 flex items-center justify-center bg-background/80 backdrop-blur-sm">
                <div className="flex flex-col items-center gap-2">
                    <Spinner className="h-8 w-8" />
                    <p className="text-sm text-muted-foreground">Logging out...</p>
                </div>
            </div>
        );
    }

    return (
        <SidebarProvider>
            <AppSidebar />
            <SidebarInset className="bg-background min-w-0 w-full overflow-hidden">
                <header className="sticky top-0 z-10 flex h-16 shrink-0 items-center gap-2 border-b bg-background/80 px-4 shadow-sm backdrop-blur-md transition-[width,height] ease-linear group-has-data-[collapsible=icon]/sidebar-wrapper:h-12">
                    <div className="flex items-center gap-2 px-4">
                        <SidebarTrigger className="-ml-1" />
                        <Separator orientation="vertical" className="mr-2 h-4" />
                        <Breadcrumb>
                            <BreadcrumbList>
                                {crumbs.map((c, index) => (
                                    <React.Fragment key={c.path}>
                                        <BreadcrumbItem>
                                            {index < crumbs.length - 1 ? (
                                                <BreadcrumbLink asChild className="hover:text-primary text-[16px]">
                                                    <Link to={c.path}>{c.label}</Link>
                                                </BreadcrumbLink>
                                            ) : (
                                                <BreadcrumbPage className="text-[16px]">{c.label}</BreadcrumbPage>
                                            )}
                                        </BreadcrumbItem>

                                        {index < crumbs.length - 1 && <BreadcrumbSeparator />}
                                    </React.Fragment>
                                ))}
                            </BreadcrumbList>
                        </Breadcrumb>
                    </div>

                    <div className="ml-auto flex items-center gap-2">
                        <Button variant="ghost" size="icon" className="h-9 w-9 text-muted-foreground hover:text-primary" onClick={toggleTheme}>
                            <SunMoon className="h-5 w-5" />
                            <span className="sr-only">Toggle theme</span>
                        </Button>

                        <DropdownMenu>
                            <DropdownMenuTrigger asChild>
                                <Button variant="ghost" className="relative h-9 w-9 rounded-full ring-primary/20 hover:ring-2">
                                    <Avatar className="h-9 w-9">
                                        <AvatarImage src="/avatars/01.png" alt={fullName.trim() ? `${fullName} avatar` : 'User avatar'} />
                                        <AvatarFallback className="bg-primary/10 text-primary font-medium">{userInitials}</AvatarFallback>
                                    </Avatar>
                                </Button>
                            </DropdownMenuTrigger>
                            <DropdownMenuContent className="w-56" align="end" forceMount>
                                <DropdownMenuLabel className="font-normal">
                                    <div className="flex flex-col space-y-1">
                                        <p className="text-sm font-medium leading-none">{fullName}</p>
                                        <p className="text-xs leading-none text-muted-foreground">{email}</p>
                                    </div>
                                </DropdownMenuLabel>
                                <DropdownMenuItem
                                    onClick={logout}
                                    disabled={isLoggingOut}
                                    className="cursor-pointer text-destructive focus:text-destructive"
                                >
                                    Log out
                                </DropdownMenuItem>
                            </DropdownMenuContent>
                        </DropdownMenu>
                    </div>
                </header>

                <main className="flex flex-1 flex-col gap-4 p-4 min-w-0 overflow-hidden">
                    <div className="flex-1 min-w-0 w-full max-w-full overflow-hidden rounded-xl border bg-card text-card-foreground shadow-sm p-9 pt-8">
                        {children}
                    </div>
                </main>
            </SidebarInset>
        </SidebarProvider>
    );
};
