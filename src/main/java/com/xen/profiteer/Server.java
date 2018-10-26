package com.xen.profiteer;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServer;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class Server extends AbstractVerticle {

    private final Logger log = LoggerFactory.getLogger(Server.class);


    private EventBus eb;
    private Router router = Router.router(vertx);

    @Override
    public void start() {
        eb = vertx.eventBus();
        HttpServer httpServer = vertx.createHttpServer();

        router.get("/").handler(req -> req.response().end("What's this OwO"));

        router.get("/item/:itemId").produces("application/json").handler(this::getItemDetails);

        router.get("/prof/:profName").handler(ctx -> {
            eb.<String>send(WowdbReader.BRING_ME, ctx.pathParam("profName"), reply -> {
                ctx.response().end(reply.result().body());
            });
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

        eb.<String>send(PriceChecker.PRICE_CHECK, itemId, reply -> {
            if (reply.succeeded()) {
                String result = reply.result().body();
                log.debug("Got reply " + result);
                ctx.response().end(result);
            } else {
                ctx.response().setStatusCode(500).end("Something bad happened");
            }
        });
    }


}
