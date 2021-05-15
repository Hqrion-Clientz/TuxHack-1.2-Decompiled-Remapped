// 
// Decompiled by Procyon v0.5.36
// 

package me.ka1.vulcan.command.commands;

import me.ka1.vulcan.util.config.Stopper;
import com.mojang.realmsclient.gui.ChatFormatting;
import me.ka1.vulcan.Vulcan;
import me.ka1.vulcan.command.Command;

public class ConfigCommand extends Command
{
    @Override
    public String[] getAlias() {
        return new String[] { "config", "configs", "c" };
    }
    
    @Override
    public String getSyntax() {
        return "config < save > ";
    }
    
    @Override
    public void onCommand(final String command, final String[] args) {
        if (args[0].equalsIgnoreCase("load")) {
            Vulcan.loadConfiguration.loadEnabled();
            Vulcan.loadConfiguration.loadBinds();
            Vulcan.loadConfiguration.loadGUI();
            Vulcan.loadConfiguration.loadFont();
            Vulcan.loadConfiguration.loadFriends();
            Vulcan.loadConfiguration.loadPrefix();
            Vulcan.loadModules.loadHud();
            Vulcan.loadModules.loadClient();
            Vulcan.loadModules.loadMisc();
            Vulcan.loadModules.loadCombat();
            Vulcan.loadModules.loadMovement();
            Vulcan.loadModules.loadPlayer();
            Vulcan.loadModules.loadRender();
            Command.sendClientMessage(ChatFormatting.BOLD + "Loaded Config");
        }
        else if (args[0].equalsIgnoreCase("save")) {
            Stopper.saveConfig();
            Command.sendClientMessage(ChatFormatting.BOLD + "Saved Config");
        }
        else {
            Command.sendClientMessage(this.getSyntax());
        }
    }
}
