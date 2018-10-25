package com.xen.profiteer;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServer;

public class Server extends AbstractVerticle {

    private HttpServer httpServer;
    private EventBus eb;

    @Override
    public void start() throws Exception {
        eb = vertx.eventBus();
        httpServer = vertx.createHttpServer();
        httpServer.requestHandler(req -> req.response().end("What's this OwO"));

        httpServer.listen(8080);
    }




}
