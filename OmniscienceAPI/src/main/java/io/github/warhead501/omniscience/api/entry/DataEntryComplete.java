package io.github.warhead501.omniscience.api.entry;

import io.github.warhead501.omniscience.api.OmniApi;
import io.github.warhead501.omniscience.api.util.DateUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

import static io.github.warhead501.omniscience.api.data.DataKeys.CREATED;

public class DataEntryComplete extends DataEntry {
    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(OmniApi.getDateFormat());

    public String getRelativeTime() {
        Optional<Object> date = data.get(CREATED);
        return date.map(time -> {
            Date created = null;
            if (time instanceof Date) {
                created = (Date) time;
            } else if (time instanceof Long) {
                created = new Date(((Long) time) * 1000);
            }

            if (created != null) {
                return DateUtil.getTimeSince(created);
            } else {
                return "";
            }
        }).orElse(null);
    }

    public String getTime() {
        Optional<Object> date = data.get(CREATED);
        return date.map(time -> {
            if (time instanceof Date) {
                return simpleDateFormat.format((Date) time);
            } else if (time instanceof Long) {
                return simpleDateFormat.format(new Date(((Long) time) * 1000));
            }
            return null;
        }).orElse(null);
    }
}
