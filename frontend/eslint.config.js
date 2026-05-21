import js from '@eslint/js';
import globals from 'globals';
import reactHooks, { rules } from 'eslint-plugin-react-hooks';
import reactRefresh from 'eslint-plugin-react-refresh';
import tseslint from 'typescript-eslint';
import { defineConfig, globalIgnores } from 'eslint/config';
import eslintConfigPrettier from 'eslint-config-prettier';
import importX from 'eslint-plugin-import-x';

export default defineConfig([
    globalIgnores(['dist', 'node_modules', 'src/components/ui/**']),
    {
        files: ['**/*.{ts,tsx}'],
        extends: [
            js.configs.recommended,
            eslintConfigPrettier,
            reactHooks.configs.flat.recommended,
            reactRefresh.configs.vite,
            ...tseslint.configs.recommended,
        ],
        languageOptions: {
            ecmaVersion: 2020,
            globals: globals.browser,
        },
        plugins: {
            import: importX,
        },
        rules: {
            curly: ['error', 'all'],
        },
    },
]);
