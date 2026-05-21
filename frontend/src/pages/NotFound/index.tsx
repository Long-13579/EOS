import { Link } from '@tanstack/react-router';
import { Button } from '@/components/ui/button';
import { LayoutDashboard } from 'lucide-react';

export function NotFoundPage() {
    return (
        <div className="flex min-h-svh items-center justify-center p-4 bg-background">
            <div className="w-full max-w-md text-center">
                <h1 className="text-4xl font-extrabold tracking-tight sm:text-5xl mb-3">404</h1>
                <h2 className="text-xl font-semibold mb-4">Page not found</h2>
                <p className="text-muted-foreground leading-relaxed mb-8">We couldn't find the page you're looking for.</p>

                <Button asChild size="lg" className="w-full sm:w-auto gap-2 shadow-md shadow-primary/20">
                    <Link to="/">
                        <LayoutDashboard className="size-4" />
                        Return to Dashboard
                    </Link>
                </Button>
            </div>
        </div>
    );
}
