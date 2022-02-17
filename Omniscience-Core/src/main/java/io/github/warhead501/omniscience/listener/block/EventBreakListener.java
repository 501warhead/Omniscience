package io.github.warhead501.omniscience.listener.block;

import com.google.common.collect.ImmutableList;
import io.github.warhead501.omniscience.api.data.LocationTransaction;
import io.github.warhead501.omniscience.api.entry.OEntry;
import io.github.warhead501.omniscience.Omniscience;
import io.github.warhead501.omniscience.listener.OmniListener;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.*;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.Bed;
import org.bukkit.block.data.type.Door;
import org.bukkit.block.data.type.Lantern;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityBreakDoorEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValue;

import java.util.List;
import java.util.UUID;

public class EventBreakListener extends OmniListener {

    private static final BlockFace[] DIRS = new BlockFace[]{BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH};

    public EventBreakListener() {
        super(ImmutableList.of("break"));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent event) {
        OEntry.create().source(event.getPlayer()).brokeBlock(new LocationTransaction<>(event.getBlock().getLocation(), event.getBlock().getState(), null)).save();
        saveContainerDrops(event.getPlayer(), event.getBlock());
        //For rollbacks and restores dependents should be saved after the parent
        saveMultiBreak(event.getPlayer(), event.getBlock());
    }


    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBlockExplode(BlockExplodeEvent event) {
        if (event.getBlock().hasMetadata("player-source")) {
            List<MetadataValue> metadataValues = event.getBlock().getMetadata("player-source");
            for (MetadataValue value : metadataValues) {
                if (writeBlockBreakForMetaData(value, event.blockList(), event.getBlock())) {
                    return;
                }
            }
        } else {
            event.blockList().forEach(block -> {
                OEntry.create().source(event.getBlock().getType().name()).brokeBlock(new LocationTransaction<>(block.getLocation(), block.getState(), null)).save();
                saveContainerDrops(event.getBlock(), block);
                saveMultiBreak(event.getBlock(), block);
            });
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onEntityBreakDoor(EntityBreakDoorEvent e) {
        OEntry.create().source(e.getEntity()).brokeBlock(new LocationTransaction<>(e.getBlock().getLocation(), e.getBlock().getState(), null)).save();
        Door door = (Door) e.getBlockData();
        final Block otherHalf;
        if (door.getHalf() == Bisected.Half.TOP) {
            otherHalf = e.getBlock().getRelative(BlockFace.DOWN);
        } else {
            otherHalf = e.getBlock().getRelative(BlockFace.UP);
        }
        if (otherHalf.getBlockData() instanceof Door) {
            OEntry.create().source(e.getEntity()).brokeBlock(new LocationTransaction<>(otherHalf.getLocation(), otherHalf.getState(), null)).save();
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onEntityExplode(EntityExplodeEvent event) {
        if (event.getEntity().hasMetadata("player-source")) {
            List<MetadataValue> metadataValues = event.getEntity().getMetadata("player-source");
            for (MetadataValue value : metadataValues) {
                if (writeBlockBreakForMetaData(value, event.blockList(), event.getEntity())) {
                    return;
                }
            }
        } else {
            event.blockList()
                    .stream()
                    .filter(block -> block.getType() == Material.CAVE_AIR)
                    .forEach(block -> {
                        OEntry.create().source(event.getEntity()).brokeBlock(new LocationTransaction<>(block.getLocation(), block.getState(), null)).save();
                        saveContainerDrops(event.getEntity(), block);
                        //For rollbacks and restores dependents should be saved after the parent
                        saveMultiBreak(event.getEntity(), block);
                    });
            event.blockList()
                    .stream()
                    .filter(block -> block.getType() != Material.CAVE_AIR)
                    .forEach(block -> {
                        OEntry.create().source(event.getEntity()).brokeBlock(new LocationTransaction<>(block.getLocation(), block.getState(), null)).save();
                        saveContainerDrops(event.getEntity(), block);
                        //For rollbacks and restores dependents should be saved after the parent
                        saveMultiBreak(event.getEntity(), block);
                    });
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBlockBurn(BlockBurnEvent event) {
        OEntry.create().environment().brokeBlock(new LocationTransaction<>(event.getBlock().getLocation(), event.getBlock().getState(), null)).save();
        saveDependantBreaks(null, event.getBlock());
    }

    private boolean writeBlockBreakForMetaData(MetadataValue value, List<Block> blocks, Object source) {
        if (value.getOwningPlugin() instanceof Omniscience) {
            OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(value.asString()));
            blocks.stream()
                    .filter(block -> block.getType() != Material.CAVE_AIR)
                    .forEach(block -> {
                        OEntry.create().source(player).brokeBlock(new LocationTransaction<>(block.getLocation(), block.getState(), null)).save();
                        saveContainerDrops(source, block);
                        //For rollbacks and restores dependents should be saved after the parent
                        saveMultiBreak(source, block);
                    });
            return true;
        }
        return false;
    }

    private void saveMultiBreak(Object source, Block broken) {
        if (broken.getType() == Material.CACTUS ||
                broken.getType() == Material.SUGAR_CANE ||
                broken.getType() == Material.KELP_PLANT ||
                broken.getType() == Material.BAMBOO) {
            Block step = broken.getRelative(BlockFace.UP);
            while (step.getType() == Material.CACTUS ||
                    step.getType() == Material.SUGAR_CANE ||
                    step.getType() == Material.KELP_PLANT ||
                    step.getType() == Material.BAMBOO) {
                OEntry.create().source(source).brokeBlock(new LocationTransaction<>(step.getLocation(), step.getState(), null)).save();
                saveDependantBreaks(source, step);
                step = step.getRelative(BlockFace.UP);
            }
        } else if (broken.getBlockData() instanceof Bed) {
            Bed bed = (Bed) broken.getBlockData();
            final Block otherBlock;
            if (bed.getPart() == Bed.Part.HEAD) {
                otherBlock = broken.getRelative(bed.getFacing().getOppositeFace());
            } else {
                otherBlock = broken.getRelative(bed.getFacing());
            }
            if (otherBlock.getBlockData() instanceof Bed) {
                OEntry.create().source(source).brokeBlock(new LocationTransaction<>(otherBlock.getLocation(), otherBlock.getState(), null)).save();
                saveDependantBreaks(source, otherBlock);
            }
        }
        saveDependantBreaks(source, broken);
    }

    private void saveContainerDrops(Object source, Block container) {
        if (container.getState() instanceof Container) {
            Container cont = (Container) container.getState();
            Inventory inventory = cont.getInventory();
            if (inventory.getHolder() instanceof DoubleChest) {
                DoubleChest chest = (DoubleChest) inventory.getHolder();
                Chest left = (Chest) chest.getLeftSide();
                Chest right = (Chest) chest.getRightSide();
                if (sameLocations(left.getBlock(), container)) {
                    inventory = left.getBlockInventory();
                } else if (sameLocations(right.getBlock(), container)) {
                    inventory = right.getBlockInventory();
                }
            }
            for (ItemStack content : inventory) {
                if (content != null && content.getType() != Material.AIR) {
                    OEntry.create().source(source).droppedItem(content, container.getLocation()).save();
                }
            }
        }
    }

    private boolean sameLocations(Block a, Block b) {
        return a.getX() == b.getX() && a.getY() == b.getY() && a.getZ() == b.getZ();
    }

    private void saveDependantBreaks(Object source, Block broken) {
        Block up = broken.getRelative(BlockFace.UP);
        Block down = broken.getRelative(BlockFace.DOWN);
        if (getStyle(up.getType()) == DependantStyle.BOTTOM) {
            OEntry.create().source(source).brokeBlock(new LocationTransaction<>(up.getLocation(), up.getState(), null)).save();
        } else if (getStyle(up.getType()) == DependantStyle.ALL) {
            if (up.getBlockData() instanceof Directional) {
                Directional direct = (Directional) up.getBlockData();
                if (direct.getFacing().getOppositeFace() == BlockFace.UP) {
                    OEntry.create().source(source).brokeBlock(new LocationTransaction<>(up.getLocation(), up.getState(), null)).save();
                }
            } else if (up.getBlockData() instanceof Lantern) {
                Lantern lantern = (Lantern) up.getBlockData();
                if (!lantern.isHanging()) {
                    OEntry.create().source(source).brokeBlock(new LocationTransaction<>(up.getLocation(), up.getState(), null)).save();
                }
            }
        } else if (getStyle(up.getType()) == DependantStyle.TALL) {
            OEntry.create().source(source).brokeBlock(new LocationTransaction<>(up.getLocation(), up.getState(), null)).save();
            if (getStyle(up.getRelative(BlockFace.UP).getType()) == DependantStyle.TALL) {
                Block up2 = up.getRelative(BlockFace.UP);
                OEntry.create().source(source).brokeBlock(new LocationTransaction<>(up2.getLocation(), up2.getState(), null)).save();
            }
        }
        for (BlockFace face : DIRS) {
            Block relative = broken.getRelative(face);
            if (getStyle(relative.getType()) == DependantStyle.WALL || getStyle(relative.getType()) == DependantStyle.ALL) {
                if (relative.getBlockData() instanceof Directional) {
                    Directional direct = (Directional) relative.getBlockData();
                    if (face == direct.getFacing()) {
                        OEntry.create().source(source).brokeBlock(new LocationTransaction<>(relative.getLocation(), relative.getState(), null)).save();
                    }
                }
            }
        }
        if (getStyle(down.getType()) == DependantStyle.ALL) {
            if (down.getBlockData() instanceof Directional) {
                Directional direct = (Directional) down.getBlockData();
                if (direct.getFacing().getOppositeFace() == BlockFace.DOWN) {
                    OEntry.create().source(source).brokeBlock(new LocationTransaction<>(down.getLocation(), down.getState(), null)).save();
                }
            } else if (down.getBlockData() instanceof Lantern) {
                Lantern lantern = (Lantern) down.getBlockData();
                if (lantern.isHanging()) {
                    OEntry.create().source(source).brokeBlock(new LocationTransaction<>(down.getLocation(), down.getState(), null)).save();
                }
            }
        } else if (getStyle(down.getType()) == DependantStyle.TALL && getStyle(broken.getType()) == DependantStyle.TALL) {
            OEntry.create().source(source).brokeBlock(new LocationTransaction<>(down.getLocation(), down.getState(), null)).save();
        }
    }

    /**
     * Fetches a registered list of dependent blocks and their style of dependency. Excludes {@link Bed} intentionally.
     * <p>
     * We do this because Spigot doesn't, and would reject a PR this small. (Just make a PR looooooooooooooooooooool)
     *
     * @param material The material to check for
     * @return The dependant style
     */
    private DependantStyle getStyle(Material material) {
        switch (material) {
            case POPPY:
            case DANDELION:
            case LILY_OF_THE_VALLEY:
            case WITHER_ROSE:
            case CORNFLOWER:
            case OAK_SAPLING:
            case BIRCH_SAPLING:
            case JUNGLE_SAPLING:
            case ACACIA_SAPLING:
            case DARK_OAK_SAPLING:
            case GRASS:
            case FERN:
            case DEAD_BUSH:
            case SEAGRASS:
            case SEA_PICKLE:
            case BRAIN_CORAL_FAN:
            case WHEAT:
            case POTATOES:
            case CARROTS:
            case BLUE_ORCHID:
            case ALLIUM:
            case RED_TULIP:
            case ORANGE_TULIP:
            case WHITE_TULIP:
            case PINK_TULIP:
            case OXEYE_DAISY:
            case BROWN_MUSHROOM:
            case RED_MUSHROOM:
            case TORCH:
            case REDSTONE_TORCH:
            case REDSTONE_WIRE:
            case COMPARATOR:
            case REPEATER:
            case BLACK_CARPET:
            case BLUE_CARPET:
            case BROWN_CARPET:
            case CYAN_CARPET:
            case GRAY_CARPET:
            case GREEN_CARPET:
            case LIGHT_BLUE_CARPET:
            case LIGHT_GRAY_CARPET:
            case LIME_CARPET:
            case MAGENTA_CARPET:
            case ORANGE_CARPET:
            case PINK_CARPET:
            case PURPLE_CARPET:
            case RED_CARPET:
            case WHITE_CARPET:
            case YELLOW_CARPET:
            case HORN_CORAL:
            case FIRE_CORAL:
            case TUBE_CORAL:
            case BRAIN_CORAL:
            case BUBBLE_CORAL:
            case DEAD_BRAIN_CORAL_BLOCK:
            case DEAD_BUBBLE_CORAL_BLOCK:
            case DEAD_FIRE_CORAL_BLOCK:
            case DEAD_HORN_CORAL_BLOCK:
            case DEAD_TUBE_CORAL_BLOCK:
            case ACACIA_PRESSURE_PLATE:
            case BIRCH_PRESSURE_PLATE:
            case DARK_OAK_PRESSURE_PLATE:
            case HEAVY_WEIGHTED_PRESSURE_PLATE:
            case JUNGLE_PRESSURE_PLATE:
            case LIGHT_WEIGHTED_PRESSURE_PLATE:
            case OAK_PRESSURE_PLATE:
            case SPRUCE_PRESSURE_PLATE:
            case STONE_PRESSURE_PLATE:
            case RAIL:
            case ACTIVATOR_RAIL:
            case DETECTOR_RAIL:
            case POWERED_RAIL:
            case CHORUS_FLOWER:
            case BEETROOTS:
            case MELON_STEM:
            case PUMPKIN_STEM:
            case ATTACHED_MELON_STEM:
            case ATTACHED_PUMPKIN_STEM:
            case SNOW:
            case SPRUCE_SIGN:
            case ACACIA_SIGN:
            case BIRCH_SIGN:
            case DARK_OAK_SIGN:
            case JUNGLE_SIGN:
            case OAK_SIGN:
                return DependantStyle.BOTTOM;
            case BUBBLE_CORAL_WALL_FAN:
            case DEAD_BRAIN_CORAL_WALL_FAN:
            case DEAD_BUBBLE_CORAL_WALL_FAN:
            case DEAD_FIRE_CORAL_WALL_FAN:
            case DEAD_HORN_CORAL_WALL_FAN:
            case DEAD_TUBE_CORAL_WALL_FAN:
            case FIRE_CORAL_WALL_FAN:
            case HORN_CORAL_WALL_FAN:
            case TUBE_CORAL_WALL_FAN:
            case WHITE_WALL_BANNER:
            case BLACK_WALL_BANNER:
            case BLUE_WALL_BANNER:
            case BROWN_WALL_BANNER:
            case CYAN_WALL_BANNER:
            case GRAY_WALL_BANNER:
            case GREEN_WALL_BANNER:
            case LIGHT_BLUE_WALL_BANNER:
            case LIGHT_GRAY_WALL_BANNER:
            case LIME_WALL_BANNER:
            case MAGENTA_WALL_BANNER:
            case ORANGE_WALL_BANNER:
            case PINK_WALL_BANNER:
            case PURPLE_WALL_BANNER:
            case RED_WALL_BANNER:
            case YELLOW_WALL_BANNER:
            case BRAIN_CORAL_WALL_FAN:
            case WALL_TORCH:
            case REDSTONE_WALL_TORCH:
            case FIRE_CORAL_FAN:
            case BUBBLE_CORAL_FAN:
            case DEAD_BRAIN_CORAL_FAN:
            case DEAD_BUBBLE_CORAL_FAN:
            case DEAD_FIRE_CORAL_FAN:
            case DEAD_HORN_CORAL_FAN:
            case DEAD_TUBE_CORAL_FAN:
            case HORN_CORAL_FAN:
            case TUBE_CORAL_FAN:
            case COCOA_BEANS:
            case LADDER:
            case TRIPWIRE_HOOK:
            case ACACIA_WALL_SIGN:
            case BIRCH_WALL_SIGN:
            case DARK_OAK_WALL_SIGN:
            case JUNGLE_WALL_SIGN:
            case OAK_WALL_SIGN:
            case SPRUCE_WALL_SIGN:
                return DependantStyle.WALL;
            case DARK_OAK_DOOR:
            case ACACIA_DOOR:
            case BIRCH_DOOR:
            case IRON_DOOR:
            case JUNGLE_DOOR:
            case OAK_DOOR:
            case SPRUCE_DOOR:
            case TALL_GRASS:
            case TALL_SEAGRASS:
            case SUNFLOWER:
            case LILAC:
            case PEONY:
            case ROSE_BUSH:
            case LARGE_FERN:
                return DependantStyle.TALL;
            case LEVER:
            case BIRCH_BUTTON:
            case ACACIA_BUTTON:
            case DARK_OAK_BUTTON:
            case JUNGLE_BUTTON:
            case OAK_BUTTON:
            case SPRUCE_BUTTON:
            case STONE_BUTTON:
            case LANTERN:
            case SOUL_LANTERN:
            case CHAIN:
                return DependantStyle.ALL;
            default:
                return DependantStyle.NONE;
        }
    }

    private enum DependantStyle {
        WALL,
        BOTTOM,
        TALL,
        ALL,
        NONE
    }
}
