import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Field } from '@/components/ui/field';
import { GoogleButton } from './GoogleButton';

export function LoginForm() {
    return (
        <div>
            <Card>
                <CardHeader className="text-center">
                    <CardTitle className="text-xl">Welcome back</CardTitle>
                    <CardDescription>Login with your Google account</CardDescription>
                </CardHeader>
                <CardContent>
                    <Field>
                        <GoogleButton />
                    </Field>
                </CardContent>
            </Card>
        </div>
    );
}
