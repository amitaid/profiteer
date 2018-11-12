package com.xen.profiteer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Item {

    private String itemId;
    private String itemName;
    private Long priceCoppers; // copper

}
