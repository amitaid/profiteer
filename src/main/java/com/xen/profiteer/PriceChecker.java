package com.xen.profiteer;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
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

    private final Logger log = LoggerFactory.getLogger(PriceChecker.class);

    private static final String ITEM_PATH = TujApiPaths.BASE_PATH + TujApiPaths.ITEM_PATH;
    public static final String PRICE_CHECK = "PRICE_CHECK";

    private HttpClient client = HttpClient.newHttpClient();
    private EventBus eb;


    private Map<String, Item> itemDb = new HashMap<>();

    @Override
    public void start() {
        eb = vertx.eventBus();

        eb.<String>consumer(PRICE_CHECK, message -> {
            String itemId = message.body();
            log.debug("Price check for item id " + itemId);
            if (itemDb.containsKey(itemId)) {
                log.info("Item %s already in db, returning cached version", itemId);
                message.reply(itemDb.get(itemId));
            } else {
                String requestPath = ITEM_PATH + TujApiPaths.DEFAULT_REALM + "&item=" + itemId;
                HttpRequest request = HttpRequest.newBuilder(URI.create(requestPath)).GET().build();

                client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                        .thenApplyAsync(response -> {
                            if (response.statusCode() == 200) {
                                log.debug("Received reply for itemId " + itemId + ", " + response.body());
                                JsonObject jsonResponse = new JsonObject(response.body());
                                JsonObject stats = jsonResponse.getJsonArray("stats").getJsonObject(0);
                                String itemName = stats.getString("name_enus");
                                long price = stats.getInteger("price");
                                Item item = new Item(itemId, itemName, price);
                                log.debug(item.toString());

                                itemDb.put(itemId, item);
                                message.reply(JsonObject.mapFrom(item).toString());
                            } else {
                                log.error("Response code was " + response.statusCode());
                                message.reply(JsonObject.mapFrom(null));
                            }
                            return itemId;
                        });

            }

        });
    }

}
