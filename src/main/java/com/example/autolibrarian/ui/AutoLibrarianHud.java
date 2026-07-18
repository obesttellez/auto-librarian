package com.example.autolibrarian.ui;

import com.example.autolibrarian.AutoLibrarianClient;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public class AutoLibrarianHud implements HudRenderCallback {
    public static void register() {
        HudRenderCallback.EVENT.register(new AutoLibrarianHud());
    }

    @Override
    public void onHudRender(DrawContext drawContext, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.options.hudHidden) return;

        if (AutoLibrarianClient.paused) {
            int width = client.getWindow().getScaledWidth();
            int height = client.getWindow().getScaledHeight();
            
            String text = "Paused — look at the lectern to continue";
            int textWidth = client.textRenderer.getWidth(text);
            
            drawContext.drawTextWithShadow(client.textRenderer, text, (width - textWidth) / 2, height / 4, 0xFF5555);
        }
    }
}
