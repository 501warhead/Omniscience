package io.github.warhead501.omniscience.api.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.github.warhead501.omniscience.api.data.InventoryTransaction;
import io.github.warhead501.omniscience.api.data.Transaction;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static io.github.warhead501.omniscience.api.data.InventoryTransaction.ActionType.*;

public final class InventoryUtil {

    public static List<InventoryTransaction<ItemStack>> identifyTransactions(InventoryClickEvent e) {
        List<InventoryTransaction<ItemStack>> transactions = Lists.newLinkedList();
        if (e.getAction() == InventoryAction.CLONE_STACK) {
            transactions.add(new InventoryTransaction<>(null, e.getCurrentItem(), e.getCurrentItem(), -1, e.getInventory().getHolder(), CLONE));
        } else {
            InventoryHolder holder = e.getInventory().getHolder();
            boolean inInventory = e.getRawSlot() < e.getInventory().getSize();
            switch (e.getAction()) {
                case NOTHING:
                    break;
                case PICKUP_ALL:
                    if (inInventory) {
                        ItemStack is = e.getCurrentItem().clone();
                        int clicked = e.getSlot();
                        transactions.add(new InventoryTransaction<>(is, null, is, clicked, holder, WITHDRAW));
                    }
                    break;
                case PICKUP_SOME:
                    if (inInventory) {
                        ItemStack cursor = e.getCursor().clone();
                        int clicked = e.getSlot();
                        ItemStack is = e.getCurrentItem().clone();
                        if (cursor != null && is != null && cursor.isSimilar(is)) {
                            int neededForMax = cursor.getMaxStackSize() - cursor.getAmount();
                            int withdrew = is.getAmount() < neededForMax ? is.getAmount() : neededForMax;
                            ItemStack newIs = is.clone();
                            ItemStack leftOver = is.clone();
                            newIs.setAmount(withdrew);
                            if (is.getAmount() - withdrew > 0) {
                                leftOver.setAmount(is.getAmount() - withdrew);
                                transactions.add(new InventoryTransaction<>(is, leftOver, newIs, clicked, holder, WITHDRAW));
                            } else {
                                transactions.add(new InventoryTransaction<>(is, null, newIs, clicked, holder, WITHDRAW));
                            }
                        }
                    }
                    break;
                case PICKUP_HALF:
                    if (inInventory) {
                        int clicked = e.getSlot();
                        ItemStack is = e.getCurrentItem().clone();
                        if (is != null && !is.getType().name().contains("AIR")) {
                            final int amount;
                            if (is.getAmount() == 1 || is.getAmount() == 2) {
                                amount = 1;
                            } else {
                                if (is.getAmount() % 2 == 1) {
                                    amount = ((is.getAmount() - 1) / 2) + 1;
                                } else {
                                    amount = is.getAmount() / 2;
                                }
                            }
                            ItemStack newIs = is.clone();
                            newIs.setAmount(amount);

                            if (is.getAmount() - amount > 0) {
                                ItemStack leftOver = is.clone();
                                leftOver.setAmount(is.getAmount() - amount);
                                transactions.add(new InventoryTransaction<>(is, leftOver, newIs, clicked, holder, WITHDRAW));
                            } else {
                                transactions.add(new InventoryTransaction<>(is, null, newIs, clicked, holder, WITHDRAW));
                            }

                        }
                    }
                    break;
                case PICKUP_ONE:
                    if (inInventory) {
                        int clicked = e.getSlot();
                        ItemStack is = e.getCurrentItem().clone();
                        //TODO we need to verify that this isnt called when the itemstack in the players hand is @ max capacity
                        if (is != null
                                && !is.getType().name().contains("AIR")) {
                            ItemStack newIs = is.clone();
                            newIs.setAmount(1);
                            if (is.getAmount() - 1 > 0) {
                                ItemStack leftOver = is.clone();
                                leftOver.setAmount(is.getAmount() - 1);
                                transactions.add(new InventoryTransaction<>(is, leftOver, newIs, clicked, holder, WITHDRAW));
                            } else {
                                transactions.add(new InventoryTransaction<>(is, null, newIs, clicked, holder, WITHDRAW));
                            }

                        }
                    }
                    break;
                case PLACE_SOME:
                    if (inInventory) {
                        ItemStack cursor = e.getCursor().clone();
                        int clicked = e.getSlot();
                        ItemStack is = e.getCurrentItem().clone();
                        if (cursor != null && is != null && cursor.isSimilar(is)) {
                            int neededForMax = is.getMaxStackSize() - is.getAmount();
                            int deposited = cursor.getAmount() < neededForMax ? cursor.getAmount() : neededForMax;
                            ItemStack newCursor = cursor.clone();
                            newCursor.setAmount(deposited);
                            ItemStack afterDeposit = is.clone();
                            afterDeposit.setAmount(is.getAmount() + deposited);
                            transactions.add(new InventoryTransaction<>(is, afterDeposit, newCursor, clicked, holder, DEPOSIT));
                        }
                    }
                    break;
                case PLACE_ALL:
                    if (inInventory) {
                        ItemStack is = e.getCursor();
                        int clicked = e.getSlot();
                        transactions.add(new InventoryTransaction<>(null, is, is, clicked, holder, DEPOSIT));
                    }
                    break;
                case PLACE_ONE:
                    if (inInventory) {
                        ItemStack cursor = e.getCursor().clone();
                        int clicked = e.getSlot();
                        ItemStack is = e.getCurrentItem().clone();
                        if (cursor != null) {
                            if (is != null && is.isSimilar(cursor)) {
                                if (is.getMaxStackSize() - is.getAmount() >= 1) {
                                    ItemStack newCursor = cursor.clone();
                                    newCursor.setAmount(1);
                                    ItemStack afterDeposit = is.clone();
                                    afterDeposit.setAmount(is.getAmount() + 1);
                                    transactions.add(new InventoryTransaction<>(is, afterDeposit, newCursor, clicked, holder, DEPOSIT));
                                    break;
                                }
                            } else {
                                ItemStack newCursor = cursor.clone();
                                newCursor.setAmount(1);
                                transactions.add(new InventoryTransaction<>(null, newCursor, newCursor, clicked, holder, DEPOSIT));
                            }
                        }
                    }
                    break;
                case SWAP_WITH_CURSOR:
                    if (inInventory) {
                        ItemStack cursor = e.getCursor();
                        ItemStack toSwap = e.getCurrentItem();
                        int clicked = e.getSlot();
                        transactions.add(new InventoryTransaction<>(toSwap, cursor, cursor, clicked, holder, DEPOSIT));
                        transactions.add(new InventoryTransaction<>(toSwap, cursor, toSwap, clicked, holder, WITHDRAW));
                    }
                    break;
                case DROP_ALL_CURSOR:
                case DROP_ONE_CURSOR:
                    //NO:OP
                    break;
                case DROP_ALL_SLOT:
                    if (inInventory) {
                        ItemStack item = e.getCurrentItem().clone();
                        transactions.add(new InventoryTransaction<>(item, null, item, e.getSlot(), holder, WITHDRAW));
                    }
                    break;
                case DROP_ONE_SLOT:
                    if (inInventory) {
                        ItemStack item = e.getCurrentItem().clone();

                        if (item.getAmount() - 1 > 0) {
                            ItemStack leftOver = item.clone();
                            leftOver.setAmount(item.getAmount() - 1);
                            item.setAmount(1);
                            transactions.add(new InventoryTransaction<>(e.getCurrentItem().clone(), leftOver, item, e.getSlot(), holder, WITHDRAW));
                        } else {
                            item.setAmount(1);
                            transactions.add(new InventoryTransaction<>(e.getCurrentItem().clone(), null, item, e.getSlot(), holder, WITHDRAW));
                        }
                    }
                    break;
                case MOVE_TO_OTHER_INVENTORY:
                    ItemStack is = e.getCurrentItem().clone();
                    Inventory tar = inInventory ? e.getWhoClicked().getInventory() : e.getInventory();
                    int leftOver = is.getAmount();
                    if (tar.all(e.getCurrentItem().getType()).size() > 0) {
                        Map<Integer, ? extends ItemStack> items = tar.all(e.getCurrentItem().getType());
                        Map<Integer, ItemTransaction> changedItems = Maps.newHashMap();

                        for (Map.Entry<Integer, ? extends ItemStack> entry : items.entrySet()) {
                            ItemStack invItem = entry.getValue().clone();
                            if (is.isSimilar(invItem)) {
                                int diff = invItem.getMaxStackSize() - invItem.getAmount();
                                // Item amount = 16
                                // 64 - 61 = 3: diff is 3.
                                // 16 - 3 = 13, aka amt - diff = leftover
                                // 3 items were placed into the inventory at this location
                                if (diff > 0) {
                                    if (leftOver - diff <= 0) {
                                        invItem.setAmount(leftOver);
                                        leftOver -= diff;
                                        ItemStack afterDeposit = invItem.clone();
                                        afterDeposit.setAmount(entry.getValue().getAmount() + leftOver);
                                        changedItems.put(entry.getKey(), new ItemTransaction(invItem, new Transaction<>(entry.getValue().clone(), afterDeposit)));
                                        break;
                                    } else {
                                        invItem.setAmount(diff);
                                        leftOver -= diff;
                                        ItemStack afterDeposit = invItem.clone();
                                        afterDeposit.setAmount(entry.getValue().getAmount() + diff);
                                        changedItems.put(entry.getKey(), new ItemTransaction(invItem, new Transaction<>(entry.getValue().clone(), afterDeposit)));
                                    }
                                }
                            }
                        }
                        changedItems.forEach((key, value) -> transactions.add(new InventoryTransaction<>(value.getItemTransaction().getOriginalState().orElse(null), value.getItemTransaction().getFinalState().orElse(null),
                                value.getChangedItem(), key, holder, inInventory ? WITHDRAW : DEPOSIT)));
                    }
                    if (tar.firstEmpty() != -1 && leftOver > 0) {
                        is.setAmount(leftOver > is.getMaxStackSize() ? is.getMaxStackSize() : leftOver);
                        if (inInventory) {
                            transactions.add(new InventoryTransaction<>(is, null, is, e.getSlot(), holder, WITHDRAW));
                        } else {
                            transactions.add(new InventoryTransaction<>(null, is, is, tar.firstEmpty(), holder, DEPOSIT));
                        }
                    }
                    break;
                case HOTBAR_MOVE_AND_READD:
                    if (inInventory) {
                        int slot = e.getHotbarButton();
                        ItemStack item = e.getWhoClicked().getInventory().getItem(slot).clone();
                        ItemStack current = e.getCurrentItem().clone();
                        if (item.isSimilar(current)
                                && current.getAmount() < current.getMaxStackSize()) {
                            int toCap = current.getMaxStackSize() - current.getAmount();
                            ItemStack newStack = item.clone();
                            if (toCap < item.getAmount()) {
                                newStack.setAmount(toCap);
                            }
                            ItemStack afterDeposit = item.clone();
                            afterDeposit.setAmount(item.getAmount() + toCap);
                            transactions.add(new InventoryTransaction<>(item, afterDeposit, newStack, e.getSlot(), holder, DEPOSIT));
                        } else if (current == null || current.getType().name().contains("AIR")) {
                            transactions.add(new InventoryTransaction<>(null, item, item, e.getSlot(), holder, DEPOSIT));
                        } else if (!current.isSimilar(item)) {
                            transactions.add(new InventoryTransaction<>(current, item, current, e.getSlot(), holder, WITHDRAW));
                            if (item != null && !item.getType().name().contains("AIR")) {
                                transactions.add(new InventoryTransaction<>(current, item, item, e.getSlot(), holder, DEPOSIT));
                            }
                        }
                    }
                    break;
                case HOTBAR_SWAP:
                    if (inInventory) {
                        int slot = e.getHotbarButton();

                        ItemStack item = e.getWhoClicked().getInventory().getItem(slot);
                        ItemStack toSwap = e.getCurrentItem();
                        if (toSwap != null && !toSwap.getType().name().contains("AIR")) {
                            toSwap = toSwap.clone();
                            transactions.add(new InventoryTransaction<>(toSwap, item, toSwap, e.getSlot(), holder, WITHDRAW));
                        }
                        if (item != null && !item.getType().name().contains("AIR")) {
                            item = item.clone();
                            transactions.add(new InventoryTransaction<>(toSwap, item, item, e.getSlot(), holder, DEPOSIT));
                        }
                    }
                    break;
                case CLONE_STACK:
                    //NO:OP
                    break;
                case COLLECT_TO_CURSOR:
                    InventoryView view = e.getView();
                    ItemStack targetItem = e.getCurrentItem().clone();
                    int currentAmount = targetItem.getAmount();
                    int spaceLeft = targetItem.getMaxStackSize() - currentAmount;
                    Map<Integer, ? extends ItemStack> containerInventory = holder.getInventory().all(targetItem);
                    Map<Integer, ? extends ItemStack> playerInventory = e.getWhoClicked().getInventory().all(targetItem);
                    Map<ItemWrapper, ItemTransaction> changedItems = Maps.newHashMap();
                    for (Map.Entry<Integer, ? extends ItemStack> entry : containerInventory.entrySet()) {
                        ItemStack invItem = entry.getValue().clone();
                        int itemAmount = invItem.getAmount();
                        if (spaceLeft - itemAmount <= 0) {
                            ItemStack afterWithdraw = invItem.clone();
                            afterWithdraw.setAmount(itemAmount - spaceLeft);
                            changedItems.put(new ItemWrapper(true, entry.getKey()), new ItemTransaction(invItem, new Transaction<>(invItem, afterWithdraw)));
                            spaceLeft -= itemAmount;
                            break;
                        } else {
                            spaceLeft -= itemAmount;
                            changedItems.put(new ItemWrapper(true, entry.getKey()), new ItemTransaction(invItem, new Transaction<>(invItem, null)));
                        }
                    }
                    if (spaceLeft > 0) {
                        for (Map.Entry<Integer, ? extends ItemStack> entry : playerInventory.entrySet()) {
                            ItemStack invItem = entry.getValue().clone();
                            int itemAmount = invItem.getAmount();
                            if (spaceLeft - itemAmount <= 0) {
                                ItemStack afterWithdraw = invItem.clone();
                                afterWithdraw.setAmount(itemAmount - spaceLeft);
                                changedItems.put(new ItemWrapper(false, entry.getKey()), new ItemTransaction(invItem, new Transaction<>(invItem, afterWithdraw)));
                                spaceLeft -= itemAmount;
                                break;
                            } else {
                                spaceLeft -= itemAmount;
                                changedItems.put(new ItemWrapper(false, entry.getKey()), new ItemTransaction(invItem, new Transaction<>(invItem, null)));
                            }
                        }
                    }
                    for (Map.Entry<ItemWrapper, ItemTransaction> item : changedItems.entrySet()) {
                        if (item.getKey().top) {
                            transactions.add(new InventoryTransaction<>(item.getValue().itemTransaction.getOriginalState().orElse(null), item.getValue().itemTransaction.getFinalState().orElse(null),
                                    item.getValue().getChangedItem(), item.getKey().slot, holder, WITHDRAW));
                        }
                    }
                    break;
                case UNKNOWN:
                    break;
            }
        }
        return transactions;
    }

    public static List<InventoryTransaction<ItemStack>> identifyTransactions(InventoryDragEvent e) {
        List<InventoryTransaction<ItemStack>> transactions = Lists.newLinkedList();
        InventoryHolder holder = e.getInventory().getHolder();
        if (holder != null) {
            e.getNewItems().forEach((key, value) -> {
                if (key < holder.getInventory().getSize()) {
                    ItemStack original = holder.getInventory().getItem(key);
                    if (original == null || original.getType().name().contains("AIR")) {
                        transactions.add(new InventoryTransaction<>(null, value, value, key, holder, DEPOSIT));
                    } else {
                        int diff = value.getAmount() - original.getAmount();
                        if (diff > 0) {
                            ItemStack diffItem = value.clone();
                            diffItem.setAmount(diff);
                            transactions.add(new InventoryTransaction<>(original, value, diffItem, key, holder, DEPOSIT));
                        } else {
                            transactions.add(new InventoryTransaction<>(null, value, value, key, holder, DEPOSIT));
                        }
                    }

                }
            });
        }
        return transactions;
    }

    private static class ItemWrapper {
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

    private static class ItemTransaction {
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
