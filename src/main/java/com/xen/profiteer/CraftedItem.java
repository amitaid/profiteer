package com.xen.profiteer;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
class CraftedItem {
    private String itemName;
    private String itemId;
    private final Map<String, Integer> ingredients = new HashMap<>();

    public CraftedItem(String itemName, String itemId) {
        this.itemName = itemName;
        this.itemId = itemId;
    }

    public void addIngredient(String id, int amount) {
        ingredients.put(id, amount);
    }

}
