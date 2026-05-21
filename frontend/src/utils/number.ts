export const normalizeDecimal = (value?: string) => {
    if (!value) {
        return '';
    }
    let val = value.trim();

    if (val.includes('.') && val.includes(',')) {
        val = val.replace(/\./g, '').replace(',', '.');
    } else if (val.includes(',')) {
        val = val.replace(',', '.');
    }

    return val;
};

export const parseNumber = (value?: string): number | null => {
    if (!value) {
        return null;
    }

    const normalized = normalizeDecimal(value);
    const num = Number(normalized);

    return isNaN(num) ? null : num;
};

export const formatNumber = (num: number, locale = 'en-US') => {
    return new Intl.NumberFormat(locale, {
        maximumFractionDigits: 2,
    }).format(num);
};
