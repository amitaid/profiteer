package com.xen.profiteer;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;

import java.util.List;

public class WowDbScraper extends AbstractVerticle {

    private static String BRING_ME = "a.shrubbery";

    private EventBus eb;

    @Override
    public void start() {
        this.eb = vertx.eventBus();


    }

    private List<Item> getItemsList() {
        return null;
    }
}
