//Deobfuscated with https://github.com/PetoPetko/Minecraft-Deobfuscator3000 using mappings "1.12 stable mappings"!

// 
// Decompiled by Procyon v0.5.36
// 

package me.ka1.vulcan.module.modules.render;

import me.ka1.vulcan.Vulcan;
import net.minecraft.util.math.AxisAlignedBB;
import org.lwjgl.opengl.GL11;
import java.awt.Color;
import me.ka1.vulcan.util.RenderUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.item.EntityExpBottle;
import me.ka1.vulcan.event.events.RenderEvent;
import java.util.List;
import net.minecraft.entity.Entity;
import java.util.ArrayList;
import me.ka1.vulcan.setting.Setting;
import me.ka1.vulcan.module.Module;

public class Esp extends Module
{
    Setting.Mode mode;
    Setting.Boolean crystal;
    Setting.Integer red;
    Setting.Integer green;
    Setting.Integer blue;
    Setting.Integer alpha;
    Setting.Integer lineWidth;
    ArrayList<Entity> entities;
    
    public Esp() {
        super("ESP", Category.Render);
    }
    
    @Override
    public void setup() {
        final ArrayList<String> modes = new ArrayList<String>();
        modes.add("Glow");
        modes.add("Outline");
        modes.add("Box");
        this.red = this.registerInteger("Red", "red", 255, 0, 255);
        this.green = this.registerInteger("Green", "green", 255, 0, 255);
        this.blue = this.registerInteger("Blue", "blue", 255, 0, 255);
        this.alpha = this.registerInteger("Alpha", "alpha", 120, 0, 255);
        this.mode = this.registerMode("Mode", "mode", modes, "Box");
        this.crystal = this.registerBoolean("Crystals", "crystal", true);
        this.lineWidth = this.registerInteger("Line Width", "LineWidth", 3, 0, 20);
    }
    
    @Override
    public int onUpdate() {
        this.entities = (ArrayList<Entity>)Esp.mc.world.getLoadedEntityList();
        return 0;
    }
    
    @Override
    public void onWorldRender(final RenderEvent event) {
        boolean drawThisThing;
        AxisAlignedBB bb;
        Esp.mc.world.loadedEntityList.stream().filter(entity -> entity != Esp.mc.player).forEach(e -> {
            drawThisThing = false;
            if (e instanceof EntityExpBottle) {
                drawThisThing = true;
            }
            else if (e instanceof EntityEnderPearl) {
                drawThisThing = true;
            }
            else if (this.crystal.getValue() && e instanceof EntityEnderCrystal) {
                drawThisThing = true;
            }
            else if (e instanceof EntityItem) {
                drawThisThing = true;
            }
            else if (e instanceof EntityXPOrb) {
                drawThisThing = true;
            }
            else if (e instanceof EntityPlayer) {
                drawThisThing = true;
            }
            if (drawThisThing) {
                if (this.mode.getValue().equalsIgnoreCase("Box")) {
                    e.setGlowing(false);
                    bb = e.getRenderBoundingBox();
                    RenderUtil.prepare(7);
                    RenderUtil.drawBox(bb, new Color(this.red.getValue(), this.green.getValue(), this.blue.getValue(), this.alpha.getValue()).getRGB(), 63);
                    RenderUtil.release();
                    RenderUtil.prepare(1);
                    GL11.glLineWidth((float)this.lineWidth.getValue());
                    RenderUtil.drawBox(bb, new Color(this.red.getValue(), this.green.getValue(), this.blue.getValue(), 255).getRGB(), 63);
                    RenderUtil.release();
                }
                else if (this.mode.getValue().equalsIgnoreCase("Glow")) {
                    e.setGlowing(true);
                }
                else if (this.mode.getValue().equalsIgnoreCase("Outline")) {
                    e.setGlowing(false);
                }
            }
            RenderUtil.releaseGL();
            return;
        });
        super.onWorldRender(event);
    }
    
    void RenderEspBox(final Entity entity) {
        RenderUtil.prepare(7);
        RenderUtil.drawBox(entity.getRenderBoundingBox(), new Color(this.red.getValue(), this.green.getValue(), this.blue.getValue(), this.alpha.getValue()).getRGB(), 63);
        RenderUtil.release();
    }
    
    @Override
    protected void onDisable() {
        if (this.mode.getValue().equalsIgnoreCase("Glow")) {
            Esp.mc.world.loadedEntityList.stream().filter(e -> e != Esp.mc.player).forEach(e -> {
                if (e instanceof EntityExpBottle) {
                    e.setGlowing(false);
                }
                if (e instanceof EntityEnderPearl) {
                    e.setGlowing(false);
                }
                if (e instanceof EntityEnderCrystal) {
                    e.setGlowing(false);
                }
                if (e instanceof EntityItem) {
                    e.setGlowing(false);
                }
                if (e instanceof EntityXPOrb) {
                    e.setGlowing(false);
                }
                if (e instanceof EntityPlayer) {
                    e.setGlowing(false);
                }
                return;
            });
        }
        Vulcan.EVENT_BUS.unsubscribe(this);
    }
}
