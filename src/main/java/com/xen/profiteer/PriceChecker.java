package com.xen.profiteer;

import com.google.common.util.concurrent.RateLimiter;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import lombok.extern.java.Log;

@Log
public class PriceChecker extends AbstractVerticle {

    private static final String HOST_NAME = "theunderminejournal.com";
    private static final int PORT = 443;
    private static final String BASE_PATH = "/api/";
    private static final String ITEM_PATH = "item.php?";
    private static final String DEFAULT_REALM = "house=231"; // Ragnaros EU


    private static final String STATS = "stats";
    private static final String NAME = "name_enus";
    private static final String PRICE = "price";

    // This Verticle's functionality
    public static final String GET_ITEM_PRICE = "GET_ITEM_PRICE";
    public static final String GET_ITEMS_PRICES = "GET_ITEMS_PRICES";

    private WebClient client;

    private final int REQUESTS_PER_SECOND = 10;
    private final RateLimiter limiter = RateLimiter.create(REQUESTS_PER_SECOND);

    @Override
    public void start() {
        EventBus eb = vertx.eventBus();
        client = WebClient.create(vertx);

        eb.<String>consumer(GET_ITEM_PRICE, message -> {
            limiter.acquire();
            String itemId = message.body();
            log.info("Price check for item id " + itemId);

            String requestPath = BASE_PATH + ITEM_PATH + DEFAULT_REALM + "&item=" + itemId;
            client.get(PORT, HOST_NAME, requestPath).ssl(true).send(response -> {
                if (response.succeeded()) {
                    log.info(String.format("Response for item %s was %s", itemId, response.result().bodyAsString()));
                    JsonObject jsonResponse = response.result().bodyAsJsonObject();
                    JsonArray statsArray = jsonResponse.getJsonArray(STATS);
                    if (statsArray == null || statsArray.size() == 0) {
                        message.fail(500, "There were no item stats");
                    } else {
                        JsonObject stats = statsArray.getJsonObject(0);
                        String itemName = stats.getString(NAME);
                        Long price = stats.getLong(PRICE);
                        Item item = new Item(itemId, itemName, price);
                        log.info(item.toString());

                        message.reply(item);
                    }
                } else {
                    log.severe("Response code was " + response.result().statusCode());
                    message.fail(500, "The server had a stroke while processing your bullshit");
                }
            });


        });
    }

}
