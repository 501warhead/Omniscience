package io.github.warhead501.omniscience.command.result;

import com.mongodb.lang.Nullable;

public class CommandResult {

    private final String reason;
    private final Status status;

    private CommandResult(@Nullable String reason, Status status) {
        this.reason = reason;
        this.status = status;
    }

    public static CommandResult success() {
        return new CommandResult(null, Status.SUCCESS);
    }

    public static CommandResult failure(String reason) {
        return new CommandResult(reason, Status.FAILURE);
    }

    public String getReason() {
        return reason;
    }

    public Status getStatus() {
        return status;
    }

    public boolean wasSuccessful() {
        return status == Status.SUCCESS;
    }

    enum Status {
        SUCCESS,
        FAILURE
    }
}
