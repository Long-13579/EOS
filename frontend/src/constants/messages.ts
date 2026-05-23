export const ERROR_MESSAGES = {
    COMMON: {
        UNKNOWN: 'Unknown error occurred.',
        GENERIC_UNKNOWN: 'Unknown error',
        NETWORK: 'Network error. Please check your connection.',
        SERVER: 'Server error.',
        CONFLICT: 'Data already exists or is in conflict.',
        VALIDATION: 'Data is invalid.',
        UNAUTHORIZED: 'Session has expired.',
        FORBIDDEN: 'You do not have permission to perform this action.',
        NOT_FOUND: 'Data not found.',
    },

    TEAM: {
        LOAD_FAILED: 'Failed to load teams. Please try again.',
        NOT_FOUND: 'No teams found.',
        CONFLICT: 'Team already exists.',
        VALIDATION: 'Team data is invalid.',
    },

    USER: {
        LOAD_FAILED: 'Failed to load users. Please try again.',
        NOT_FOUND: 'No users found.',
        CONFLICT: 'User already exists.',
        VALIDATION: 'User data is invalid.',
    },
    TODO: {
        LOAD_FAILED: 'Failed to load To-dos. Please try again.',
        NOT_FOUND: 'No To-dos found.',
        ARCHIVED_NOT_FOUND: 'No archived To-dos found.',
        VALIDATION: 'To-do data is invalid.',
        DELETE_FAILED: 'Failed to delete To-do. Please try again.',
        ARCHIVE_FAILED: 'Failed to archive To-do. Please try again.',
        UNARCHIVE_FAILED: 'Failed to unarchive To-do. Please try again.',
    },
    ISSUE: {
        LOAD_FAILED: 'Failed to load issues. Please try again.',
        NOT_FOUND: 'No issues found.',
        ARCHIVED_NOT_FOUND: 'No archived issues found.',
        LONG_TERM_NOT_FOUND: 'No long-term issues found.',
        VALIDATION: 'Issue data is invalid.',
        DELETE_FAILED: 'Failed to delete issue. Please try again.',
        ARCHIVE_FAILED: 'Failed to archive issue. Please try again.',
        UNARCHIVE_FAILED: 'Failed to unarchive issue. Please try again.',
    },
    ISSUE_TYPE: {
        LOAD_FAILED: 'Failed to load issue types. Please try again.',
        NOT_FOUND: (issueType?: string) => `Issue type "${issueType}" not found.`,
    },
    HEADLINE: {
        LOAD_FAILED: 'Failed to load headlines. Please try again.',
        NOT_FOUND: 'No headlines found.',
        ARCHIVED_NOT_FOUND: 'No archived headlines found.',
        VALIDATION: 'Headline data is invalid.',
        DELETE_FAILED: 'Failed to delete headline. Please try again.',
        ARCHIVE_FAILED: 'Failed to archive headline. Please try again.',
        UNARCHIVE_FAILED: 'Failed to unarchive headline. Please try again.',
    },
    L10_MEETING: {
        LOAD_FAILED: 'Failed to load L10 meetings. Please try again.',
        NOT_FOUND: 'No L10 meetings found.',
    },
    ROCK: {
        LOAD_FAILED: 'Failed to load rocks. Please try again.',
        NOT_FOUND: 'No rocks found.',
        ARCHIVED_NOT_FOUND: 'No archived rocks found.',
        UPDATE_STATUS_FAILED: 'Failed to update rock status. Please try again.',
        ARCHIVE_FAILED: 'Failed to archive rock. Please try again.',
        UNARCHIVE_FAILED: 'Failed to unarchive rock. Please try again.',
        CREATE_FAILED: 'Failed to create rock. Please try again.',
        UPDATE_FAILED: 'Failed to update rock. Please try again.',
    },
    METRIC: {
        LOAD_FAILED: 'Failed to load metrics. Please try again.',
        NOT_FOUND: 'No metrics found.',
        VALIDATION: 'Metric data is invalid.',
        CREATE_FAILED: 'Failed to create metric. Please try again.',
        UPDATE_FAILED: 'Failed to update metric. Please try again.',
        TREND_LOAD_FAILED: 'Failed to load trend metrics. Please try again.',
        TREND_NOT_FOUND: 'No trend metrics found.',
    },
} as const;

export const SUCCESS_MESSAGES = {
    TEAM: {
        CREATED: 'Team created successfully!',
        UPDATED: 'Team updated successfully!',
        DELETED: 'Team deleted successfully!',
    },
    USER: {
        CREATED: 'User created successfully!',
        UPDATED: 'User updated successfully!',
        DELETED: 'User deleted successfully!',
    },
    METRIC: {
        CREATED: 'Metric created successfully!',
        UPDATED: 'Metric updated successfully!',
    },
    TODO: {
        CREATED: 'To-do created successfully!',
        UPDATED: 'To-do updated successfully!',
        DELETED: 'To-do deleted successfully!',
        ARCHIVED: 'To-do archived successfully!',
        UNARCHIVED: 'To-do unarchived successfully!',
    },
    ISSUE: {
        CREATED: 'Issue created successfully!',
        UPDATED: 'Issue updated successfully!',
        DELETED: 'Issue deleted successfully!',
        ARCHIVED: 'Issue archived successfully!',
        UNARCHIVED: 'Issue unarchived successfully!',
    },
    HEADLINE: {
        CREATED: 'Headline created successfully!',
        UPDATED: 'Headline updated successfully!',
        DELETED: 'Headline deleted successfully!',
        ARCHIVED: 'Headline archived successfully!',
        UNARCHIVED: 'Headline unarchived successfully!',
    },
    L10_MEETING: {
        SCHEDULED: 'L10 meeting scheduled successfully!',
    },
    ROCK: {
        CREATED: 'Rock created successfully!',
        UPDATED: 'Rock updated successfully!',
        DELETED: 'Rock deleted successfully!',
        STATUS_UPDATED: 'Rock status updated successfully!',
        ARCHIVED: 'Rock archived successfully!',
        UNARCHIVED: 'Rock unarchived successfully!',
    },
} as const;

export const CONFIRM_MESSAGES = {
    DELETE: {
        CONFIRM_ITEM: (item: string) => `Are you sure you want to delete "${item}"?`,
        GENERIC: 'Are you sure you want to delete this item?',
    },
} as const;

export const UI_MESSAGES = {
    COMMON: {
        LOADING: 'Loading...',
        EMPTY: 'No data available',
    },
    YEAR: {
        LABEL: 'Year:',
        LOAD_ERROR: 'Failed to load years.',
    },
    QUARTER: {
        LABEL: 'Quarter:',
        LOAD_ERROR: 'Failed to load quarters.',
    },
    WEEK: {
        LABEL: 'Week:',
        LOAD_ERROR: 'Failed to load weeks.',
        NOT_FOUND: 'No weeks available.',
    },
} as const;
