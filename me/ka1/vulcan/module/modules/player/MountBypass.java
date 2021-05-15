//Deobfuscated with https://github.com/PetoPetko/Minecraft-Deobfuscator3000 using mappings "1.12 stable mappings"!

// 
// Decompiled by Procyon v0.5.36
// 

package me.ka1.vulcan.module.modules.player;

import java.util.function.Predicate;
import net.minecraft.world.World;
import net.minecraft.entity.passive.AbstractChestHorse;
import net.minecraft.network.play.client.CPacketUseEntity;
import me.zero.alpine.listener.EventHandler;
import me.ka1.vulcan.event.events.EventNetworkPacketEvent;
import me.zero.alpine.listener.Listener;
import me.ka1.vulcan.module.Module;

public class MountBypass extends Module
{
    @EventHandler
    private Listener<EventNetworkPacketEvent> onPacketEventSend;
    
    public MountBypass() {
        super("MountBypass", "Forcefully mounts chests on entities", Category.Player);
        CPacketUseEntity packet;
        this.onPacketEventSend = new Listener<EventNetworkPacketEvent>(event -> {
            if (event.getPacket() instanceof CPacketUseEntity) {
                packet = (CPacketUseEntity)event.getPacket();
                if (packet.getEntityFromWorld((World)MountBypass.mc.world) instanceof AbstractChestHorse && packet.getAction() == CPacketUseEntity.Action.INTERACT_AT) {
                    event.cancel();
                }
            }
        }, (Predicate<EventNetworkPacketEvent>[])new Predicate[0]);
    }
}
