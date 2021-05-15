//Deobfuscated with https://github.com/PetoPetko/Minecraft-Deobfuscator3000 using mappings "1.12 stable mappings"!

// 
// Decompiled by Procyon v0.5.36
// 

package me.ka1.vulcan.module.modules.player;

import me.ka1.vulcan.module.Module;

public class Suicide extends Module
{
    public Suicide() {
        super("Suicide", "/kills you lol", Category.Player);
    }
    
    public void onEnable() {
        this.disable();
        Suicide.mc.player.sendChatMessage("/kill");
    }
}
