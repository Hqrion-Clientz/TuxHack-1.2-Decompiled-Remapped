//Deobfuscated with https://github.com/PetoPetko/Minecraft-Deobfuscator3000 using mappings "1.12 stable mappings"!

// 
// Decompiled by Procyon v0.5.36
// 

package me.ka1.vulcan.event;

import net.minecraft.client.Minecraft;
import me.zero.alpine.fork.event.type.Cancellable;

public class MinecraftEvent extends Cancellable
{
    private boolean cancelled;
    private Era era;
    private final float partialTicks;
    
    public MinecraftEvent() {
        this.cancelled = false;
        this.era = Era.PRE;
        this.partialTicks = Minecraft.getMinecraft().getRenderPartialTicks();
    }
    
    public MinecraftEvent(final Era p_Era) {
        this.cancelled = false;
        this.era = Era.PRE;
        this.partialTicks = Minecraft.getMinecraft().getRenderPartialTicks();
        this.era = p_Era;
    }
    
    public void setEra(final Era p_Era) {
        this.setCancelled(false);
        this.era = p_Era;
    }
    
    public Era getEra() {
        return this.era;
    }
    
    public float getPartialTicks() {
        return this.partialTicks;
    }
    
    public void setCancelled(final boolean way) {
        this.cancelled = way;
    }
    
    public enum Era
    {
        PRE, 
        PERI, 
        POST;
    }
}
