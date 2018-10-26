package com.xen.profiteer;

import io.vertx.core.Vertx;

public class Main {

    public static void main(String[] args) {

        Vertx vertx = Vertx.vertx();
        Server server = new Server();

        vertx.deployVerticle(server);
        vertx.deployVerticle(new PriceChecker());
        vertx.deployVerticle(new WowdbReader());


    }

}
