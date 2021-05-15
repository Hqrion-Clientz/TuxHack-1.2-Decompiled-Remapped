// 
// Decompiled by Procyon v0.5.36
// 

package me.ka1.vulcan.command.commands;

import me.ka1.vulcan.module.Module;
import me.ka1.vulcan.module.ModuleManager;
import org.lwjgl.input.Keyboard;
import me.ka1.vulcan.command.Command;

public class BindCommand extends Command
{
    @Override
    public String[] getAlias() {
        return new String[] { "bind", "b" };
    }
    
    @Override
    public String getSyntax() {
        return "bind <Module> <Key>";
    }
    
    @Override
    public void onCommand(final String command, final String[] args) throws Exception {
        final int key = Keyboard.getKeyIndex(args[1].toUpperCase());
        final int bind;
        ModuleManager.getModules().forEach(m -> {
            if (args[0].equalsIgnoreCase(m.getName())) {
                m.setBind(bind);
                Command.sendClientMessage(args[0] + " bound to " + args[1].toUpperCase());
            }
        });
    }
}
