import type { UseFormReturn, Path, FieldValues } from 'react-hook-form';
import type { NormalizedApiError } from '@/utils/apiErrorNormalizer';

export const useFormError = <T extends FieldValues>(form: UseFormReturn<T>) => {
    return (normalizedError: NormalizedApiError): boolean => {
        const isMappable = ['validation', 'conflict'].includes(normalizedError.type);

        if (!isMappable || !normalizedError.fieldErrors) {
            return false;
        }

        const formFields = form.getValues();
        let hasMappedAnyField = false;

        Object.entries(normalizedError.fieldErrors).forEach(([field, message]) => {
            if (field in formFields) {
                form.setError(field as Path<T>, {
                    type: 'server',
                    message,
                });

                if (!hasMappedAnyField) {
                    form.setFocus(field as Path<T>);
                }
                hasMappedAnyField = true;
            }
        });

        return hasMappedAnyField;
    };
};
