//Deobfuscated with https://github.com/PetoPetko/Minecraft-Deobfuscator3000 using mappings "1.12 stable mappings"!

// 
// Decompiled by Procyon v0.5.36
// 

package me.ka1.vulcan.module.modules.Hud;

import net.minecraft.entity.Entity;
import java.awt.Color;
import me.ka1.vulcan.Vulcan;
import java.util.ArrayList;
import me.ka1.vulcan.setting.Setting;
import me.ka1.vulcan.module.Module;

public class Coordinates extends Module
{
    Setting.Mode spaces;
    Setting.Mode separators;
    Setting.Boolean nether;
    Setting.Boolean nice;
    Setting.Integer r;
    Setting.Integer g;
    Setting.Integer b;
    Setting.Integer X;
    Setting.Integer Y;
    
    public Coordinates() {
        super("Coordinates", "Displays coordinates on screen", Category.Hud);
    }
    
    @Override
    public void setup() {
        final ArrayList<String> Separators = new ArrayList<String>();
        Separators.add("None");
        Separators.add("Commas");
        final ArrayList<String> Spaces = new ArrayList<String>();
        Spaces.add("One");
        Spaces.add("Two");
        this.nether = this.registerBoolean("Nether", "nether", true);
        this.nice = this.registerBoolean("Nice", "nice", true);
        this.r = this.registerInteger("Red", "red", 255, 0, 255);
        this.g = this.registerInteger("Green", "green", 255, 0, 255);
        this.b = this.registerInteger("Blue", "blue", 255, 0, 255);
        this.X = this.registerInteger("X", "x", 0, 0, 1280);
        this.Y = this.registerInteger("Y", "y", 320, 0, 960);
    }
    
    @Override
    public void onRender() {
        final String x = String.format("%.1f", Coordinates.mc.player.posX);
        final String y = String.format("%.1f", Coordinates.mc.player.posY);
        final String z = String.format("%.1f", Coordinates.mc.player.posZ);
        final String netherX = String.format("%.1f", Coordinates.mc.player.posX / 8.0);
        final String netherZ = String.format("%.1f", Coordinates.mc.player.posZ / 8.0);
        final String owX = String.format("%.1f", Coordinates.mc.player.posX * 8.0);
        final String owZ = String.format("%.1f", Coordinates.mc.player.posZ * 8.0);
        final Entity viewEntity = Coordinates.mc.getRenderViewEntity();
        final int dimension = viewEntity.dimension;
        if (Coordinates.mc.player.isDead || Coordinates.mc.player == null || Coordinates.mc.world == null) {
            return;
        }
        switch (dimension) {
            case 0: {
                if (!this.nether.getValue()) {
                    Vulcan.fontRenderer.drawStringWithShadow("[" + x + "  " + y + "  " + z + "]", this.X.getValue(), this.Y.getValue(), new Color(this.r.getValue(), this.g.getValue(), this.b.getValue(), 255).getRGB());
                    break;
                }
                if (Coordinates.mc.player.posY == 69.0 && this.nice.getValue()) {
                    Vulcan.fontRenderer.drawStringWithShadow("[" + x + "  nice  " + z + "]  [" + netherX + "  " + netherZ + "]", this.X.getValue(), this.Y.getValue(), new Color(this.r.getValue(), this.g.getValue(), this.b.getValue(), 255).getRGB());
                    break;
                }
                Vulcan.fontRenderer.drawStringWithShadow("[" + x + "  " + y + "  " + z + "]  [" + netherX + "  " + netherZ + "]", this.X.getValue(), this.Y.getValue(), new Color(this.r.getValue(), this.g.getValue(), this.b.getValue(), 255).getRGB());
                break;
            }
            case -1: {
                if (Coordinates.mc.player.posY == 69.0 && this.nice.getValue()) {
                    Vulcan.fontRenderer.drawStringWithShadow("[" + x + "  nice  " + z + "]  [" + owX + "  " + owZ + "]", this.X.getValue(), this.Y.getValue(), new Color(this.r.getValue(), this.g.getValue(), this.b.getValue(), 255).getRGB());
                    break;
                }
                Vulcan.fontRenderer.drawStringWithShadow("[" + x + "  " + y + "  " + z + "]  [" + owX + "  " + owZ + "]", this.X.getValue(), this.Y.getValue(), new Color(this.r.getValue(), this.g.getValue(), this.b.getValue(), 255).getRGB());
                break;
            }
            case 1: {
                if (Coordinates.mc.player.posY == 69.0 && this.nice.getValue()) {
                    Vulcan.fontRenderer.drawStringWithShadow("[" + x + "  nice  " + z + "]", this.X.getValue(), this.Y.getValue(), new Color(this.r.getValue(), this.g.getValue(), this.b.getValue(), 255).getRGB());
                    break;
                }
                Vulcan.fontRenderer.drawStringWithShadow("[" + x + "  " + y + "  " + z + "]", this.X.getValue(), this.Y.getValue(), new Color(this.r.getValue(), this.g.getValue(), this.b.getValue(), 255).getRGB());
                break;
            }
        }
    }
}
