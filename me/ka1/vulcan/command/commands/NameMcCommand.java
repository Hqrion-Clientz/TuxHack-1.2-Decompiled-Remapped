// 
// Decompiled by Procyon v0.5.36
// 

package me.ka1.vulcan.command.commands;

import java.net.URI;
import java.awt.Desktop;
import me.ka1.vulcan.command.Command;

public class NameMcCommand extends Command
{
    String name;
    
    @Override
    public String[] getAlias() {
        return new String[] { "nmc", "namecheck", "name", "namemc" };
    }
    
    @Override
    public String getSyntax() {
        return "font <Name> <Size>";
    }
    
    @Override
    public void onCommand(final String command, final String[] args) throws Exception {
        this.name = args[0];
        Desktop.getDesktop().browse(URI.create("https://namemc.com/search?q=" + this.name));
    }
}
