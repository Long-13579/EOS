import type * as React from 'react';
import { Link, useMatchRoute, useRouterState } from '@tanstack/react-router';
import { Settings, Command } from 'lucide-react';

import {
    Sidebar,
    SidebarContent,
    SidebarGroup,
    SidebarGroupContent,
    SidebarHeader,
    SidebarMenu,
    SidebarMenuButton,
    SidebarMenuItem,
    SidebarRail,
    SidebarSeparator,
} from '@/components/ui/sidebar';
import { navItems } from './navItems';
import { useUserStore } from '@/stores/useUserStore';

export function AppSidebar({ ...props }: React.ComponentProps<typeof Sidebar>) {
    const pathname = useRouterState().location.pathname;
    const matchRoute = useMatchRoute();
    const user = useUserStore((state) => state.user);
    const isActive = (to: string) => !!matchRoute({ to, fuzzy: true });

    return (
        <Sidebar {...props} className="border-r border-sidebar-border bg-sidebar text-sidebar-foreground">
            <SidebarHeader>
                <SidebarMenu>
                    <SidebarMenuButton asChild tooltip="EOS Flow Hub">
                        <Link to={'/'} className="h-full">
                            <div className="flex aspect-square size-9 items-center justify-center rounded-lg bg-primary text-primary-foreground">
                                <Command className="size-4.5" />
                            </div>
                            <div className="grid flex-1 text-left text-sm leading-tight">
                                <span className="truncate font-bold text-[20px]">EOS Flow Hub</span>
                                <span className="truncate text-xs text-muted-foreground">Enterprise</span>
                            </div>
                        </Link>
                    </SidebarMenuButton>
                </SidebarMenu>
            </SidebarHeader>

            <SidebarContent>
                <SidebarGroup>
                    <SidebarGroupContent>
                        <SidebarMenu>
                            {navItems.map((item) => (
                                <SidebarMenuItem key={item.title}>
                                    <SidebarMenuButton
                                        asChild
                                        isActive={isActive(item.to)}
                                        tooltip={item.title}
                                        className="hover:bg-sidebar-accent/50 data-[active=true]:bg-primary/10 data-[active=true]:text-primary data-[active=true]:font-medium"
                                    >
                                        <Link to={item.to}>
                                            <item.icon className="size-4" />
                                            <span>{item.title}</span>
                                        </Link>
                                    </SidebarMenuButton>
                                </SidebarMenuItem>
                            ))}
                        </SidebarMenu>
                    </SidebarGroupContent>
                </SidebarGroup>

                {user?.role === 'ADMIN' && (
                    <SidebarGroup className="mt-auto">
                        <SidebarGroupContent>
                            <SidebarMenu>
                                <SidebarMenuItem>
                                    <SidebarMenuButton
                                        asChild
                                        isActive={pathname.startsWith('/settings')}
                                        tooltip="Settings"
                                        className="hover:bg-sidebar-accent/50 data-[active=true]:bg-primary/10 data-[active=true]:text-primary data-[active=true]:font-medium"
                                    >
                                        <Link to={'/settings'}>
                                            <Settings className="size-4" />
                                            <span>Settings</span>
                                        </Link>
                                    </SidebarMenuButton>
                                </SidebarMenuItem>
                            </SidebarMenu>
                        </SidebarGroupContent>
                    </SidebarGroup>
                )}
            </SidebarContent>

            <SidebarSeparator />
            <SidebarRail />
        </Sidebar>
    );
}
