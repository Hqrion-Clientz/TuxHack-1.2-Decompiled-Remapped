// 
// Decompiled by Procyon v0.5.36
// 

package me.ka1.vulcan.event.events;

import net.minecraft.network.Packet;
import me.ka1.vulcan.event.MinecraftEvent;

public class EventNetworkPacketEvent extends MinecraftEvent
{
    public Packet m_Packet;
    
    public EventNetworkPacketEvent(final Packet p_Packet) {
        this.m_Packet = p_Packet;
    }
    
    public Packet GetPacket() {
        return this.m_Packet;
    }
    
    public Packet getPacket() {
        return this.m_Packet;
    }
}
