package io.github.warhead501.omniscience.api.data;

import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class InventoryTransaction<T> extends Transaction<T> {

    private final ItemStack diff;
    private final int slot;
    private final InventoryHolder holder;
    private final ActionType type;

    public InventoryTransaction(T originalState, T finalState, ItemStack diff, int slot, InventoryHolder holder, ActionType type) {
        super(originalState, finalState);
        this.diff = diff;
        this.slot = slot;
        this.holder = holder;
        this.type = type;
    }

    public int getSlot() {
        return slot;
    }

    public ItemStack getDiff() {
        return diff;
    }

    public InventoryHolder getHolder() {
        return holder;
    }

    public ActionType getType() {
        return type;
    }

    public enum ActionType {
        WITHDRAW,
        DEPOSIT,
        CLONE
    }
}
