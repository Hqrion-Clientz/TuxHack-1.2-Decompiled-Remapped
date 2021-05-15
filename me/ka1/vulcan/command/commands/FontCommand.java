// 
// Decompiled by Procyon v0.5.36
// 

package me.ka1.vulcan.command.commands;

import me.ka1.vulcan.Vulcan;
import me.ka1.vulcan.util.font.CFontRenderer;
import java.awt.Font;
import me.ka1.vulcan.command.Command;

public class FontCommand extends Command
{
    @Override
    public String[] getAlias() {
        return new String[] { "font", "setfont" };
    }
    
    @Override
    public String getSyntax() {
        return "font <Name> <Size>";
    }
    
    @Override
    public void onCommand(final String command, final String[] args) throws Exception {
        final String font = args[0].replace("_", " ");
        final int size = Integer.parseInt(args[1]);
        (Vulcan.fontRenderer = new CFontRenderer(new Font(font, 0, size), true, false)).setFontName(font);
        Vulcan.fontRenderer.setFontSize(size);
    }
}
