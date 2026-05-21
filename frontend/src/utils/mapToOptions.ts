/**
 * Converts an array of objects into a format compatible with MultiSelect components.
 */
export const mapToOptions = <T>(items: T[], labelKey: keyof T, valueKey: keyof T) => {
    return items.map((item) => ({
        label: String(item[labelKey]),
        value: String(item[valueKey]),
    }));
};
