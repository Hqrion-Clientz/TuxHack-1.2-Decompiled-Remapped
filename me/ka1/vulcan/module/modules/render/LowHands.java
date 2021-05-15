//Deobfuscated with https://github.com/PetoPetko/Minecraft-Deobfuscator3000 using mappings "1.12 stable mappings"!

// 
// Decompiled by Procyon v0.5.36
// 

package me.ka1.vulcan.module.modules.render;

import me.ka1.vulcan.setting.Setting;
import net.minecraft.client.renderer.ItemRenderer;
import me.ka1.vulcan.module.Module;

public class LowHands extends Module
{
    ItemRenderer itemRenderer;
    Setting.Double off;
    Setting.Double main;
    
    public LowHands() {
        super("LowHands", Category.Render);
        this.itemRenderer = LowHands.mc.entityRenderer.itemRenderer;
    }
    
    @Override
    public void setup() {
        this.off = this.registerDouble("Off Height", "LowOffhandHeight", 0.5, 0.0, 1.0);
        this.main = this.registerDouble("Main Height", "LowMainhandHeight", 0.5, 0.0, 1.0);
    }
    
    @Override
    public void onRender() {
        this.itemRenderer.equippedProgressOffHand = (float)this.off.getValue();
        this.itemRenderer.equippedProgressMainHand = (float)this.main.getValue();
    }
}
