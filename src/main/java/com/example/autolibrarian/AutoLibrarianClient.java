package com.example.autolibrarian;

import com.example.autolibrarian.ui.AutoLibrarianHud;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.village.MerchantOfferList;
import net.minecraft.client.gui.screen.ingame.MerchantScreen;

public class AutoLibrarianClient implements ClientModInitializer {
    private static KeyBinding toggleKey;
    private static boolean active = false;
    public static boolean paused = false;
    
    private BlockPos targetLectern = null;
    private int waitTicks = 0;
    private int attempts = 0;
    
    private enum State { IDLE, BREAKING, WAITING_BREAK, PLACING, WAITING_VILLAGER, CHECKING_OFFERS }
    private State currentState = State.IDLE;

    @Override
    public void onInitializeClient() {
        ConfigManager.init();
        AutoLibrarianHud.register();
        
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.autolibrarian.toggle",
            InputUtil.Type.KEYSYM,
            org.lwjgl.glfw.GLFW.GLFW_KEY_R,
            "category.autolibrarian.main"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
    }

    private void onTick(MinecraftClient client) {
        while (toggleKey.wasPressed()) {
            active = !active;
            if (active) startLoop(client);
            else stopLoop(client);
        }

        if (!active || client.player == null) return;

        if (!ValidationUtil.isLookingAtTargetBlock(client, targetLectern) && currentState != State.WAITING_VILLAGER) {
            paused = true;
            return; 
        }
        paused = false;

        if (waitTicks > 0) {
            waitTicks--;
            return;
        }

        switch (currentState) {
            case BREAKING:
                client.interactionManager.updateBlockBreakingProgress(targetLectern, client.player.getMovementDirection());
                if (client.world.isAir(targetLectern)) {
                    currentState = State.WAITING_BREAK;
                    waitTicks = ConfigManager.getConfig().tickDelay;
                }
                break;
                
            case WAITING_BREAK:
                if (!ValidationUtil.hasLecternInInventory(client)) {
                    client.player.sendMessage(Text.literal("§cOut of lecterns!"), false);
                    stopLoop(client);
                    return;
                }
                client.interactionManager.interactBlock(client.player, client.player.getActiveHand(), 
                    new net.minecraft.util.hit.BlockHitResult(client.player.getPos(), client.player.getHorizontalFacing(), targetLectern, false));
                currentState = State.WAITING_VILLAGER;
                waitTicks = 40; 
                break;
                
            case WAITING_VILLAGER:
                if (client.currentScreen instanceof MerchantScreen) {
                    currentState = State.CHECKING_OFFERS;
                }
                break;
                
            case CHECKING_OFFERS:
                MerchantScreen screen = (MerchantScreen) client.currentScreen;
                MerchantOfferList offers = screen.getScreenHandler().getRecipes();
                
                ModConfig.WishlistEntry match = ConfigManager.findMatch(offers);
                if (match != null) {
                    client.player.sendMessage(Text.literal("§aMatch found: " + match.enchantmentId + " under " + match.maxPrice + " emeralds!"), false);
                    stopLoop(client);
                } else {
                    client.player.closeHandledScreen();
                    attempts++;
                    if (attempts >= ConfigManager.getConfig().maxAttempts) {
                        client.player.sendMessage(Text.literal("§cMax attempts reached."), false);
                        stopLoop(client);
                    } else {
                        currentState = State.BREAKING;
                    }
                }
                break;
        }
    }

    private void startLoop(MinecraftClient client) {
        if (client.crosshairTarget != null && client.crosshairTarget.getType() == net.minecraft.util.hit.HitResult.Type.BLOCK) {
            targetLectern = ((net.minecraft.util.hit.BlockHitResult) client.crosshairTarget).getBlockPos();
            attempts = 0;
            currentState = State.BREAKING;
            client.player.sendMessage(Text.literal("§aReroll loop started."), false);
        } else {
            client.player.sendMessage(Text.literal("§cMust be looking at a lectern to start."), false);
            active = false;
        }
    }

    private void stopLoop(MinecraftClient client) {
        active = false;
        paused = false;
        targetLectern = null;
        currentState = State.IDLE;
    }
}
