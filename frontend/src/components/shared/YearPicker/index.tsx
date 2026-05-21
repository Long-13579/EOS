'use client';

import { useEffect, useMemo, useRef, useState } from 'react';
import { Check, ChevronsUpDown } from 'lucide-react';
import { cn } from '@/lib/utils';
import { Button } from '@/components/ui/button';
import { Command, CommandEmpty, CommandGroup, CommandInput, CommandItem, CommandList } from '@/components/ui/command';
import { Popover, PopoverContent, PopoverTrigger } from '@/components/ui/popover';

interface YearPickerProps {
    value?: number;
    onChange: (year: number) => void;
    startYear?: number;
    futureOffset?: number;
    placeholder?: string;
    disabled?: boolean;
}

export function YearPicker({
    value,
    onChange,
    startYear = 1900,
    futureOffset = 100,
    placeholder = 'Select year...',
    disabled = false,
}: YearPickerProps) {
    const [open, setOpen] = useState(false);
    const listContainerRef = useRef<HTMLDivElement>(null);

    // 1. Perpetual Logic: Runs once on mount
    const years = useMemo(() => {
        const currentYear = new Date().getFullYear();
        const endYear = currentYear + futureOffset;
        return Array.from(
            { length: endYear - startYear + 1 },
            (_, i) => startYear + i, // Ascending order: newest last
        );
    }, [startYear, futureOffset]);

    useEffect(() => {
        if (!open || !value) {
            return;
        }

        const frameId = requestAnimationFrame(() => {
            const selectedItem = listContainerRef.current?.querySelector<HTMLElement>(`[data-year="${value}"]`);
            selectedItem?.scrollIntoView({ block: 'center' });
        });

        return () => cancelAnimationFrame(frameId);
    }, [open, value]);

    return (
        <Popover open={open} onOpenChange={setOpen}>
            <PopoverTrigger asChild>
                <Button
                    aria-label="Year"
                    variant="outline"
                    role="combobox"
                    aria-expanded={open}
                    className="w-full justify-between font-normal"
                    disabled={disabled}
                >
                    {value ? value : placeholder}
                    <ChevronsUpDown className="ml-2 h-4 w-4 shrink-0 opacity-50" />
                </Button>
            </PopoverTrigger>
            <PopoverContent className="w-[220px] p-0" align="start">
                <Command>
                    <CommandInput placeholder="Search a year..." />
                    <CommandList ref={listContainerRef} className="max-h-72 pr-2" onWheel={(e) => e.stopPropagation()}>
                        <CommandEmpty>No year found.</CommandEmpty>
                        <CommandGroup>
                            {years.map((year) => (
                                <CommandItem
                                    key={year}
                                    data-year={year}
                                    value={year.toString()}
                                    onSelect={(currentValue) => {
                                        onChange(Number(currentValue));
                                        setOpen(false);
                                    }}
                                >
                                    <Check className={cn('mr-2 h-4 w-4', value === year ? 'opacity-100' : 'opacity-0')} />
                                    {year}
                                </CommandItem>
                            ))}
                        </CommandGroup>
                    </CommandList>
                </Command>
            </PopoverContent>
        </Popover>
    );
}
