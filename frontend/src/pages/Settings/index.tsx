import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { TooltipProvider } from '@/components/ui/tooltip';
import { UsersTab, TeamsTab } from '@/features/settings';
import { UserCog, Users } from 'lucide-react';
import { PageHeader } from '@/components/shared/PageHeader';

export function Settings() {
    return (
        <TooltipProvider delayDuration={200}>
            <div className="flex-1 space-y-4">
                <PageHeader title="Settings" description="Manage user and team." />

                <Tabs defaultValue="users" className="space-y-4">
                    <TabsList>
                        <TabsTrigger value="users" className="gap-2">
                            <UserCog className="h-4 w-4" /> User Management
                        </TabsTrigger>

                        <TabsTrigger value="teams" className="gap-2">
                            <Users className="h-4 w-4" /> Team Management
                        </TabsTrigger>
                    </TabsList>

                    <TabsContent value="users" className="space-y-4">
                        <UsersTab />
                    </TabsContent>

                    <TabsContent value="teams" className="space-y-4">
                        <TeamsTab />
                    </TabsContent>
                </Tabs>
            </div>
        </TooltipProvider>
    );
}
