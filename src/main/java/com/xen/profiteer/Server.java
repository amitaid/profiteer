package com.xen.profiteer;

import com.xen.profiteer.codec.JsonCodec;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.java.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Log
public class Server extends AbstractVerticle {

    private EventBus eb;

    @Override
    public void start() {
        eb = vertx.eventBus();

        vertx.deployVerticle(new PriceChecker(), new DeploymentOptions().setWorker(true));
        vertx.deployVerticle(new WowdbReader(), new DeploymentOptions().setWorker(true));
        vertx.deployVerticle(new ItemCache());
        eb.registerDefaultCodec(ArrayList.class, new JsonCodec<>(ArrayList.class));
        eb.registerDefaultCodec(HashMap.class, new JsonCodec<>(HashMap.class));
        eb.registerDefaultCodec(Item.class, new JsonCodec<>(Item.class));
        eb.registerDefaultCodec(CraftedItem.class, new JsonCodec<>(CraftedItem.class));

        HttpServer httpServer = vertx.createHttpServer();
        Router router = Router.router(vertx);

        router.get("/").handler(req -> req.response().end("What's this OwO"));

        router.get("/item/:itemId").produces("application/json").handler(this::getItemDetails);

        router.get("/prof/:profName").handler(this::getCraftedItemsForProf);

        router.get("/profit/:profName").handler(this::getProfitableItems);

        httpServer.requestHandler(router::accept).listen(8080);
    }

    private void getItemDetails(RoutingContext ctx) {
        String itemId = ctx.pathParam("itemId");
        try {
            Integer.parseInt(itemId);
        } catch (NumberFormatException e) {
            ctx.response().setStatusCode(500).end();
        }

        eb.<Item>send(ItemCache.CHECK_ITEM_PRICE, itemId, reply -> {
            log.info("Getting item " + itemId);
            Item result = reply.result().body();
            ctx.response().end(Json.encodePrettily(result));

        });
    }


    private void getCraftedItemsForProf(RoutingContext ctx) {
        eb.<List<CraftedItem>>send(ItemCache.CHECK_PROFESSION_ITEMS, ctx.pathParam("profName"), reply ->
                ctx.response().end(Json.encodePrettily(reply.result().body())));
    }

    private void cacheProfession(RoutingContext ctx) {
        eb.<List<CraftedItem>>send(ItemCache.CHECK_PROFESSION_ITEMS, ctx.pathParam("profName"), reply -> {
            var items = reply.result().body();
            List<String> itemIds = items.stream()
                    .flatMap(i -> i.getIngredients().keySet().stream())
                    .distinct()
                    .collect(Collectors.toList());
            eb.<Item>send(ItemCache.CHECK_ITEM_PRICE, itemIds.get(0), itemMsg -> {
                ctx.response().end(itemMsg.result().body().toString());
            });
        });
    }

    private void getProfitableItems(RoutingContext ctx) {
        eb.<List<CraftedItem>>send(ItemCache.CHECK_PROFESSION_ITEMS, ctx.pathParam("profName"), reply -> {
            var items = reply.result().body();
            List<String> itemIds = items.stream()
                    .flatMap(i -> i.getIngredients().keySet().stream())
                    .distinct()
                    .collect(Collectors.toList());

            List<Future> futures = new ArrayList<>();

            for (String itemId : itemIds) {
                Future f = Future.future();
                eb.<Item>send(ItemCache.CHECK_ITEM_PRICE, itemId, res -> f.complete(res.result().body()));
                futures.add(f);
            }

            CompositeFuture.all(futures).setHandler(res -> {
                if (res.succeeded()) {
                    log.info("All items returned results.");
                    Map<String, Long> itemPrices = futures.stream()
                            .map(f -> ((Message<Item>) f.result()).body())
                            .collect(Collectors.toMap(Item::getItemId, Item::getPriceCoppers));

                    Map<CraftedItem, Long> profitableItems = new HashMap<>();

                    for (CraftedItem item : items) {
                        long craftingCost = item.getIngredients().entrySet().stream()
                                .mapToLong(e -> itemPrices.get(e.getKey()) * e.getValue()).sum();
                        if (itemPrices.get(item.getItemId()) > craftingCost) {
                            profitableItems.put(item, itemPrices.get(item.getItemId()) - craftingCost);
                        }
                    }

                    ctx.response().end(Json.encodePrettily(profitableItems));
                } else {
                    ctx.response().end("Failed somewhere along the futures.");
                }
            });

        });
    }
}
