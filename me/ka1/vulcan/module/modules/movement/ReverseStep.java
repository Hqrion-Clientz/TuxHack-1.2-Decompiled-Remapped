//Deobfuscated with https://github.com/PetoPetko/Minecraft-Deobfuscator3000 using mappings "1.12 stable mappings"!

// 
// Decompiled by Procyon v0.5.36
// 

package me.ka1.vulcan.module.modules.movement;

import net.minecraft.entity.Entity;
import me.ka1.vulcan.setting.Setting;
import me.ka1.vulcan.module.Module;

public class ReverseStep extends Module
{
    Setting.Double height;
    
    public ReverseStep() {
        super("ReverseStep", "Allows you to instantly step down blocks", Category.Movement);
    }
    
    @Override
    public void setup() {
        this.height = this.registerDouble("Height", "Height", 2.5, 0.5, 15.0);
    }
    
    @Override
    public int onUpdate() {
        if (ReverseStep.mc.world == null || ReverseStep.mc.player == null || ReverseStep.mc.player.isInWater() || ReverseStep.mc.player.isInLava() || ReverseStep.mc.player.isOnLadder() || ReverseStep.mc.gameSettings.keyBindJump.isKeyDown()) {
            return 0;
        }
        if (ReverseStep.mc.player != null && ReverseStep.mc.player.onGround && !ReverseStep.mc.player.isInWater() && !ReverseStep.mc.player.isOnLadder()) {
            for (double y = 0.0; y < this.height.getValue() + 0.5; y += 0.01) {
                if (!ReverseStep.mc.world.getCollisionBoxes((Entity)ReverseStep.mc.player, ReverseStep.mc.player.getEntityBoundingBox().offset(0.0, -y, 0.0)).isEmpty()) {
                    ReverseStep.mc.player.motionY = -10.0;
                    break;
                }
            }
        }
        return 0;
    }
}
