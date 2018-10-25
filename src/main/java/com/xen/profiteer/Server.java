package com.xen.profiteer;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServer;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;

public class Server extends AbstractVerticle {

    private final Logger log = LoggerFactory.getLogger(Server.class);


    private EventBus eb;
    private Router router = Router.router(vertx);

    @Override
    public void start() {
        eb = vertx.eventBus();
        HttpServer httpServer = vertx.createHttpServer();

        router.get("/").handler(req -> req.response().end("What's this OwO"));

        router.get("/item/:itemId").produces("application/json").handler(ctx ->
                eb.<String>send(PriceChecker.PRICE_CHECK, ctx.pathParam("itemId"), reply -> {
                    log.debug("Got reply " + reply.result().body());
                    ctx.response().end(reply.result().body());
                }));

        httpServer.requestHandler(router::accept).listen(8080);
    }


}
