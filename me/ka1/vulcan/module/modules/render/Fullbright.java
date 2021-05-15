//Deobfuscated with https://github.com/PetoPetko/Minecraft-Deobfuscator3000 using mappings "1.12 stable mappings"!

// 
// Decompiled by Procyon v0.5.36
// 

package me.ka1.vulcan.module.modules.render;

import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.Potion;
import java.util.List;
import java.util.ArrayList;
import me.ka1.vulcan.setting.Setting;
import me.ka1.vulcan.module.Module;

public class Fullbright extends Module
{
    Setting.Mode fullbrightMode;
    
    public Fullbright() {
        super("Fullbright", "Brightens up your world", Category.Render);
    }
    
    @Override
    public void setup() {
        final ArrayList<String> Modes = new ArrayList<String>();
        Modes.add("Potion");
        Modes.add("Gamma");
        this.fullbrightMode = this.registerMode("Mode", "fullbrightMode", Modes, "Potion");
    }
    
    @Override
    public void onRender() {
        if (this.fullbrightMode.getValue().equals("Potion")) {
            final PotionEffect NightVis = new PotionEffect(Potion.getPotionById(16), 696969696, 5);
            NightVis.setPotionDurationMax(true);
            Fullbright.mc.player.addPotionEffect(NightVis);
        }
        else if (this.fullbrightMode.getValue().equals("Gamma")) {
            Fullbright.mc.gameSettings.gammaSetting = 420.0f;
            Fullbright.mc.player.removePotionEffect(Potion.getPotionById(16));
        }
    }
}
