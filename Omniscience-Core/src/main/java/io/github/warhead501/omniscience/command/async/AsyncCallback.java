package io.github.warhead501.omniscience.command.async;

import io.github.warhead501.omniscience.api.entry.DataEntry;

import java.util.List;

public interface AsyncCallback {

    void success(List<DataEntry> results);

    void empty();

    void error(Exception e);
}
