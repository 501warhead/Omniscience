package io.github.warhead501.omniscience.io;

import io.github.warhead501.omniscience.api.data.DataWrapper;
import io.github.warhead501.omniscience.api.entry.DataEntry;
import io.github.warhead501.omniscience.api.query.QuerySession;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface RecordHandler {

    void write(List<DataWrapper> wrappers);

    CompletableFuture<List<DataEntry>> query(QuerySession session) throws Exception;

}
