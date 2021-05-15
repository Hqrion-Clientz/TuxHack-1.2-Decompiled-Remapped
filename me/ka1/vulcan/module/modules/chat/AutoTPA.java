//Deobfuscated with https://github.com/PetoPetko/Minecraft-Deobfuscator3000 using mappings "1.12 stable mappings"!

// 
// Decompiled by Procyon v0.5.36
// 

package me.ka1.vulcan.module.modules.chat;

import me.ka1.vulcan.module.Module;

public class AutoTPA extends Module
{
    public AutoTPA() {
        super("AutoTPA", "automatically /tpa's you to AnarchyBOT", Category.Chat);
    }
    
    public void onEnable() {
        this.disable();
        AutoTPA.mc.player.sendChatMessage("/tpa AnarchyB0T");
    }
}
