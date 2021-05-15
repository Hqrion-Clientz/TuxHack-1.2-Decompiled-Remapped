//Deobfuscated with https://github.com/PetoPetko/Minecraft-Deobfuscator3000 using mappings "1.12 stable mappings"!

// 
// Decompiled by Procyon v0.5.36
// 

package me.ka1.vulcan.ClickGui2;

import java.io.IOException;
import java.util.Iterator;
import me.ka1.vulcan.ClickGui2.frame.Component;
import me.ka1.vulcan.module.ModuleManager;
import me.ka1.vulcan.module.Module;
import me.ka1.vulcan.ClickGui2.frame.Frames;
import java.util.ArrayList;
import net.minecraft.client.gui.GuiScreen;

public class ClickGUI2 extends GuiScreen
{
    public static ArrayList<Frames> frames;
    
    public ClickGUI2() {
        ClickGUI2.frames = new ArrayList<Frames>();
        int DevFrameX = 10;
        for (final Module.Category category : Module.Category.values()) {
            final Frames devframe = new Frames(category);
            devframe.setX(DevFrameX);
            ClickGUI2.frames.add(devframe);
            DevFrameX += devframe.getWidth() + 10;
        }
    }
    
    public void drawScreen(final int mouseX, final int mouseY, final float partialTicks) {
        for (final Frames frames : ClickGUI2.frames) {
            if (ModuleManager.isModuleEnabled("HudEditor")) {
                if (frames.category.name() != "Hud") {
                    continue;
                }
                frames.renderGUIFrame(this.fontRenderer);
                frames.updatePosition(mouseX, mouseY);
                frames.updateMouseWheel();
                for (final Component comp : frames.getComponents()) {
                    comp.updateComponent(mouseX, mouseY);
                }
            }
            else {
                frames.renderGUIFrame(this.fontRenderer);
                frames.updatePosition(mouseX, mouseY);
                frames.updateMouseWheel();
                for (final Component comp : frames.getComponents()) {
                    comp.updateComponent(mouseX, mouseY);
                }
            }
        }
    }
    
    protected void mouseClicked(final int mouseX, final int mouseY, final int mouseButton) throws IOException {
        for (final Frames frames : ClickGUI2.frames) {
            if (ModuleManager.isModuleEnabled("HudEditor")) {
                if (frames.category.name() != "Hud") {
                    continue;
                }
                if (frames.isWithinHeader(mouseX, mouseY) && mouseButton == 0) {
                    frames.setDrag(true);
                    frames.dragX = mouseX - frames.getX();
                    frames.dragY = mouseY - frames.getY();
                }
                if (frames.isWithinHeader(mouseX, mouseY) && mouseButton == 1) {
                    frames.setOpen(!frames.isOpen());
                }
                if (!frames.isOpen() || frames.getComponents().isEmpty()) {
                    continue;
                }
                for (final Component component : frames.getComponents()) {
                    component.mouseClicked(mouseX, mouseY, mouseButton);
                }
            }
            else {
                if (frames.isWithinHeader(mouseX, mouseY) && mouseButton == 0) {
                    frames.setDrag(true);
                    frames.dragX = mouseX - frames.getX();
                    frames.dragY = mouseY - frames.getY();
                }
                if (frames.isWithinHeader(mouseX, mouseY) && mouseButton == 1) {
                    frames.setOpen(!frames.isOpen());
                }
                if (!frames.isOpen() || frames.getComponents().isEmpty()) {
                    continue;
                }
                for (final Component component : frames.getComponents()) {
                    component.mouseClicked(mouseX, mouseY, mouseButton);
                }
            }
        }
    }
    
    protected void mouseReleased(final int mouseX, final int mouseY, final int state) {
        for (final Frames frames : ClickGUI2.frames) {
            frames.setDrag(false);
        }
        for (final Frames frames : ClickGUI2.frames) {
            if (frames.isOpen() && !frames.getComponents().isEmpty()) {
                for (final Component component : frames.getComponents()) {
                    component.mouseReleased(mouseX, mouseY, state);
                }
            }
        }
    }
    
    protected void keyTyped(final char typedChar, final int keyCode) {
        for (final Frames frames : ClickGUI2.frames) {
            if (frames.isOpen() && !frames.getComponents().isEmpty()) {
                for (final Component component : frames.getComponents()) {
                    component.keyTyped(typedChar, keyCode);
                }
            }
        }
        if (keyCode == 1) {
            this.mc.displayGuiScreen((GuiScreen)null);
        }
    }
    
    public boolean doesGuiPauseGame() {
        return false;
    }
    
    public void initGui() {
    }
    
    public static Frames getFrameByName(final String name) {
        Frames pa = null;
        for (final Frames frames : getFrames()) {
            if (name.equalsIgnoreCase(String.valueOf(frames.category))) {
                pa = frames;
            }
        }
        return pa;
    }
    
    public static ArrayList<Frames> getFrames() {
        return ClickGUI2.frames;
    }
}
