//Deobfuscated with https://github.com/PetoPetko/Minecraft-Deobfuscator3000 using mappings "1.12 stable mappings"!

// 
// Decompiled by Procyon v0.5.36
// 

package me.ka1.vulcan.module.modules.misc;

import java.util.Iterator;
import net.minecraft.item.ItemStack;
import me.ka1.vulcan.util.font.FontUtils;
import java.awt.Color;
import me.ka1.vulcan.setting.Setting;
import me.ka1.vulcan.module.Module;

public class ArmorNotification extends Module
{
    Setting.Integer x;
    Setting.Integer y;
    Setting.Integer r;
    Setting.Integer g;
    Setting.Integer b;
    
    public ArmorNotification() {
        super("ArmorNotify", "Notifies you when you need to mend your armor", Category.Misc);
    }
    
    @Override
    public void setup() {
        this.x = this.registerInteger("X", "x", 500, 0, 1920);
        this.y = this.registerInteger("Y", "y", 500, 0, 1080);
        this.r = this.registerInteger("Red", "red", 255, 0, 255);
        this.g = this.registerInteger("Green", "green", 255, 0, 255);
        this.b = this.registerInteger("Blue", "blue", 255, 0, 255);
    }
    
    @Override
    public int onUpdate() {
        super.onUpdate();
        return 0;
    }
    
    @Override
    public void onRender() {
        final boolean ArmorDurability = this.getArmorDurability();
        if (ArmorDurability) {
            FontUtils.drawStringWithShadow(false, "Armor is below 50%", this.x.getValue(), this.y.getValue(), new Color(this.r.getValue(), this.g.getValue(), this.b.getValue(), 255).getRGB());
        }
        super.onRender();
    }
    
    private boolean getArmorDurability() {
        final boolean TotalDurability = false;
        for (final ItemStack itemStack : ArmorNotification.mc.player.inventory.armorInventory) {
            if (itemStack.getMaxDamage() / 2 < itemStack.getItemDamage()) {
                return true;
            }
        }
        return TotalDurability;
    }
}
