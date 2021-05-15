//Deobfuscated with https://github.com/PetoPetko/Minecraft-Deobfuscator3000 using mappings "1.12 stable mappings"!

// 
// Decompiled by Procyon v0.5.36
// 

package me.ka1.vulcan.module.modules.player;

import net.minecraft.init.Items;
import me.ka1.vulcan.module.Module;

public class FastCrystal extends Module
{
    public FastCrystal() {
        super("FastCrystal", "mc.rightClickDelayTimer = 0;", Category.Player);
    }
    
    @Override
    public int onUpdate() {
        if (FastCrystal.mc.player == null || FastCrystal.mc.world == null) {
            return 0;
        }
        if (FastCrystal.mc.player.getHeldItemOffhand().getItem() == Items.END_CRYSTAL || FastCrystal.mc.player.getHeldItemMainhand().getItem() == Items.END_CRYSTAL) {
            return FastCrystal.mc.rightClickDelayTimer = 0;
        }
        return 0;
    }
}
