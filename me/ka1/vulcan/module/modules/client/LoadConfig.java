//Deobfuscated with https://github.com/PetoPetko/Minecraft-Deobfuscator3000 using mappings "1.12 stable mappings"!

// 
// Decompiled by Procyon v0.5.36
// 

package me.ka1.vulcan.module.modules.client;

import me.ka1.vulcan.module.Module;

public class LoadConfig extends Module
{
    public LoadConfig() {
        super("LoadConfig", "loadConfig", Category.Client);
    }
    
    public void onEnable() {
        if (LoadConfig.mc.world != null && LoadConfig.mc.world != null) {
            this.disable();
        }
    }
}
