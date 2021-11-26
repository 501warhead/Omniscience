package io.github.warhead501.omniscience.io.mongo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.BulkWriteOptions;
import com.mongodb.client.model.InsertOneModel;
import com.mongodb.client.model.WriteModel;
import io.github.warhead501.omniscience.api.OmniApi;
import io.github.warhead501.omniscience.api.data.DataKey;
import io.github.warhead501.omniscience.api.data.DataKeys;
import io.github.warhead501.omniscience.api.data.DataWrapper;
import io.github.warhead501.omniscience.api.entry.DataAggregateEntry;
import io.github.warhead501.omniscience.api.entry.DataEntry;
import io.github.warhead501.omniscience.api.flag.Flag;
import io.github.warhead501.omniscience.api.query.*;
import io.github.warhead501.omniscience.api.util.DataHelper;
import io.github.warhead501.omniscience.api.util.DateUtil;
import io.github.warhead501.omniscience.OmniConfig;
import io.github.warhead501.omniscience.Omniscience;
import io.github.warhead501.omniscience.io.RecordHandler;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static com.google.common.base.Preconditions.checkNotNull;

public class MongoRecordHandler implements RecordHandler {
    private final BulkWriteOptions bulkWriteOptions = new BulkWriteOptions().ordered(false);

    private final MongoStorageHandler storageHandler;

    public MongoRecordHandler(MongoStorageHandler storageHandler) {
        this.storageHandler = storageHandler;
    }

    @Override
    public void write(List<DataWrapper> wrappers) {
        MongoCollection<Document> collection = MongoStorageHandler.getCollection(OmniConfig.INSTANCE.getTableName());

        List<WriteModel<Document>> documents = Lists.newArrayList();
        for (DataWrapper wrapper : wrappers) {
            Document document = documentFromDataWrapper(wrapper);

            document.append("Expires", DateUtil.parseTimeStringToDate(OmniConfig.INSTANCE.getRecordExpiry(), true));

            documents.add(new InsertOneModel<>(document));
        }

        collection.bulkWrite(documents, bulkWriteOptions);
    }

