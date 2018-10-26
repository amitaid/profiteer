package com.xen.profiteer;

import com.xen.profiteer.commons.ConfigItems;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpClient;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.util.List;

public class WowdbReader extends AbstractVerticle {

    private static String PROFESSION_BASE = "/spells/professions/%s?filter-expansion=8&filter-creates-item=1";
    private static final String WOWDB_HOST = "www.wowdb.com";
    private static final int SSL_PORT = 443;

    public static String BRING_ME = "a.shrubbery";

    private final Logger log = LoggerFactory.getLogger(WowdbReader.class);
    private HttpClient client;

    @Override
    public void start() {
        EventBus eb = vertx.eventBus();
        client = vertx.createHttpClient(ConfigItems.DEFAULT_CLIENT_OPTIONS
                .setDefaultHost(WOWDB_HOST).setDefaultPort(SSL_PORT));

        eb.<String>consumer(BRING_ME, message -> {
            String prof = message.body();
            String professionUrl = String.format(PROFESSION_BASE, prof);

            log.info("Retrieving " + professionUrl);
            client.getNow(professionUrl, response -> {
                response.bodyHandler(body -> message.reply(body.toString()));
            });

        });

    }

    private List<String> getItemsFromPage(String pageBody) {
        return null;
    }

    // Professions are unlikely to change between expansions,
    // and this service was written specifically for BfA
    enum Professions {
        BLACKSMITHING("164"),
        LEATHERWORKING("165"),
        ALCHEMY("171"),
        COOKING("185"),
        TAILORING("197"),
        ENGINEERING("202"),
        ENCHANTING("333"),
        JEWELCRAFTING("755"),
        INSCRIPTION("773");

        private String wowdbId;

        Professions(String wowdbId) {
            this.wowdbId = wowdbId;
        }

        @Override
        public String toString() {
            return this.wowdbId;
        }
    }
}
