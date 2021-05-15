//Deobfuscated with https://github.com/PetoPetko/Minecraft-Deobfuscator3000 using mappings "1.12 stable mappings"!

// 
// Decompiled by Procyon v0.5.36
// 

package me.ka1.vulcan.util;

import net.minecraft.util.math.MathHelper;
import me.ka1.vulcan.Vulcan;
import java.util.Arrays;
import java.util.function.Predicate;
import net.minecraft.network.play.server.SPacketTimeUpdate;
import me.zero.alpine.listener.EventHandler;
import me.ka1.vulcan.event.events.PacketEvent;
import me.zero.alpine.listener.Listener;

public class TpsUtils
{
    private static final float[] tickRates;
    private int nextIndex;
    private long timeLastTimeUpdate;
    @EventHandler
    Listener<PacketEvent.Receive> listener;
    
    public TpsUtils() {
        this.nextIndex = 0;
        this.listener = new Listener<PacketEvent.Receive>(event -> {
            if (event.getPacket() instanceof SPacketTimeUpdate) {
                this.onTimeUpdate();
            }
            return;
        }, (Predicate<PacketEvent.Receive>[])new Predicate[0]);
        this.nextIndex = 0;
        this.timeLastTimeUpdate = -1L;
        Arrays.fill(TpsUtils.tickRates, 0.0f);
        Vulcan.EVENT_BUS.subscribe(this);
    }
    
    public static float getTickRate() {
        float numTicks = 0.0f;
        float sumTickRates = 0.0f;
        for (final float tickRate : TpsUtils.tickRates) {
            if (tickRate > 0.0f) {
                sumTickRates += tickRate;
                ++numTicks;
            }
        }
        return MathHelper.clamp(sumTickRates / numTicks, 0.0f, 20.0f);
    }
    
    private void onTimeUpdate() {
        if (this.timeLastTimeUpdate != -1L) {
            final float timeElapsed = (System.currentTimeMillis() - this.timeLastTimeUpdate) / 1000.0f;
            TpsUtils.tickRates[this.nextIndex % TpsUtils.tickRates.length] = MathHelper.clamp(20.0f / timeElapsed, 0.0f, 20.0f);
            ++this.nextIndex;
        }
        this.timeLastTimeUpdate = System.currentTimeMillis();
    }
    
    static {
        tickRates = new float[20];
    }
}
