// 
// Decompiled by Procyon v0.5.36
// 

package me.ka1.vulcan.module.modules.player;

import me.ka1.vulcan.Vulcan;
import java.util.function.Predicate;
import net.minecraft.network.play.client.CPacketCloseWindow;
import me.zero.alpine.listener.EventHandler;
import me.ka1.vulcan.event.events.PacketEvent;
import me.zero.alpine.listener.Listener;
import me.ka1.vulcan.module.Module;

public class Xcarry extends Module
{
    @EventHandler
    private final Listener<PacketEvent.Send> listener;
    
    public Xcarry() {
        super("XCarry", "Lets you store items in crafting slots", Category.Player);
        this.listener = new Listener<PacketEvent.Send>(event -> {
            if (event.getPacket() instanceof CPacketCloseWindow) {
                event.cancel();
            }
        }, (Predicate<PacketEvent.Send>[])new Predicate[0]);
    }
    
    public void onEnable() {
        Vulcan.EVENT_BUS.subscribe(this);
    }
    
    public void onDisable() {
        Vulcan.EVENT_BUS.unsubscribe(this);
    }
}
