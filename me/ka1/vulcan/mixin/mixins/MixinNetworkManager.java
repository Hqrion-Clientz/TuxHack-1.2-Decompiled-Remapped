// 
// Decompiled by Procyon v0.5.36
// 

package me.ka1.vulcan.mixin.mixins;

import io.netty.channel.ChannelHandlerContext;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import me.ka1.vulcan.Vulcan;
import me.ka1.vulcan.event.events.PacketEvent;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.network.Packet;
import net.minecraft.network.NetworkManager;
import org.spongepowered.asm.mixin.Mixin;

@Mixin({ NetworkManager.class })
public class MixinNetworkManager
{
    @Inject(method = { "sendPacket(Lnet/minecraft/network/Packet;)V" }, at = { @At("HEAD") }, cancellable = true)
    private void preSendPacket(final Packet<?> packet, final CallbackInfo callbackInfo) {
        final PacketEvent.Send event = new PacketEvent.Send(packet);
        Vulcan.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            callbackInfo.cancel();
        }
    }
    
    @Inject(method = { "channelRead0" }, at = { @At("HEAD") }, cancellable = true)
    private void preChannelRead(final ChannelHandlerContext context, final Packet<?> packet, final CallbackInfo callbackInfo) {
        final PacketEvent.Receive event = new PacketEvent.Receive(packet);
        Vulcan.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            callbackInfo.cancel();
        }
    }
    
    @Inject(method = { "sendPacket(Lnet/minecraft/network/Packet;)V" }, at = { @At("TAIL") }, cancellable = true)
    private void postSendPacket(final Packet<?> packet, final CallbackInfo callbackInfo) {
        final PacketEvent.PostSend event = new PacketEvent.PostSend(packet);
        Vulcan.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            callbackInfo.cancel();
        }
    }
    
    @Inject(method = { "channelRead0" }, at = { @At("TAIL") }, cancellable = true)
    private void postChannelRead(final ChannelHandlerContext context, final Packet<?> packet, final CallbackInfo callbackInfo) {
        final PacketEvent.PostReceive event = new PacketEvent.PostReceive(packet);
        Vulcan.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            callbackInfo.cancel();
        }
    }
}
