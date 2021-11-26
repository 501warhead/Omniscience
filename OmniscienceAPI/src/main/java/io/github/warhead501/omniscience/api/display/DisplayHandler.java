package io.github.warhead501.omniscience.api.display;

import io.github.warhead501.omniscience.api.query.QuerySession;
import io.github.warhead501.omniscience.api.data.DataKey;
import io.github.warhead501.omniscience.api.data.DataKeys;
import io.github.warhead501.omniscience.api.data.DataWrapper;
import io.github.warhead501.omniscience.api.entry.DataEntry;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.List;
import java.util.Optional;

/**
 * An object that will mutate various things that are displayed to the end user when they perform searches.
 *
 * @author 501warhead
 */
public interface DisplayHandler {

    /**
     * Determined whether or not this handler will handle the given value.
     * <p>
     * This method will be provided the value that is stored in a {@link DataWrapper}
     * with the {@link DataKey}
     * of {@link DataKeys#DISPLAY_METHOD}. This is done so that we can be robust and selective about when these handlers are used.
     * </p>
     *
     * @param displayTag The stored display method value
     * @return Whether or not this handler will handle this method
     */
    boolean handles(String displayTag);

    /**
     * Potentially builds a different message to be used in the "Target" field for this search result.
     *
     * @param entry   The DataEntry to build for
     * @param target  The original value of target
     * @param session The session that is being used for this search
     * @return An optional containing a new message, or an empty one to have no changes to the message.
     */
    Optional<String> buildTargetMessage(DataEntry entry, String target, QuerySession session);

    /**
     * Potentially adds new lines of data onto the hover messages presented to players for the various entries. This won't be shown to non-players.
     *
     * @param entry   The DataEntry to build for
     * @param session The session that is being used for this search
     * @return An optional containing a list of new messages to append to the hover message. They will be added at the end of the existing hover message.
     */
    Optional<List<String>> buildAdditionalHoverData(DataEntry entry, QuerySession session);

    /**
     * Potentially builds out a specific, unique hover message for when a player mouses over the "target" of an event. Can be used for a variety of methods, such as displaying item data.
     *
     * @param entry   The DataEntry to build for
     * @param target  The value of target for this entry
     * @param session The session that is being used for this search
     * @return An optional containing a TextComponent that may have such things like hover or click events.
     */
    default Optional<TextComponent> buildTargetSpecificHoverData(DataEntry entry, String target, QuerySession session) {
        return Optional.empty();
    }
}
