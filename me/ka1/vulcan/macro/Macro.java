//Deobfuscated with https://github.com/PetoPetko/Minecraft-Deobfuscator3000 using mappings "1.12 stable mappings"!

// 
// Decompiled by Procyon v0.5.36
// 

package me.ka1.vulcan.macro;

import net.minecraft.client.Minecraft;

public class Macro
{
    int key;
    String value;
    
    public Macro(final int k, final String v) {
        this.key = k;
        this.value = v;
    }
    
    public void onMacro() {
        if (Minecraft.getMinecraft().player != null) {
            Minecraft.getMinecraft().player.sendChatMessage(this.value);
        }
    }
    
    public int getKey() {
        return this.key;
    }
    
    public String getValue() {
        return this.value;
    }
}
