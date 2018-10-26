package com.xen.profiteer;

public interface WowdbPaths {

    // Filters include: Item comes from BfA, Item is available, Item is BoE, Item is created by profession %s
    String PROFESSION_BASE = "https://www.wowdb.com/items?filter-expansion=8&filter-available=1&filter-bind=2&filter-crafted-with=%s";

    // Professions are unlikely to change between expansions, and this service was writtenspeci
    enum Professions {
        BLACKSMITHING("164"),
        LEATHERWORKING("165"),
        ALCHEMY("171"),
        COOKING("185"),
        TAILORING("197"),
        ENGINEERING("202"),
        ENCHANGING("333"),
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
