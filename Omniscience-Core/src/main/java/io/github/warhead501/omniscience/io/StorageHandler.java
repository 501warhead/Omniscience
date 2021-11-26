package io.github.warhead501.omniscience.io;

import io.github.warhead501.omniscience.Omniscience;

public interface StorageHandler {

    boolean connect(Omniscience omniscience) throws Exception;

    RecordHandler records();

    void close();
}
