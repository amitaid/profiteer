package com.xen.profiteer;

import io.vertx.core.Vertx;

public class Main {

    public static void main(String[] args) {

        Vertx vertx = Vertx.vertx();

        vertx.deployVerticle(new PriceChecker());
        vertx.deployVerticle(new WowdbReader());
        vertx.deployVerticle(new Server());


    }

}
