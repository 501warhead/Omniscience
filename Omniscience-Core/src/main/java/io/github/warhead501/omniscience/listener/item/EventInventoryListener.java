package io.github.warhead501.omniscience.listener.item;

import com.google.common.collect.ImmutableList;
import io.github.warhead501.omniscience.api.data.InventoryTransaction;
import io.github.warhead501.omniscience.api.data.Transaction;
import io.github.warhead501.omniscience.api.entry.OEntry;
import io.github.warhead501.omniscience.api.util.InventoryUtil;
import io.github.warhead501.omniscience.Omniscience;
import io.github.warhead501.omniscience.listener.OmniListener;
import org.bukkit.Location;
import org.bukkit.block.Container;
import org.bukkit.block.DoubleChest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Objects;

public class EventInventoryListener extends OmniListener {

    public EventInventoryListener() {
        super(ImmutableList.of("withdraw", "deposit", "clone"));
    }

    /**
     * If this looks like a monster, it is.
     * <p>
     * We're going through and trying to filter down everything into either a WITHDRAW action or a DEPOSIT action. This means that sometimes, just sometimes,
     * we'll trigger both events - as is the case when we "swap" items on our cursor. There are some headaches we have to go and figure out - such as, wonderfully, how
     * in the world we handle when someone double clicks a stack to grab <i>everything</i> of that type. So here we are, trying to explain the abomination that is this
     * event handler in such a way that God won't suddenly decide we've gone too far and annihilate humanity as to prevent this work from ever seeing the light of day
     *
     * @param e Don't directly call this method for the love of all that is good and holy in the WORLD
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onInventoryClick(InventoryClickEvent e) {
        // This refactor goes out to tofuus who took the bait hook line and sinker
        List<InventoryTransaction<ItemStack>> transactions = InventoryUtil.identifyTransactions(e);
        for (InventoryTransaction<ItemStack> transaction : transactions) {
            if (transaction.getType() == InventoryTransaction.ActionType.CLONE) {
                OEntry.create().player(e.getWhoClicked()).cloned(transaction.getDiff()).save();
            } else {
                InventoryHolder holder = transaction.getHolder();
                if (holder instanceof Container || holder instanceof DoubleChest) {
                    Location location = holder instanceof Container ? ((Container) holder).getLocation() : ((DoubleChest) holder).getLocation();

                    switch (transaction.getType()) {
                        case WITHDRAW:
                            if (w()) {
                                OEntry.create().player(e.getWhoClicked()).withdrew(transaction, location, null).save();
                            }
                            break;
                        case DEPOSIT:
                            if (d()) {
                                OEntry.create().player(e.getWhoClicked()).deposited(transaction, location, null).save();
                            }
                            break;
                        case CLONE:
                            break;
                    }
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onInventoryDrag(InventoryDragEvent e) {
        if (e.getInventory().getHolder() instanceof Container) {
            List<InventoryTransaction<ItemStack>> transactions = InventoryUtil.identifyTransactions(e);
            InventoryHolder holder = e.getInventory().getHolder();
            Location loc = ((Container) holder).getLocation();
            for (InventoryTransaction<ItemStack> transaction : transactions) {
                switch (transaction.getType()) {
                    case WITHDRAW:
                    case CLONE:
                        // NO:OP
                        break;
                    case DEPOSIT:
                        if (d()) {
                            OEntry.create().player(e.getWhoClicked()).deposited(transaction, loc, null);
                        }
                        break;
                }
            }
        }
    }

    private boolean w() {
        return isEnabled("withdraw");
    }

    private boolean d() {
        return isEnabled("deposit");
    }

    private void debugEvent(InventoryClickEvent e) {
        Omniscience.logDebug("====== INVENTORY CLICK EVENT DEBUG ======");
        Omniscience.logDebug("Action: " + e.getAction());
        Omniscience.logDebug("Click: " + e.getClick());
        Omniscience.logDebug("Current Item: " + e.getCurrentItem());
        Omniscience.logDebug("Cursor Item: " + e.getCursor());
        Omniscience.logDebug("Hotbar button: " + e.getHotbarButton());
        Omniscience.logDebug("Slot: " + e.getSlot());
        Omniscience.logDebug("Slot Type: " + e.getSlotType());
        Omniscience.logDebug("Raw Slot: " + e.getRawSlot());
        Omniscience.logDebug("Inventory Clicked: " + e.getInventory());
        Omniscience.logDebug("Inventory in general: " + e.getInventory());
    }

    private class ItemWrapper {
        private final int slot;
        private final boolean top;

        ItemWrapper(boolean top, int slot) {
            this.top = top;
            this.slot = slot;
        }

        public boolean isTop() {
            return top;
        }

        public int getSlot() {
            return slot;
        }

        @Override
        public int hashCode() {
            return Objects.hash(top, slot);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ItemWrapper that = (ItemWrapper) o;
            return top == that.top &&
                    slot == that.slot;
        }

        @Override
        public String toString() {
            return "ItemWrapper{" +
                    "top=" + top +
                    ", slot=" + slot +
                    '}';
        }
    }

    private class ItemTransaction {
        private final ItemStack changedItem;
        private final Transaction<ItemStack> itemTransaction;

        public ItemTransaction(ItemStack changedItem, Transaction<ItemStack> itemTransaction) {
            this.changedItem = changedItem;
            this.itemTransaction = itemTransaction;
        }

        public ItemStack getChangedItem() {
            return changedItem;
        }

        public Transaction<ItemStack> getItemTransaction() {
            return itemTransaction;
        }

        @Override
        public int hashCode() {
            return Objects.hash(changedItem, itemTransaction);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ItemTransaction)) return false;
            ItemTransaction that = (ItemTransaction) o;
            return changedItem.equals(that.changedItem) &&
                    itemTransaction.equals(that.itemTransaction);
        }

        @Override
        public String toString() {
            return "ItemTransaction{" +
                    "changedItem=" + changedItem +
                    ", itemTransaction=" + itemTransaction +
                    '}';
        }
    }
}
