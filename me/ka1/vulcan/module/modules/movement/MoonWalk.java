//Deobfuscated with https://github.com/PetoPetko/Minecraft-Deobfuscator3000 using mappings "1.12 stable mappings"!

// 
// Decompiled by Procyon v0.5.36
// 

package me.ka1.vulcan.module.modules.movement;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.block.material.Material;
import me.ka1.vulcan.setting.Setting;
import me.ka1.vulcan.module.Module;

public class MoonWalk extends Module
{
    Setting.Double poop;
    
    public MoonWalk() {
        super("MoonWalk", "old rusherhack modules", Category.Movement);
    }
    
    @Override
    public void setup() {
        this.poop = this.registerDouble("slipperiness", "poop", 69.0, 6.9, 420.0);
    }
    
    @Override
    public int onUpdate() {
        if (MoonWalk.mc.player.onGround && MoonWalk.mc.gameSettings.keyBindJump.isPressed()) {
            MoonWalk.mc.player.motionY = 0.25;
        }
        else if (MoonWalk.mc.player.isAirBorne && !MoonWalk.mc.player.isInWater() && !MoonWalk.mc.player.isOnLadder() && !MoonWalk.mc.player.isInsideOfMaterial(Material.LAVA)) {
            MoonWalk.mc.player.motionY = 1.0E-6;
            final EntityPlayerSP player = MoonWalk.mc.player;
            player.jumpMovementFactor *= 1.21337f;
        }
        return 0;
    }
}
