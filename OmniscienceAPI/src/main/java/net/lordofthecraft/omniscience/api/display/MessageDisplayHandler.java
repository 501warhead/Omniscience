package net.lordofthecraft.omniscience.api.display;

import com.google.common.collect.Lists;
import net.lordofthecraft.omniscience.api.entry.DataEntry;
import net.lordofthecraft.omniscience.api.flag.Flag;
import net.lordofthecraft.omniscience.api.query.QuerySession;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static net.lordofthecraft.omniscience.api.data.DataKeys.MESSAGE;
import static net.lordofthecraft.omniscience.api.data.DataKeys.TARGET_META;

public class MessageDisplayHandler extends SimpleDisplayHandler {

    public MessageDisplayHandler() {
        super("message");
    }

    @Override
    public Optional<String> buildTargetMessage(DataEntry entry, String target, QuerySession session) {
        if (!session.hasFlag(Flag.NO_GROUP) || !entry.data.getKeys(false).contains(MESSAGE)) {
            return Optional.empty();
        }
        return entry.data.getString(MESSAGE);
    }

    @Override
    public Optional<List<String>> buildAdditionalHoverData(DataEntry entry, QuerySession session) {
    	 if (!entry.data.getKeys(false).contains(TARGET_META)) {
             return Optional.empty();
         }
    	 return Optional.of(Lists.newArrayList(
                 ChatColor.DARK_GRAY + "Recipients: " + ChatColor.WHITE + StringUtils.join(entry.data.getStringList(TARGET_META).get().stream().map(uid->
                         Bukkit.getOfflinePlayer(UUID.fromString(uid)).getName()).collect(Collectors.toList()), ", ")
         ));
    }
}
