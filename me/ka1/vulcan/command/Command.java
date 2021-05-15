//Deobfuscated with https://github.com/PetoPetko/Minecraft-Deobfuscator3000 using mappings "1.12 stable mappings"!

// 
// Decompiled by Procyon v0.5.36
// 

package me.ka1.vulcan.command;

import java.awt.Color;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import me.ka1.vulcan.module.modules.client.CommandColor;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.client.Minecraft;

public abstract class Command
{
    static Minecraft mc;
    public static String prefix;
    public static boolean MsgWaterMark;
    public static ChatFormatting cf;
    
    public abstract String[] getAlias();
    
    public abstract String getSyntax();
    
    public abstract void onCommand(final String p0, final String[] p1) throws Exception;
    
    public static void sendClientMessage(final String message) {
        if (Command.MsgWaterMark) {
            Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion((ITextComponent)new TextComponentString(CommandColor.getBrackets() + "[" + CommandColor.getTextColor() + "TuxHack :^)" + CommandColor.getBrackets() + "] " + ChatFormatting.RESET + Command.cf + message), 69);
        }
        else {
            Command.mc.player.sendMessage((ITextComponent)new TextComponentString(Command.cf + message));
        }
    }
    
    public static Color getColorFromChatFormatting(final ChatFormatting cf) {
        if (cf == ChatFormatting.BLACK) {
            return Color.BLACK;
        }
        if (cf == ChatFormatting.GRAY) {
            return Color.GRAY;
        }
        if (cf == ChatFormatting.AQUA) {
            return Color.CYAN;
        }
        if (cf == ChatFormatting.BLUE || cf == ChatFormatting.DARK_BLUE || cf == ChatFormatting.DARK_AQUA) {
            return Color.BLUE;
        }
        if (cf == ChatFormatting.DARK_GRAY) {
            return Color.DARK_GRAY;
        }
        if (cf == ChatFormatting.DARK_GREEN || cf == ChatFormatting.GREEN) {
            return Color.GREEN;
        }
        if (cf == ChatFormatting.DARK_PURPLE) {
            return Color.MAGENTA;
        }
        if (cf == ChatFormatting.RED || cf == ChatFormatting.DARK_RED) {
            return Color.RED;
        }
        if (cf == ChatFormatting.LIGHT_PURPLE) {
            return Color.PINK;
        }
        if (cf == ChatFormatting.YELLOW) {
            return Color.YELLOW;
        }
        if (cf == ChatFormatting.GOLD) {
            return Color.ORANGE;
        }
        return Color.WHITE;
    }
    
    public static void sendRawMessage(final String message) {
        Command.mc.player.sendMessage((ITextComponent)new TextComponentString(message));
    }
    
    public static String getPrefix() {
        return Command.prefix;
    }
    
    public static void setPrefix(final String p) {
        Command.prefix = p;
    }
    
    static {
        Command.mc = Minecraft.getMinecraft();
        Command.prefix = "-";
        Command.MsgWaterMark = true;
        Command.cf = ChatFormatting.GRAY;
    }
}
