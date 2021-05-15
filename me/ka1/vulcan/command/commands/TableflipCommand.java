//Deobfuscated with https://github.com/PetoPetko/Minecraft-Deobfuscator3000 using mappings "1.12 stable mappings"!

// 
// Decompiled by Procyon v0.5.36
// 

package me.ka1.vulcan.command.commands;

import net.minecraft.client.Minecraft;
import me.ka1.vulcan.command.Command;

public class TableflipCommand extends Command
{
    @Override
    public String[] getAlias() {
        return new String[] { "tableflip" };
    }
    
    @Override
    public String getSyntax() {
        return "tableflip";
    }
    
    @Override
    public void onCommand(final String command, final String[] args) throws Exception {
        Minecraft.getMinecraft().player.sendChatMessage("hello");
    }
}
