interface EmptyTeamStateProps {
    message?: string;
}

export const EmptyTeamState = ({ message = 'Please select a team.' }: EmptyTeamStateProps) => {
    return (
        <div className="flex h-[400px] items-center justify-center">
            <p className="text-muted-foreground">{message}</p>
        </div>
    );
};
