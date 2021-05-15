//Deobfuscated with https://github.com/PetoPetko/Minecraft-Deobfuscator3000 using mappings "1.12 stable mappings"!

// 
// Decompiled by Procyon v0.5.36
// 

package me.ka1.vulcan;

import club.minnced.discord.rpc.DiscordEventHandlers;
import club.minnced.discord.rpc.DiscordRichPresence;
import club.minnced.discord.rpc.DiscordRPC;
import net.minecraft.client.Minecraft;

public class VulcanRPC
{
    private static final String ClientId = "832760557433257994";
    private static final Minecraft mc;
    private static final DiscordRPC rpc;
    public static DiscordRichPresence presence;
    private static String details;
    private static String state;
    
    public static void init() {
        final DiscordEventHandlers handlers = new DiscordEventHandlers();
        handlers.disconnected = ((var1, var2) -> System.out.println("Discord RPC disconnected, var1: " + String.valueOf(var1) + ", var2: " + var2));
        VulcanRPC.rpc.Discord_Initialize("832760557433257994", handlers, true, "");
        VulcanRPC.presence.startTimestamp = System.currentTimeMillis() / 1000L;
        VulcanRPC.presence.details = "TuxHack 1";
        VulcanRPC.presence.state = me.ka1.vulcan.module.modules.client.DiscordRPC.presenceState.getValue();
        VulcanRPC.presence.largeImageKey = me.ka1.vulcan.module.modules.client.DiscordRPC.logoMode.getValue().toLowerCase();
        VulcanRPC.presence.largeImageText = "TuxHack 1";
        VulcanRPC.presence.smallImageKey = "transparent";
        VulcanRPC.presence.smallImageText = VulcanRPC.mc.getSession().getUsername();
        VulcanRPC.rpc.Discord_UpdatePresence(VulcanRPC.presence);
        new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    VulcanRPC.rpc.Discord_RunCallbacks();
                    VulcanRPC.details = "TuxHack1";
                    VulcanRPC.state = me.ka1.vulcan.module.modules.client.DiscordRPC.presenceState.getValue();
                    if (VulcanRPC.mc.isIntegratedServerRunning()) {
                        VulcanRPC.details = "Singleplayer";
                    }
                    else if (VulcanRPC.mc.getCurrentServerData() != null && me.ka1.vulcan.module.modules.client.DiscordRPC.server.getValue()) {
                        if (!VulcanRPC.mc.getCurrentServerData().serverIP.equals("")) {
                            VulcanRPC.details = "bullying autistic kids on " + VulcanRPC.mc.getCurrentServerData().serverIP;
                        }
                    }
                    else {
                        VulcanRPC.details = " chilling in the main menu :^)";
                    }
                    if (!VulcanRPC.details.equals(VulcanRPC.presence.details) || !VulcanRPC.state.equals(VulcanRPC.presence.state)) {
                        VulcanRPC.presence.startTimestamp = System.currentTimeMillis() / 1000L;
                    }
                    VulcanRPC.presence.details = VulcanRPC.details;
                    VulcanRPC.presence.state = VulcanRPC.state;
                    VulcanRPC.rpc.Discord_UpdatePresence(VulcanRPC.presence);
                }
                catch (Exception e2) {
                    e2.printStackTrace();
                }
                try {
                    Thread.sleep(5000L);
                }
                catch (InterruptedException e3) {
                    e3.printStackTrace();
                }
            }
        }, "Discord-RPC-Callback-Handler").start();
    }
    
    public static void shutdown() {
        VulcanRPC.rpc.Discord_Shutdown();
    }
    
    static {
        mc = Minecraft.getMinecraft();
        rpc = DiscordRPC.INSTANCE;
        VulcanRPC.presence = new DiscordRichPresence();
    }
}
