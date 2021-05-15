//Deobfuscated with https://github.com/PetoPetko/Minecraft-Deobfuscator3000 using mappings "1.12 stable mappings"!

// 
// Decompiled by Procyon v0.5.36
// 

package me.ka1.vulcan.module.modules.chat;

import me.ka1.vulcan.Vulcan;
import java.util.List;
import java.util.ArrayList;
import java.util.function.Predicate;
import me.ka1.vulcan.command.Command;
import net.minecraft.network.play.client.CPacketChatMessage;
import me.zero.alpine.listener.EventHandler;
import me.ka1.vulcan.event.events.PacketEvent;
import me.zero.alpine.listener.Listener;
import me.ka1.vulcan.setting.Setting;
import me.ka1.vulcan.module.Module;

public class ChatSuffix extends Module
{
    Setting.Mode Separator;
    Setting.Mode Mode;
    @EventHandler
    private final Listener<PacketEvent.Send> listener;
    
    public ChatSuffix() {
        super("ChatSuffix", "Adds a suffix to the end of your message", Category.Chat);
        String Separator2;
        String Mode2;
        String old;
        String suffix;
        String s;
        this.listener = new Listener<PacketEvent.Send>(event -> {
            if (event.getPacket() instanceof CPacketChatMessage) {
                if (!((CPacketChatMessage)event.getPacket()).getMessage().startsWith("/") && !((CPacketChatMessage)event.getPacket()).getMessage().startsWith(Command.getPrefix())) {
                    if (!((CPacketChatMessage)event.getPacket()).getMessage().startsWith("!") && !((CPacketChatMessage)event.getPacket()).getMessage().startsWith(Command.getPrefix())) {
                        if (!((CPacketChatMessage)event.getPacket()).getMessage().startsWith(";") && !((CPacketChatMessage)event.getPacket()).getMessage().startsWith(Command.getPrefix())) {
                            if (!((CPacketChatMessage)event.getPacket()).getMessage().startsWith(".") && !((CPacketChatMessage)event.getPacket()).getMessage().startsWith(Command.getPrefix())) {
                                if (!((CPacketChatMessage)event.getPacket()).getMessage().startsWith(":") && !((CPacketChatMessage)event.getPacket()).getMessage().startsWith(Command.getPrefix())) {
                                    if (!((CPacketChatMessage)event.getPacket()).getMessage().startsWith("-") && !((CPacketChatMessage)event.getPacket()).getMessage().startsWith(Command.getPrefix())) {
                                        Separator2 = "|";
                                        if (this.Separator.getValue().equalsIgnoreCase(">>")) {
                                            Separator2 = " \u300b";
                                        }
                                        else if (this.Separator.getValue().equalsIgnoreCase("|")) {
                                            Separator2 = " \u23d0 ";
                                        }
                                        Mode2 = "TuxHack";
                                        if (this.Mode.getValue().equalsIgnoreCase("Vulcan")) {
                                            Mode2 = "TuxHack";
                                        }
                                        else if (this.Mode.getValue().equalsIgnoreCase("VULCAN")) {
                                            Mode2 = "tuxhack";
                                        }
                                        else if (this.Mode.getValue().equalsIgnoreCase("BUSHMOD")) {
                                            Mode2 = "TuxHack :^)";
                                        }
                                        old = ((CPacketChatMessage)event.getPacket()).getMessage();
                                        suffix = this.toUnicode(Mode2);
                                        s = old + Separator2 + suffix;
                                        if (s.length() <= 255) {
                                            ((CPacketChatMessage)event.getPacket()).message = s;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }, (Predicate<PacketEvent.Send>[])new Predicate[0]);
    }
    
    @Override
    public void setup() {
        final ArrayList<String> Separators = new ArrayList<String>();
        Separators.add(" >");
        Separators.add(" >>");
        Separators.add(" |");
        Separators.add(" ||");
        Separators.add(" ~");
        Separators.add(" :^)");
        Separators.add(" |:^)");
        this.Separator = this.registerMode("Separator", "Separator", Separators, "|");
        final ArrayList<String> Modes = new ArrayList<String>();
        Modes.add(" TuxHack");
        Modes.add(" tuxhack");
        Modes.add(" TuxHack :^)");
        this.Mode = this.registerMode("Mode", "Mode", Modes, "Tuxhack");
    }
    
    public void onEnable() {
        Vulcan.EVENT_BUS.subscribe(this);
    }
    
    public String toUnicode(final String s) {
        return s.toLowerCase().replace("a", "\u1d00").replace("b", "\u0299").replace("c", "\u1d04").replace("d", "\u1d05").replace("e", "\u1d07").replace("f", "\ua730").replace("g", "\u0262").replace("h", "\u029c").replace("i", "\u026a").replace("j", "\u1d0a").replace("k", "\u1d0b").replace("l", "\u029f").replace("m", "\u1d0d").replace("n", "\u0274").replace("o", "\u1d0f").replace("p", "\u1d18").replace("q", "\u01eb").replace("r", "\u0280").replace("s", "\ua731").replace("t", "\u1d1b").replace("u", "\u1d1c").replace("v", "\u1d20").replace("w", "\u1d21").replace("x", "\u02e3").replace("y", "\u028f").replace("z", "\u1d22");
    }
    
    public void onDisable() {
        Vulcan.EVENT_BUS.unsubscribe(this);
    }
}
