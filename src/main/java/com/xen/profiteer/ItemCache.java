package com.xen.profiteer;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import lombok.extern.java.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log
public class ItemCache extends AbstractVerticle {

    private final Map<String, List<CraftedItem>> craftedItems = new HashMap<>(); //TODO enum of profs
    private final Map<String, Item> itemPrices = new HashMap<>(); //TODO add update date and optimize

    // functionality
    public static final String CHECK_PROFESSION_ITEMS = "read.profession.items";
    public static final String CHECK_ITEM_PRICE = "check.item.price";

    @Override
    public void start() {
        EventBus eb = vertx.eventBus();

        eb.<String>consumer(CHECK_ITEM_PRICE, message -> {
            String itemId = message.body();
            if (itemPrices.containsKey(itemId)) {
                log.info(String.format("Already had item %s, returning cached version", itemId));
                message.reply(itemPrices.get(itemId));
            } else {
                log.info(String.format("Item %s not in cache, retrieving", itemId));

                eb.<Item>send(PriceChecker.GET_ITEM_PRICE, itemId, reply -> {
                    Item item = reply.result().body();
                    itemPrices.put(itemId, item);
                    message.reply(item);
                });
            }
        });

        eb.<String>consumer(CHECK_PROFESSION_ITEMS, message -> {
            String profession = message.body();
            if (craftedItems.containsKey(profession)) {
                message.reply(craftedItems.get(profession));
            } else {
                eb.<List<CraftedItem>>send(WowdbReader.BRING_ME, profession, reply -> {
                    craftedItems.put(profession, reply.result().body());
                    message.reply(reply.result().body());
                });
            }
        });


    }
}
