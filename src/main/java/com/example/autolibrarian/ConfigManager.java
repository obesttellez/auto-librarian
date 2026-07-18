package com.example.autolibrarian;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.village.MerchantOffer;
import net.minecraft.village.MerchantOfferList;
import net.minecraft.item.ItemStack;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.Registries;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Map;

public class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = new File(FabricLoader.getInstance().getConfigDir().toFile(), "auto_librarian.json");
    private static ModConfig currentConfig = new ModConfig();

    public static void init() {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                currentConfig = GSON.fromJson(reader, ModConfig.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            save();
        }
    }

    public static void save() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(currentConfig, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static ModConfig getConfig() {
        return currentConfig;
    }

    public static ModConfig.WishlistEntry findMatch(MerchantOfferList offers) {
        for (MerchantOffer offer : offers) {
            ItemStack sellItem = offer.getSellItem();
            if (sellItem.getItem() instanceof EnchantedBookItem) {
                Map<Enchantment, Integer> enchantments = EnchantmentHelper.get(sellItem);
                for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                    String enchantId = Registries.ENCHANTMENT.getId(entry.getKey()).toString();
                    int level = entry.getValue();
                    int price = offer.getOriginalFirstBuyItem().getCount();

                    for (ModConfig.WishlistEntry wishlistEntry : currentConfig.wishlist) {
                        if (wishlistEntry.enchantmentId.equals(enchantId) &&
                            level >= wishlistEntry.minLevel &&
                            price <= wishlistEntry.maxPrice) {
                            return wishlistEntry;
                        }
                    }
                }
            }
        }
        return null;
    }
}
