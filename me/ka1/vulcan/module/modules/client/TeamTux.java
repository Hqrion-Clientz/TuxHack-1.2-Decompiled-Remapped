//Deobfuscated with https://github.com/PetoPetko/Minecraft-Deobfuscator3000 using mappings "1.12 stable mappings"!

// 
// Decompiled by Procyon v0.5.36
// 

package me.ka1.vulcan.module.modules.client;

import java.nio.file.Paths;
import java.net.URI;
import java.awt.Desktop;
import me.ka1.vulcan.module.Module;

public class TeamTux extends Module
{
    public TeamTux() {
        super("TeamTux", Category.Client);
    }
    
    public void onEnable() {
        try {
            Desktop.getDesktop().browse(URI.create("https://discord.gg/VugXs9TESD"));
            Desktop.getDesktop().browse(URI.create("https://namemc.com/search?q=tuxiscool"));
            TeamTux.mc.player.sendChatMessage("Join team tux here! https://discord.gg/VugXs9TESD");
            Paths.get("C:\\Users\\FiercePC\\AppData\\Roaming\\.minecraft", new String[0]);
        }
        catch (Exception ex) {}
        this.disable();
    }
}
