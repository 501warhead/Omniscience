package io.github.warhead501.omniscience.api.display;

import io.github.warhead501.omniscience.api.query.QuerySession;
import io.github.warhead501.omniscience.api.data.DataKeys;
import io.github.warhead501.omniscience.api.entry.DataEntry;
import io.github.warhead501.omniscience.api.util.reflection.ReflectionHandler;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Optional;

public class ItemDisplayHandler extends SimpleDisplayHandler {

    public ItemDisplayHandler() {
        super("item");
    }

    @Override
    public Optional<String> buildTargetMessage(DataEntry entry, String target, QuerySession session) {
        Optional<String> entity = entry.data.getString(DataKeys.ENTITY_TYPE);
        Optional<String> event = entry.data.getString(DataKeys.EVENT_NAME);
        boolean withdraw = event.isPresent() && event.get().contains("withdraw");
        return entity.map(s -> target + (withdraw ? " from " : " into ") + s);
    }

    @Override
    public Optional<List<String>> buildAdditionalHoverData(DataEntry entry, QuerySession session) {
        return Optional.empty();
    }

    @Override
    public Optional<TextComponent> buildTargetSpecificHoverData(DataEntry entry, String target, QuerySession session) {
        Optional<ItemStack> oItemStack = entry.data.getConfigSerializable(DataKeys.ITEMSTACK);
        if (oItemStack.isPresent()) {
            ItemStack is = oItemStack.get();
            TextComponent component = new TextComponent(target);
            ComponentBuilder hover = new ComponentBuilder("");
            hover.append(ReflectionHandler.getItemJson(is));
            component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, hover.create()));
            return Optional.of(component);
        }
        return Optional.empty();
    }
}
