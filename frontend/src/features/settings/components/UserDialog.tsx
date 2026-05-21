import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { UserForm } from '@/features/settings/components/UserForm';

import { useCreateUser } from '../hooks/useCreateUser';

import { toast } from 'sonner';
import { SUCCESS_MESSAGES } from '@/constants/messages';
import type { CreateUser, UpdateUser, User } from '@/types/user';
import { useUpdateUser } from '../hooks/useUpdateUser';

type UserDialogProps = {
    user?: User;
    onUserSaved?: () => void;
    isOpen: boolean;
    onClose: () => void;
};

export function UserDialog({ user, onUserSaved, isOpen, onClose }: UserDialogProps) {
    const { isCreatingUser, createUser } = useCreateUser();
    const { isUpdatingUser, updateUser } = useUpdateUser();

    const isEditMode = !!user;
    const isSubmitting = isCreatingUser || isUpdatingUser;

    const config = {
        title: isEditMode ? 'Edit User' : 'Add New User',
        description: isEditMode ? "Update the user's information below." : 'Fill out the form below to create a new user account.',
    };

    const handleClose = () => {
        if (isSubmitting) {
            return;
        }
        onClose();
    };

    const handleOpenChange = (open: boolean) => {
        if (!open) {
            handleClose();
        }
    };

    const handleUpdateUser = async (userId: string, data: UpdateUser) => {
        await updateUser(
            { id: userId, payload: data },
            {
                onSuccess: () => {
                    toast.success(SUCCESS_MESSAGES.USER.UPDATED);
                    onUserSaved?.();
                },
            },
        );
    };

    const handleCreateUser = async (data: CreateUser) => {
        await createUser(data, {
            onSuccess: () => {
                toast.success(SUCCESS_MESSAGES.USER.CREATED);
                onUserSaved?.();
            },
        });
    };

    return (
        <Dialog open={isOpen} onOpenChange={handleOpenChange}>
            <DialogContent showCloseButton={!isSubmitting}>
                <DialogHeader>
                    <DialogTitle>{config.title}</DialogTitle>
                </DialogHeader>
                <DialogDescription className="mb-4">{config.description}</DialogDescription>
                {isEditMode && user ? (
                    <UserForm
                        onCancel={handleClose}
                        onSubmit={(data) => handleUpdateUser(user.id, data)}
                        mode="update"
                        initialValues={{
                            firstName: user.firstName,
                            lastName: user.lastName,
                            email: user.email,
                            role: user.role,
                            teamIds: user.teams?.map((team) => team.id) || [],
                        }}
                    />
                ) : (
                    <UserForm onCancel={handleClose} onSubmit={handleCreateUser} mode="create" />
                )}
            </DialogContent>
        </Dialog>
    );
}
