package io.github.warhead501.omniscience.api.query;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import io.github.warhead501.omniscience.api.flag.Flag;
import io.github.warhead501.omniscience.api.parameter.ParameterException;
import io.github.warhead501.omniscience.api.parameter.ParameterHandler;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class QuerySession {

    protected final CommandSender sender;
    protected final List<Flag> flags = Lists.newArrayList();
    protected Query query;
    protected int radius;
    protected Sort sortOrder = Sort.NEWEST_FIRST;
    protected List<ParameterHandler> ignoredDefaults = Lists.newArrayList();

    public QuerySession(CommandSender sender) {
        this.sender = sender;
    }

    public Query getQuery() {
        return query;
    }

    public void setQuery(Query query) {
        this.query = query;
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

    public void addIgnoredDefault(ParameterHandler handler) {
        ignoredDefaults.add(handler);
    }

    public boolean isIgnoredDefault(ParameterHandler handler) {
        return ignoredDefaults.contains(handler);
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

    public Query newQuery() {
        this.query = new Query();
        return query;
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
