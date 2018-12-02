package net.lordofthecraft.omniscience.api.query;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.lordofthecraft.omniscience.api.flag.Flag;
import net.lordofthecraft.omniscience.api.parameter.ParameterException;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class QuerySession {

    protected final CommandSender sender;
    protected final List<Flag> flags = Lists.newArrayList();
    protected Query query;
    protected int radius;
    protected Sort sortOrder = Sort.NEWEST_FIRST;

    public QuerySession(CommandSender sender) {
        this.sender = sender;
    }

    public Query getQuery() {
        return query;
    }

    public ImmutableList<Flag> getFlags() {
        return ImmutableList.copyOf(flags);
    }

    public void addFlag(Flag flag) {
        flags.add(flag);
    }

    public boolean hasFlag(Flag flag) {
        return flags.contains(flag);
    }

    public void clearFlags() {
        flags.clear();
    }

    public Sort getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Sort sortOrder) {
        this.sortOrder = sortOrder;
    }

    public CommandSender getSender() {
        return sender;
    }

    public void setQuery(Query query) {
        this.query = query;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public CompletableFuture<Void> newQueryFromArguments(String[] arguments) throws ParameterException {
        CompletableFuture<Query> future = QueryBuilder.fromArguments(this, arguments);
        return future.thenAccept(query -> this.query = query);
    }

    public enum Sort {
        NEWEST_FIRST(-1, "DESC"),
        OLDEST_FIRST(1, "ASC");

        private String sortString;
        private int sortVal;

        Sort(int sortVal, String sortString) {
            this.sortVal = sortVal;
            this.sortString = sortString;
        }

        public int getSortVal() {
            return sortVal;
        }

        public String getSortString() {
            return sortString;
        }
    }
}
