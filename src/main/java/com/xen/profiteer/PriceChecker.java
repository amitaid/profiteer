package com.xen.profiteer;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

public class PriceChecker extends AbstractVerticle {

    public static final String STATS = "stats";
    public static final String NAME = "name_enus";
    public static final String PRICE = "price";
    private final Logger log = LoggerFactory.getLogger(PriceChecker.class);

    private static final String ITEM_PATH = TujApiPaths.BASE_PATH + TujApiPaths.ITEM_PATH;

    // This Verticle's functionality
    public static final String PRICE_CHECK = "PRICE_CHECK";

    private final HttpClient client = HttpClient.newHttpClient();
    private final Map<String, Item> itemDb = new HashMap<>();

    @Override
    public void start() {
        EventBus eb = vertx.eventBus();

        eb.<String>consumer(PRICE_CHECK, message -> {
            String itemId = message.body();
            log.info("Price check for item id " + itemId);

            if (itemDb.containsKey(itemId)) {
                log.debug(String.format("Item %s already in db, returning cached version", itemId));
                message.reply(JsonObject.mapFrom(itemDb.get(itemId)).toString());
            } else {
                log.debug(String.format("Item %s not in db, returning cached version", itemId));
                String requestPath = ITEM_PATH + TujApiPaths.DEFAULT_REALM + "&item=" + itemId;
                HttpRequest request = HttpRequest.newBuilder(URI.create(requestPath)).GET().build();

                // Using Java basic client because I'm getting a netty error when using the vertx one
                client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                        .thenApplyAsync(response -> {
                            if (response.statusCode() == 200) {
                                log.debug("Received reply for itemId " + itemId + ", " + response.body());
                                JsonObject jsonResponse = new JsonObject(response.body());
                                JsonArray statsArray = jsonResponse.getJsonArray(STATS);
                                if (statsArray.size() == 0) {
                                    message.fail(500, "There were no item stats");
                                } else {
                                    JsonObject stats = statsArray.getJsonObject(0);
                                    String itemName = stats.getString(NAME);
                                    Long price = stats.getLong(PRICE);
                                    Item item = new Item(itemId, itemName, price);
                                    log.debug(item.toString());

                                    itemDb.put(itemId, item);
                                    message.reply(JsonObject.mapFrom(item).toString());
                                }
                            } else {
                                log.error("Response code was " + response.statusCode());
                                message.fail(500, "The server had a stroke while processing your bullshit");
                            }
                            return itemId;
                        });

            }

        });
    }

}
