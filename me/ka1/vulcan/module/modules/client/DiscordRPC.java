// 
// Decompiled by Procyon v0.5.36
// 

package me.ka1.vulcan.module.modules.client;

import me.ka1.vulcan.VulcanRPC;
import java.util.List;
import java.util.ArrayList;
import me.ka1.vulcan.setting.Setting;
import me.ka1.vulcan.module.Module;

public class DiscordRPC extends Module
{
    public static Setting.Mode logoMode;
    public static Setting.Mode presenceState;
    public static Setting.Boolean server;
    
    public DiscordRPC() {
        super("DiscordRPC", "Displays a customisable RPC", Category.Client);
    }
    
    @Override
    public void setup() {
        final ArrayList<String> logoModes = new ArrayList<String>();
        logoModes.add("Normal");
        logoModes.add("Transparent");
        final ArrayList<String> stateModes = new ArrayList<String>();
        stateModes.add("Owning Spawn");
        stateModes.add("tuxiscool based");
        stateModes.add("Hey :^)");
        stateModes.add(":^)");
        stateModes.add("Tuxhack owns you ");
        stateModes.add("Tux owns me and all");
        stateModes.add("bri'ish");
        DiscordRPC.logoMode = this.registerMode("Logo Mode", "RpcLogoMode", logoModes, "Normal");
        DiscordRPC.presenceState = this.registerMode("Text", "presenceState", stateModes, "Owning spawn");
        DiscordRPC.server = this.registerBoolean("Server", "serverRpc", true);
    }
    
    public void onEnable() {
        VulcanRPC.init();
    }
    
    public void onDisable() {
        VulcanRPC.shutdown();
    }
}
