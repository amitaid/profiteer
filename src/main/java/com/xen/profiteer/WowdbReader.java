package com.xen.profiteer;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.client.WebClient;
import org.apache.commons.text.StringEscapeUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class WowdbReader extends AbstractVerticle {

    // Professions search page. Filtered for BfA spells that create items.
    private static String PROFESSION_BASE = "/spells/professions/%s?filter-expansion=8&filter-creates-item=1";
    private static final String WOWDB_HOST = "www.wowdb.com";
    private static final int SSL_PORT = 443;

    private static final Pattern craftedItem =
            Pattern.compile("/items/(\\d+)-.*? t\".*?>(.+?)</a>", Pattern.MULTILINE | Pattern.DOTALL);
    private static final Pattern ingredients =
            Pattern.compile("<a href=.*?/items/(\\d+).*?overlay\">(\\d+).*?</a>");

    public static String BRING_ME = "a.shrubbery";

    private final Logger log = LoggerFactory.getLogger(WowdbReader.class);
    private WebClient client;

    @Override
    public void start() {
        EventBus eb = vertx.eventBus();
        client = WebClient.create(vertx);

        eb.<String>consumer(BRING_ME, message -> {
            String prof = message.body();
            String professionUri = String.format(PROFESSION_BASE, prof);

            log.info("Retrieving craftables for " + prof);

            client.get(SSL_PORT, WOWDB_HOST, professionUri).ssl(true).send(res -> {
                if (res.succeeded()) {
                    String response = getItemsFromPage(res.result().bodyAsString());
                    message.reply(response);
                } else {
                    log.error("Request error, {}, {}", res.result().statusCode(), professionUri);
                }
            });

        });

    }

    private CraftedItem getItemFromChunk(String chunk) {
        chunk = StringEscapeUtils.unescapeHtml4(chunk);

        Matcher nameMatcher = craftedItem.matcher(chunk);
        String id = null;
        String name = null;
        if (nameMatcher.find()) {
            id = nameMatcher.group(1);
            name = nameMatcher.group(2);
        } else {
            return null;
        }
        CraftedItem item = new CraftedItem(name, id);

        Matcher ingredientsMatcher = ingredients.matcher(chunk);
        while (ingredientsMatcher.find()) {
            item.addIngredient(ingredientsMatcher.group(1), Integer.parseInt(ingredientsMatcher.group(2)));
        }

        return item;
    }

    private String getItemsFromPage(String pageBody) {
        String list = pageBody.lines()
                .dropWhile(l -> !l.contains("<td class=\"col-name\">"))
                .takeWhile(l -> !l.contains("<div class=\"listing-footer\">"))
                .collect(Collectors.joining("\n"));
        String[] rows = list.split("<tr class=\"(?:even|odd)\">");
        log.info("chunks = " + rows.length);
        List<CraftedItem> items = Arrays.stream(rows)
                .filter(s -> !(s.contains("Rank 1") || s.contains("Rank 2") || s.contains("REUSE ME")))
                .skip(1)
                .map(this::getItemFromChunk)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        return items.stream().map(CraftedItem::toString).collect(Collectors.joining("\n"));

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
