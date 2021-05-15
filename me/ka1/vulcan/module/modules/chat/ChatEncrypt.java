//Deobfuscated with https://github.com/PetoPetko/Minecraft-Deobfuscator3000 using mappings "1.12 stable mappings"!

// 
// Decompiled by Procyon v0.5.36
// 

package me.ka1.vulcan.module.modules.chat;

import java.util.function.Predicate;
import me.ka1.vulcan.command.Command;
import net.minecraft.network.play.client.CPacketChatMessage;
import me.zero.alpine.listener.EventHandler;
import me.ka1.vulcan.event.events.PacketEvent;
import me.zero.alpine.listener.Listener;
import me.ka1.vulcan.module.Module;

public class ChatEncrypt extends Module
{
    @EventHandler
    private final Listener<PacketEvent.Send> listener;
    
    public ChatEncrypt() {
        super("ChatEncrypt", "Hide da secrets", Category.Chat);
        String message;
        this.listener = new Listener<PacketEvent.Send>(event -> {
            if (event.getPacket() instanceof CPacketChatMessage) {
                if (!((CPacketChatMessage)event.getPacket()).getMessage().startsWith("/") && !((CPacketChatMessage)event.getPacket()).getMessage().startsWith(Command.getPrefix())) {
                    if (!((CPacketChatMessage)event.getPacket()).getMessage().startsWith("!") && !((CPacketChatMessage)event.getPacket()).getMessage().startsWith(Command.getPrefix())) {
                        if (!((CPacketChatMessage)event.getPacket()).getMessage().startsWith(";") && !((CPacketChatMessage)event.getPacket()).getMessage().startsWith(Command.getPrefix())) {
                            if (!((CPacketChatMessage)event.getPacket()).getMessage().startsWith(".") && !((CPacketChatMessage)event.getPacket()).getMessage().startsWith(Command.getPrefix())) {
                                if (!((CPacketChatMessage)event.getPacket()).getMessage().startsWith(":") && !((CPacketChatMessage)event.getPacket()).getMessage().startsWith(Command.getPrefix())) {
                                    if (!((CPacketChatMessage)event.getPacket()).getMessage().startsWith("-") && !((CPacketChatMessage)event.getPacket()).getMessage().startsWith(Command.getPrefix())) {
                                        message = ((CPacketChatMessage)event.getPacket()).getMessage();
                                        this.encrypt(message);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }, (Predicate<PacketEvent.Send>[])new Predicate[0]);
    }
    
    public void encrypt(final String message) {
        final String encryptedmessage = "aaa " + message;
    }
}
