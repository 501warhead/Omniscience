package io.github.warhead501.omniscience.api.data;

import com.google.common.base.MoreObjects;

import java.util.Objects;
import java.util.Optional;

/**
 * A record that essentially shows the progress of something over time. It includes an original, pre-change version and a final, post-change version.
 *
 * @param <T> The class that a change is being recorded for
 */
public class Transaction<T> {

    private final T originalState;
    private final T finalState;

    public Transaction(T originalState, T finalState) {
        this.originalState = originalState;
        this.finalState = finalState;
    }

    public Optional<T> getOriginalState() {
        return Optional.ofNullable(originalState);
    }

    public Optional<T> getFinalState() {
        return Optional.ofNullable(finalState);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction<?> that = (Transaction<?>) o;
        return Objects.equals(originalState, that.originalState) &&
                Objects.equals(finalState, that.finalState);
    }

    @Override
    public int hashCode() {
        return Objects.hash(originalState, finalState);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("originalState", originalState)
                .add("finalState", finalState)
                .toString();
    }
}