    @Override
    public CompletableFuture<List<DataEntry>> query(QuerySession session) throws Exception {
        Query query = session.getQuery();
        checkNotNull(query);

        if (session.hasFlag(Flag.NO_CHAT)) {
            query.addCondition(FieldCondition.of(DataKeys.MESSAGE, MatchRule.EXISTS, false));
        }

        List<DataEntry> entries = Lists.newArrayList();
        CompletableFuture<List<DataEntry>> future = new CompletableFuture<>();

        MongoCollection<Document> collection = MongoStorageHandler.getCollection(OmniConfig.INSTANCE.getTableName());

        Document matcher = new Document("$match", buildConditions(query.getSearchCriteria()));

        Document sortFields = new Document();
        sortFields.put(DataKeys.CREATED.toString(), session.getSortOrder().getSortVal());
        Document sorter = new Document("$sort", sortFields);

        Document limit = new Document("$limit", query.getSearchLimit());

        final AggregateIterable<Document> aggregated;
        if (!session.hasFlag(Flag.NO_GROUP)) {
            Document groupFields = new Document();
            groupFields.put(DataKeys.EVENT_NAME.toString(), "$" + DataKeys.EVENT_NAME);
            groupFields.put(DataKeys.PLAYER_ID.toString(), "$" + DataKeys.PLAYER_ID);
            groupFields.put(DataKeys.CAUSE.toString(), "$" + DataKeys.CAUSE);
            groupFields.put(DataKeys.TARGET.toString(), "$" + DataKeys.TARGET);

            groupFields.put(DataKeys.ENTITY_TYPE.toString(), "$" + DataKeys.ENTITY_TYPE);

            groupFields.put("dayOfMonth", new Document("$dayOfMonth", "$" + DataKeys.CREATED));
            groupFields.put("month", new Document("$month", "$" + DataKeys.CREATED));
            groupFields.put("year", new Document("$year", "$" + DataKeys.CREATED));

            Document groupHolder = new Document("_id", groupFields);
            groupHolder.put(DataKeys.COUNT.toString(), new Document("$sum", 1));

            Document group = new Document("$group", groupHolder);

            List<Document> pipeline = Lists.newArrayList();
            pipeline.add(matcher);
            pipeline.add(group);
            pipeline.add(sorter);
            pipeline.add(limit);

            aggregated = collection.aggregate(pipeline);
            Omniscience.logDebug("MongoDB Query: " + pipeline);
        } else {
            List<Document> pipeline = Lists.newArrayList();
            pipeline.add(matcher);
            pipeline.add(sorter);
            pipeline.add(limit);

            aggregated = collection.aggregate(pipeline);
            Omniscience.logDebug("MongoDB Query: " + pipeline);
        }

        try (MongoCursor<Document> cursor = aggregated.iterator()) {
            while (cursor.hasNext()) {
                Document wrapper = cursor.next();
                Document document = session.hasFlag(Flag.NO_GROUP) ? wrapper : (Document) wrapper.get("_id");
                DataWrapper internalWrapper = documentToDataWrapper(document);

                if (!session.hasFlag(Flag.NO_GROUP)) {
                    internalWrapper.set(DataKeys.COUNT, wrapper.get(DataKeys.COUNT.toString()));
                }

                DataEntry entry = DataEntry.from(document.get(DataKeys.EVENT_NAME.toString()).toString(), !session.hasFlag(Flag.NO_GROUP));

                if (entry instanceof DataAggregateEntry) {
                    Calendar calendar = GregorianCalendar.getInstance();
                    calendar.set(Calendar.YEAR, document.getInteger("year"));
                    calendar.set(Calendar.MONTH, document.getInteger("month") - 1); //Subtract 1 because it's 0 through 11 for the months
                    calendar.set(Calendar.DAY_OF_MONTH, document.getInteger("dayOfMonth"));
                    calendar.set(Calendar.SECOND, 0);
                    calendar.set(Calendar.MINUTE, 0);
                    calendar.set(Calendar.HOUR, 0);
                    calendar.set(Calendar.MILLISECOND, 0);
                    ((DataAggregateEntry) entry).setDate(calendar);
                }

                if (document.containsKey(DataKeys.PLAYER_ID.toString())) {
                    String uuid = document.getString(DataKeys.PLAYER_ID.toString());
                    OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(uuid));
                    if (player != null) {
                        internalWrapper.set(DataKeys.CAUSE, player.getName());
                    } else {
                        internalWrapper.set(DataKeys.CAUSE, uuid);
                    }
                }

                entry.data = internalWrapper;
                entries.add(entry);
            }
            future.complete(entries);
        }
        return future;
    }

    private Document documentFromDataWrapper(DataWrapper wrapper) {
        Document document = new Document();

        Set<DataKey> keys = wrapper.getKeys(false);
        for (DataKey dataKey : keys) {
            Optional<Object> oObject = wrapper.get(dataKey);
            oObject.ifPresent(object -> {
                String key = dataKey.asString(".");
                if (object instanceof List) {
                    List<Object> convertedList = Lists.newArrayList();
                    for (Object innerObject : (List<?>) object) {
                        if (innerObject instanceof DataWrapper) {
                            convertedList.add(documentFromDataWrapper((DataWrapper) innerObject));
                        } else if (DataHelper.isPrimitiveType(innerObject)) {
                            convertedList.add(innerObject);
                        } else if (object.getClass().isEnum()) {
                            convertedList.add(object.toString());
                        } else {
                            OmniApi.warning("Unsupported List Data Type: " + innerObject.getClass().getName());
                            OmniApi.warning("DataWrapper: " + wrapper);
                        }
                    }

                    if (!convertedList.isEmpty()) {
                        document.append(key, convertedList);
                    }
                } else if (object instanceof DataWrapper) {
                    DataWrapper subWrapper = (DataWrapper) object;
                    document.append(key, documentFromDataWrapper(subWrapper));
                } else {
                    if (key.equals(DataKeys.PLAYER_ID.toString())) {
                        document.append(DataKeys.PLAYER_ID.toString(), object);
                    } else {
                        document.append(key, object);
                    }
                }
            });
        }
        return document;
    }

    private DataWrapper documentToDataWrapper(Document document) {
        DataWrapper wrapper = DataWrapper.createNew();

        for (String key : document.keySet()) {
            DataKey dataKey = DataKey.of(key);
            Object object = document.get(key);

            if (object instanceof Document) {
                wrapper.set(dataKey, documentToDataWrapper((Document) object));
            } else if (object instanceof Collection) {
                wrapper.set(dataKey, ensureCorrectDataTypes((Collection) object));
            } else if (object instanceof Map) {
                wrapper.set(dataKey, ensureCorrectDataTypes((Map<?, ?>) object));
            } else {
                wrapper.set(dataKey, object);
            }
        }
        return wrapper;
    }

    private Collection<?> ensureCorrectDataTypes(Collection<?> collection) {
        ImmutableList.Builder<Object> listBuilder = ImmutableList.builder();

        for (Object value : collection) {
            if (value instanceof Document) {
                listBuilder.add(documentToDataWrapper((Document) value));
            } else if (value instanceof Collection) {
                listBuilder.add(ensureCorrectDataTypes((Collection) value));
            } else if (value instanceof Map) {
                listBuilder.add(ensureCorrectDataTypes((Map<?, ?>) value));
            } else {
                listBuilder.add(value);
            }
        }

        return listBuilder.build();
    }

    private Map<?, ?> ensureCorrectDataTypes(Map<?, ?> map) {
        ImmutableMap.Builder<Object, Object> mapBuilder = ImmutableMap.builder();

        map.forEach((key, value) -> {
            if (value instanceof Map) {
                mapBuilder.put(key, ensureCorrectDataTypes((Map) value));
            } else if (value instanceof Document) {
                mapBuilder.put(key, documentToDataWrapper((Document) value));
            } else if (value instanceof Collection) {
                mapBuilder.put(key, ensureCorrectDataTypes((Collection) value));
            } else {
                mapBuilder.put(key, value);
            }
        });

        return mapBuilder.build();
    }

    private Document buildConditions(List<SearchCondition> conditions) {
        Document filter = new Document();

        for (SearchCondition condition : conditions) {
            if (condition instanceof SearchConditionGroup) {
                SearchConditionGroup group = (SearchConditionGroup) condition;
                Document subFilter = buildConditions(group.getConditions());

                if (group.getOperator().equals(SearchConditionGroup.Operator.OR)) {
                    filter.append("$or", subFilter);
                } else {
                    filter.putAll(subFilter);
                }
            } else {
                FieldCondition field = (FieldCondition) condition;

                Document matcher;
                if (filter.containsKey(field.getField().toString())) {
                    matcher = (Document) filter.get(field.getField().toString());
                } else {
                    matcher = new Document();
                }

                if (field.getValue() instanceof List) {
                    matcher.append(field.getRule().equals(MatchRule.INCLUDES) ? "$in" : "$nin", field.getValue());
                    filter.put(field.getField().toString(), matcher);
                } else if (field.getRule().equals(MatchRule.EXISTS)) {
                    matcher.append("$exists", field.getValue());
                    filter.put(field.getField().toString(), matcher);
                } else if (field.getRule().equals(MatchRule.EQUALS)) {
                    filter.put(field.getField().toString(), field.getValue());
                } else if (field.getRule().equals(MatchRule.GREATER_THAN_EQUAL)) {
                    matcher.append("$gte", field.getValue());
                    filter.put(field.getField().toString(), matcher);
                } else if (field.getRule().equals(MatchRule.LESS_THAN_EQUAL)) {
                    matcher.append("$lte", field.getValue());
                    filter.put(field.getField().toString(), matcher);
                } else if (field.getRule().equals(MatchRule.BETWEEN)) {
                    if (!(field.getValue() instanceof Range)) {
                        throw new IllegalArgumentException("Between matcher requires a value range");
                    }

                    Range<?> range = (Range<?>) field.getValue();

                    Document between = new Document("$gte", range.lowerEndpoint()).append("$lte", range.upperEndpoint());
                    filter.put(field.getField().toString(), between);
                }
            }
        }
        return filter;
    }
}
