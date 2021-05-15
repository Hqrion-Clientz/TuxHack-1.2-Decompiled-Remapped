//Deobfuscated with https://github.com/PetoPetko/Minecraft-Deobfuscator3000 using mappings "1.12 stable mappings"!

// 
// Decompiled by Procyon v0.5.36
// 

package me.ka1.vulcan.module.modules.player;

import net.minecraft.init.Items;
import me.ka1.vulcan.module.Module;

public class FastEXP extends Module
{
    public FastEXP() {
        super("FastEXP", "Allows you to place exp at a higher rate", Category.Player);
    }
    
    @Override
    public int onUpdate() {
        if (FastEXP.mc.player == null || FastEXP.mc.world == null) {
            return 0;
        }
        if (FastEXP.mc.player.inventory.getStackInSlot(FastEXP.mc.player.inventory.currentItem).getItem() == Items.EXPERIENCE_BOTTLE) {
            return FastEXP.mc.rightClickDelayTimer = 0;
        }
        return 0;
    }
}
