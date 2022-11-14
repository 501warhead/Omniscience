package io.github.warhead501.omniscience.command.util;

import io.github.warhead501.omniscience.api.entry.DataEntry;
import io.github.warhead501.omniscience.api.query.QuerySession;
import io.github.warhead501.omniscience.api.util.Formatter;
import io.github.warhead501.omniscience.OmniConfig;
import io.github.warhead501.omniscience.Omniscience;
import io.github.warhead501.omniscience.command.async.AsyncCallback;
import org.bukkit.Bukkit;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class Async {

    public static void lookup(final QuerySession session, AsyncCallback callback) {
        session.getQuery().setSearchLimit(OmniConfig.INSTANCE.getLookupSizeLimit());
        Bukkit.getScheduler().runTaskAsynchronously(Omniscience.getProvidingPlugin(Omniscience.class), () -> {
            try {
                CompletableFuture<List<DataEntry>> future = Omniscience.getStorageHandler().records().query(session);
                future.thenAccept(results -> {
                    try {
                        if (results.isEmpty()) {
                            callback.empty();
                        } else {
                            callback.success(results);
                        }
                    } catch (Exception e) {
                        session.getSender().sendMessage(Formatter.error(e.getMessage()));
                        e.printStackTrace();
                    }
                });
            } catch (Exception e) {
                callback.error(e);
                e.printStackTrace();
            }
        });
    }
}
