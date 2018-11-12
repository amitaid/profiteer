package com.xen.profiteer;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
class CraftedItem {
    private String itemName;
    private String itemId;
    private final Map<String, Integer> ingredients = new HashMap<>();

    public CraftedItem(String itemId, String itemName) {
        this.itemId = itemId;
        this.itemName = itemName;
    }

    public void addIngredient(String itemId, int amount) {
        ingredients.put(itemId, amount);
    }

}
