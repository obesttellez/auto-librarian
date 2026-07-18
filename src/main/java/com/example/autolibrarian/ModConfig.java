package com.example.autolibrarian;

import java.util.ArrayList;
import java.util.List;

public class ModConfig {
    public int tickDelay = 10;
    public int maxAttempts = 1000;
    public boolean confirmEachPurchase = true;
    public List<WishlistEntry> wishlist = new ArrayList<>();

    public ModConfig() {
        // Default wishlist entry if empty
        wishlist.add(new WishlistEntry("minecraft:mending", 1, 20, 10, false));
    }

    public static class WishlistEntry {
        public String enchantmentId;
        public int minLevel;
        public int maxPrice;
        public int priorityWeight;
        public boolean autoPurchase;

        public WishlistEntry(String id, int level, int price, int priority, boolean auto) {
            this.enchantmentId = id;
            this.minLevel = level;
            this.maxPrice = price;
            this.priorityWeight = priority;
            this.autoPurchase = auto;
        }
    }
}
