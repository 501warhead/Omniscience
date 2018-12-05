package net.lordofthecraft.omniscience.api.entry;

import com.google.common.collect.Lists;
import net.lordofthecraft.omniscience.Omniscience;
import net.lordofthecraft.omniscience.api.data.DataKeys;
import net.lordofthecraft.omniscience.api.data.DataWrapper;

import java.util.List;

public final class EntryQueueRunner implements Runnable {

    @Override
    public void run() {
        List<DataWrapper> batchWrappers = Lists.newArrayList();

        while (!EntryQueue.getQueue().isEmpty()) {
            DataWrapper wrapper = EntryQueue.getQueue().poll();
            if (wrapper != null) {
                System.out.println("We're now saving the event: " + wrapper.getString(DataKeys.EVENT_NAME).orElse("Unknown"));
                System.out.println("Data: " + wrapper);
                batchWrappers.add(wrapper);
            }
        }

        if (batchWrappers.size() > 0) {
            try {
                Omniscience.getStorageHandler().records().write(batchWrappers);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
