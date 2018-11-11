package com.xen.profiteer;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;

public class Main {

    public static void main(String[] args) {

        Vertx vertx = Vertx.vertx();

        vertx.deployVerticle(new PriceChecker(), new DeploymentOptions().setWorker(true));
        vertx.deployVerticle(new WowdbReader(), new DeploymentOptions().setWorker(true));
        vertx.deployVerticle(new Server());


    }

}
