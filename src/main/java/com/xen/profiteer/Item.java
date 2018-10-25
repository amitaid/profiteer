package com.xen.profiteer;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Item {

    private String itemId;
    private String itemName;
    private long currentPrice; // copper

}
