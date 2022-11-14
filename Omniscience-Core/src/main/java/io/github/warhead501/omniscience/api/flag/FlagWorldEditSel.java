package io.github.warhead501.omniscience.api.flag;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionSelector;
import io.github.warhead501.omniscience.api.data.DataKeys;
import io.github.warhead501.omniscience.api.query.*;
import io.github.warhead501.omniscience.api.util.Formatter;
import io.github.warhead501.omniscience.Omniscience;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class FlagWorldEditSel extends BaseFlagHandler {

    private final WorldEditPlugin worldEdit;

    public FlagWorldEditSel(Plugin worldEdit) {
        super(ImmutableList.of("we"));
        this.worldEdit = (WorldEditPlugin) worldEdit;
    }

    @Override
    public boolean acceptsSource(CommandSender sender) {
        return sender instanceof Player;
    }

    @Override
    public boolean acceptsValue(String value) {
        return true;
    }

    @Override
    public Optional<CompletableFuture<?>> process(QuerySession session, String flag, String value, Query query) {
        if (session.getSender() instanceof Player) {
            Player player = (Player) session.getSender();

            LocalSession localSession = worldEdit.getSession(player);

            RegionSelector selector = localSession.getRegionSelector(localSession.getSelectionWorld());

            if (selector == null) {
                player.sendMessage(Formatter.error("Please make a selection to use the flag " + flag));
                return Optional.empty();
            }

            try {
                Region region = selector.getRegion();
                if (region instanceof CuboidRegion) {
                    query.addCondition(fromSelection(region, player.getWorld()));
                    session.isIgnoredDefault(Omniscience.getParameterHandler("r").orElse(null));
                } else {
                    player.sendMessage(Formatter.error("Cannot use the flag " + flag + " with a non-cuboid region. Please make a cuboid selection."));
                }
            } catch (IncompleteRegionException ex) {
                player.sendMessage(Formatter.error("Cannot use the flag " + flag + " with an incomplete region. Please finish selecting your region."));
            }
        }

        return Optional.empty();
    }

    private SearchConditionGroup fromSelection(Region selection, World world) {
        SearchConditionGroup group = new SearchConditionGroup(SearchConditionGroup.Operator.AND);

        BlockVector3 maxPoint = selection.getMaximumPoint();
        BlockVector3 minPoint = selection.getMinimumPoint();

        Range<Integer> xRange = Range.open(minPoint.getBlockX(), maxPoint.getBlockX());
        group.add(FieldCondition.of(DataKeys.LOCATION.then(DataKeys.X), xRange));

        Range<Integer> yRange = Range.open(minPoint.getBlockY(), maxPoint.getBlockY());
        group.add(FieldCondition.of(DataKeys.LOCATION.then(DataKeys.Y), yRange));

        Range<Integer> zRange = Range.open(minPoint.getBlockZ(), maxPoint.getBlockZ());
        group.add(FieldCondition.of(DataKeys.LOCATION.then(DataKeys.Z), zRange));

        group.add(FieldCondition.of(DataKeys.LOCATION.then(DataKeys.WORLD), MatchRule.EQUALS, world.getUID().toString()));

        return group;
    }
}
