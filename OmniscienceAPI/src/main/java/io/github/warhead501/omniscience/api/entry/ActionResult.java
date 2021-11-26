package io.github.warhead501.omniscience.api.entry;

import io.github.warhead501.omniscience.api.data.Transaction;

public class ActionResult {

    private final boolean changeWasApplied;
    private final SkipReason reason;
    private final Transaction transaction;

    private ActionResult(SkipReason reason) {
        this.reason = reason;
        this.changeWasApplied = false;
        this.transaction = null;
    }

    private ActionResult(Transaction transaction) {
        this.changeWasApplied = true;
        this.reason = null;
        this.transaction = transaction;
    }

    public static ActionResult success(Transaction transaction) {
        return new ActionResult(transaction);
    }

    public static ActionResult skipped(SkipReason reason) {
        return new ActionResult(reason);
    }

    public boolean applied() {
        return changeWasApplied;
    }

    public SkipReason getReason() {
        return reason;
    }

    public Transaction getTransaction() {
        return transaction;
    }

}
