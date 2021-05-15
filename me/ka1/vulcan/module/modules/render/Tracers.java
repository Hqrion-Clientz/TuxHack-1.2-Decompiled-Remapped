//Deobfuscated with https://github.com/PetoPetko/Minecraft-Deobfuscator3000 using mappings "1.12 stable mappings"!

// 
// Decompiled by Procyon v0.5.36
// 

package me.ka1.vulcan.module.modules.render;

import org.lwjgl.opengl.GL11;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.Vec3d;
import net.minecraft.entity.Entity;
import me.ka1.vulcan.util.friend.Friends;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.client.renderer.GlStateManager;
import java.awt.Color;
import me.ka1.vulcan.setting.Setting;
import me.ka1.vulcan.module.Module;

public class Tracers extends Module
{
    Setting.Integer range;
    Setting.Boolean friends;
    Setting.Double opacity;
    Setting.Double r;
    Setting.Double g;
    Setting.Double b;
    int playerAmount;
    Color color;
    
    public Tracers() {
        super("Tracers", "Draws a line to players in render distance", Category.Render);
    }
    
    @Override
    public void setup() {
        this.range = this.registerInteger("Range", "range", 96, 1, 240);
        this.friends = this.registerBoolean("Friends", "friends", true);
        this.opacity = this.registerDouble("Opacity", "opacity", 0.800000011920929, 0.10000000149011612, 1.0);
        this.r = this.registerDouble("Red", "red", 255.0, 0.0, 255.0);
        this.g = this.registerDouble("Green", "green", 255.0, 0.0, 255.0);
        this.b = this.registerDouble("Blue", "blue", 255.0, 0.0, 255.0);
    }
    
    @Override
    public void onRender() {
        if (Tracers.mc.player == null || Tracers.mc.world == null) {
            return;
        }
        this.playerAmount = 0;
        GlStateManager.pushMatrix();
        Tracers.mc.world.loadedEntityList.stream().filter(e -> e instanceof EntityPlayer).filter(e -> e != Tracers.mc.player).filter(e -> Tracers.mc.player.getDistance(e) <= this.range.getValue()).forEach(e -> {
            if (!this.friends.getValue() && Friends.isFriend(e.getName())) {
                return;
            }
            else {
                drawLineToEntity(e, (float)this.r.getValue(), (float)this.g.getValue(), (float)this.b.getValue(), (float)this.opacity.getValue());
                ++this.playerAmount;
                return;
            }
        });
        GlStateManager.popMatrix();
    }
    
    public static double interpolate(final double now, final double then) {
        return then + (now - then) * Tracers.mc.getRenderPartialTicks();
    }
    
    public static double[] interpolate(final Entity e) {
        final double posX = interpolate(e.posX, e.lastTickPosX) - Tracers.mc.getRenderManager().renderPosX;
        final double posY = interpolate(e.posY, e.lastTickPosY) - Tracers.mc.getRenderManager().renderPosY;
        final double posZ = interpolate(e.posZ, e.lastTickPosZ) - Tracers.mc.getRenderManager().renderPosZ;
        return new double[] { posX, posY, posZ };
    }
    
    public static void drawLineToEntity(final Entity e, final float red, final float green, final float blue, final float opacity) {
        final double[] xyz = interpolate(e);
        drawLine(xyz[0], xyz[1], xyz[2], e.height, red, green, blue, opacity);
    }
    
    public static void drawLine(final double posx, final double posy, final double posz, final double up, final float red, final float green, final float blue, final float opacity) {
        final Vec3d eyes = new Vec3d(0.0, 0.0, 1.0).rotatePitch(-(float)Math.toRadians(Minecraft.getMinecraft().player.rotationPitch)).rotateYaw(-(float)Math.toRadians(Minecraft.getMinecraft().player.rotationYaw));
        drawLineFromPosToPos(eyes.x, eyes.y + Tracers.mc.player.getEyeHeight(), eyes.z, posx, posy, posz, up, red, green, blue, opacity);
    }
    
    public static void drawLineFromPosToPos(final double posx, final double posy, final double posz, final double posx2, final double posy2, final double posz2, final double up, final float red, final float green, final float blue, final float opacity) {
        GL11.glBlendFunc(770, 771);
        GL11.glEnable(3042);
        GL11.glLineWidth(1.5f);
        GL11.glDisable(3553);
        GL11.glDisable(2929);
        GL11.glDepthMask(false);
        GL11.glColor4f(red, green, blue, opacity);
        GlStateManager.disableLighting();
        GL11.glLoadIdentity();
        Tracers.mc.entityRenderer.orientCamera(Tracers.mc.getRenderPartialTicks());
        GL11.glBegin(1);
        GL11.glVertex3d(posx, posy, posz);
        GL11.glVertex3d(posx2, posy2, posz2);
        GL11.glVertex3d(posx2, posy2, posz2);
        GL11.glVertex3d(posx2, posy2 + up, posz2);
        GL11.glEnd();
        GL11.glEnable(3553);
        GL11.glEnable(2929);
        GL11.glDepthMask(true);
        GL11.glDisable(3042);
        GL11.glColor3d(1.0, 1.0, 1.0);
        GlStateManager.enableLighting();
    }
}
