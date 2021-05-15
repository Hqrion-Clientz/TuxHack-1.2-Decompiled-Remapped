//Deobfuscated with https://github.com/PetoPetko/Minecraft-Deobfuscator3000 using mappings "1.12 stable mappings"!

// 
// Decompiled by Procyon v0.5.36
// 

package me.ka1.vulcan.module.modules.movement;

import com.mojang.realmsclient.gui.ChatFormatting;
import java.util.List;
import java.util.ArrayList;
import me.ka1.vulcan.setting.Setting;
import me.ka1.vulcan.module.Module;

public class Sprint extends Module
{
    Setting.Mode mode;
    
    public Sprint() {
        super("Sprint", "Makes you sprint", Category.Movement);
    }
    
    @Override
    public void setup() {
        final ArrayList<String> Modes = new ArrayList<String>();
        Modes.add("Rage");
        Modes.add("Legit");
        this.mode = this.registerMode("Mode", "Mode", Modes, "Rage");
    }
    
    @Override
    public int onUpdate() {
        if (this.mode.getValue().equalsIgnoreCase("Legit")) {
            if (Sprint.mc.gameSettings.keyBindForward.isKeyDown() && !Sprint.mc.player.collidedHorizontally && !Sprint.mc.player.isSprinting()) {
                Sprint.mc.player.setSprinting(true);
            }
        }
        else if (this.mode.getValue().equalsIgnoreCase("Rage") && !Sprint.mc.player.isSprinting()) {
            Sprint.mc.player.setSprinting(true);
        }
        return 0;
    }
    
    @Override
    public String getHudInfo() {
        String t = "";
        if (this.mode.getValue().equalsIgnoreCase("Rage")) {
            t = "[" + ChatFormatting.WHITE + "Rage" + ChatFormatting.GRAY + "]";
        }
        if (this.mode.getValue().equalsIgnoreCase("Legit")) {
            t = "[" + ChatFormatting.WHITE + "Legit" + ChatFormatting.GRAY + "]";
        }
        return t;
    }
}
