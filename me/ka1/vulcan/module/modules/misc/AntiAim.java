//Deobfuscated with https://github.com/PetoPetko/Minecraft-Deobfuscator3000 using mappings "1.12 stable mappings"!

// 
// Decompiled by Procyon v0.5.36
// 

package me.ka1.vulcan.module.modules.misc;

import java.util.Random;
import me.ka1.vulcan.module.Module;

public class AntiAim extends Module
{
    public AntiAim() {
        super("AntiAim", "Makes it hard to target you", Category.Misc);
    }
    
    @Override
    public int onUpdate() {
        final Random r = new Random();
        AntiAim.mc.player.rotationPitch = (float)r.nextInt(150);
        AntiAim.mc.player.rotationYaw = (float)r.nextInt(150);
        return 0;
    }
}
