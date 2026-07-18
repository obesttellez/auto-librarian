package com.example.autolibrarian;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

public class ValidationUtil {
    public static boolean isLookingAtTargetBlock(MinecraftClient client, BlockPos targetPos) {
        if (client.player == null || client.world == null || client.interactionManager == null || targetPos == null) {
            return false;
        }

        double reach = client.interactionManager.getReachDistance();
        HitResult hit = client.cameraEntity.raycast(reach, 1.0F, false);
        
        if (hit.getType() == HitResult.Type.BLOCK) {
            BlockPos hitPos = ((BlockHitResult) hit).getBlockPos();
            return hitPos.equals(targetPos);
        }
        
        return false;
    }
    
    public static boolean hasLecternInInventory(MinecraftClient client) {
        return client.player.getInventory().contains(net.minecraft.item.Items.LECTERN.getDefaultStack());
    }
}
