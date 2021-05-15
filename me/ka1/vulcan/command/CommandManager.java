// 
// Decompiled by Procyon v0.5.36
// 

package me.ka1.vulcan.command;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.ka1.vulcan.command.commands.NameMcCommand;
import me.ka1.vulcan.command.commands.EnemyCommand;
import me.ka1.vulcan.command.commands.FriendCommand;
import me.ka1.vulcan.command.commands.FontCommand;
import me.ka1.vulcan.command.commands.BindCommand;
import me.ka1.vulcan.command.commands.ConfigCommand;
import java.util.ArrayList;

public class CommandManager
{
    private static ArrayList<Command> commands;
    boolean b;
    
    public static void initCommands() {
        CommandManager.commands = new ArrayList<Command>();
        addCommand(new ConfigCommand());
        addCommand(new BindCommand());
        addCommand(new FontCommand());
        addCommand(new FriendCommand());
        addCommand(new EnemyCommand());
        addCommand(new NameMcCommand());
    }
    
    public static void addCommand(final Command c) {
        CommandManager.commands.add(c);
    }
    
    public static ArrayList<Command> getCommands() {
        return CommandManager.commands;
    }
    
    public void callCommand(final String input) {
        final String[] split = input.split(" (?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
        final String command = split[0];
        final String args = input.substring(command.length()).trim();
        this.b = false;
        final String[] array;
        int length;
        int i = 0;
        String s;
        final String anotherString;
        final String s2;
        CommandManager.commands.forEach(c -> {
            c.getAlias();
            for (length = array.length; i < length; ++i) {
                s = array[i];
                if (s.equalsIgnoreCase(anotherString)) {
                    this.b = true;
                    try {
                        c.onCommand(s2, s2.split(" (?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)"));
                    }
                    catch (Exception e) {
                        Command.sendClientMessage(ChatFormatting.GRAY + c.getSyntax());
                    }
                }
            }
            return;
        });
        if (!this.b) {
            Command.sendClientMessage(ChatFormatting.GRAY + "Unknown command!");
        }
    }
}
