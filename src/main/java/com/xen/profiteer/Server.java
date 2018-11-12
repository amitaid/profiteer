package com.xen.profiteer;

import com.xen.profiteer.codec.JsonCodec;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.java.Log;

import java.util.ArrayList;

@Log
public class Server extends AbstractVerticle {

    private EventBus eb;

    @Override
    public void start() {
        eb = vertx.eventBus();
        eb.registerDefaultCodec(ArrayList.class, new JsonCodec<>(ArrayList.class));
        eb.registerDefaultCodec(Item.class, new JsonCodec<>(Item.class));
        eb.registerDefaultCodec(CraftedItem.class, new JsonCodec<>(CraftedItem.class));

        HttpServer httpServer = vertx.createHttpServer();
        Router router = Router.router(vertx);

        router.get("/").handler(req -> req.response().end("What's this OwO"));

        router.get("/item/:itemId").produces("application/json").handler(this::getItemDetails);

        router.get("/prof/:profName").handler(ctx -> {
            eb.<String>send(ItemCache.CHECK_PROFESSION_ITEMS, ctx.pathParam("profName"),
                    reply -> ctx.response().end(Json.encodePrettily(reply.result().body())));
        });

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


}
