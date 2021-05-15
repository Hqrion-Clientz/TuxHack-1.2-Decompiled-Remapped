//Deobfuscated with https://github.com/PetoPetko/Minecraft-Deobfuscator3000 using mappings "1.12 stable mappings"!

// 
// Decompiled by Procyon v0.5.36
// 

package me.ka1.vulcan.clickgui;

import org.lwjgl.opengl.GL11;
import net.minecraft.client.renderer.GlStateManager;
import me.ka1.vulcan.util.font.FontUtils;
import me.ka1.vulcan.module.ModuleManager;
import net.minecraft.client.gui.Gui;
import java.awt.Color;
import me.ka1.vulcan.module.modules.client.ClickGuiModule;
import java.util.Iterator;
import me.ka1.vulcan.clickgui.buttons.KeybindComponent;
import me.ka1.vulcan.clickgui.buttons.IntegerComponent;
import me.ka1.vulcan.clickgui.buttons.DoubleComponent;
import me.ka1.vulcan.clickgui.buttons.BooleanComponent;
import me.ka1.vulcan.clickgui.buttons.ModeComponent;
import me.ka1.vulcan.setting.Setting;
import me.ka1.vulcan.Vulcan;
import net.minecraft.util.ResourceLocation;
import java.util.ArrayList;
import me.ka1.vulcan.module.Module;

public class Buttons extends Component
{
    public Module mod;
    public Frames parent;
    public int offset;
    private boolean isHovered;
    private final ArrayList<Component> subcomponents;
    public boolean open;
    private final int height;
    private static final ResourceLocation opengui;
    private static final ResourceLocation closedgui;
    
    public Buttons(final Module mod, final Frames parent, final int offset) {
        this.mod = mod;
        this.parent = parent;
        this.offset = offset;
        this.subcomponents = new ArrayList<Component>();
        this.open = false;
        this.height = 16;
        int opY = offset + 16;
        if (Vulcan.getInstance().settingsManager.getSettingsForMod(mod) != null && !Vulcan.getInstance().settingsManager.getSettingsForMod(mod).isEmpty()) {
            for (final Setting s : Vulcan.getInstance().settingsManager.getSettingsForMod(mod)) {
                switch (s.getType()) {
                    case MODE: {
                        this.subcomponents.add(new ModeComponent((Setting.Mode)s, this, mod, opY));
                        opY += 16;
                        continue;
                    }
                    case BOOLEAN: {
                        this.subcomponents.add(new BooleanComponent((Setting.Boolean)s, this, opY));
                        opY += 16;
                        continue;
                    }
                    case DOUBLE: {
                        this.subcomponents.add(new DoubleComponent((Setting.Double)s, this, opY));
                        opY += 16;
                        continue;
                    }
                    case INT: {
                        this.subcomponents.add(new IntegerComponent((Setting.Integer)s, this, opY));
                        opY += 16;
                        continue;
                    }
                    default: {
                        continue;
                    }
                }
            }
        }
        this.subcomponents.add(new KeybindComponent(this, opY));
    }
    
    @Override
    public void setOff(final int newOff) {
        this.offset = newOff;
        int opY = this.offset + 16;
        for (final Component comp : this.subcomponents) {
            comp.setOff(opY);
            opY += 16;
        }
    }
    
    @Override
    public void renderComponent() {
        ClickGUI.color = new Color(ClickGuiModule.red.getValue(), ClickGuiModule.green.getValue(), ClickGuiModule.blue.getValue(), ClickGuiModule.opacity.getValue()).getRGB();
        Gui.drawRect(this.parent.getX(), this.parent.getY() + this.offset + 1, this.parent.getX() + this.parent.getWidth(), this.parent.getY() + 16 + this.offset, this.isHovered ? (this.mod.isEnabled() ? ClickGUI.color : new Color(0, 0, 0, ClickGuiModule.opacity.getValue() - 50).darker().darker().getRGB()) : (this.mod.isEnabled() ? ClickGUI.color : new Color(0, 0, 0, ClickGuiModule.opacity.getValue() - 50).getRGB()));
        Gui.drawRect(this.parent.getX(), this.parent.getY() + this.offset, this.parent.getX() + this.parent.getWidth(), this.parent.getY() + this.offset + 1, new Color(0, 0, 0, ClickGuiModule.opacity.getValue() - 50).getRGB());
        FontUtils.drawStringWithShadow(ModuleManager.isModuleEnabled("CustomFont"), this.mod.getName(), this.parent.getX() + 2, this.parent.getY() + this.offset + 2 + 2, -1);
        if (this.subcomponents.size() > 1) {
            FontUtils.drawStringWithShadow(ModuleManager.isModuleEnabled("CustomFont"), this.open ? "-" : "+", this.parent.getX() + this.parent.getWidth() - 10, this.parent.getY() + this.offset + 2 + 2, -1);
        }
        if (this.open && !this.subcomponents.isEmpty()) {
            for (final Component comp : this.subcomponents) {
                comp.renderComponent();
            }
        }
    }
    
    @Override
    public int getHeight() {
        if (this.open) {
            return 16 * (this.subcomponents.size() + 1);
        }
        return 16;
    }
    
    @Override
    public void updateComponent(final int mouseX, final int mouseY) {
        this.isHovered = this.isMouseOnButton(mouseX, mouseY);
        if (!this.subcomponents.isEmpty()) {
            for (final Component comp : this.subcomponents) {
                comp.updateComponent(mouseX, mouseY);
            }
        }
    }
    
    @Override
    public void mouseClicked(final int mouseX, final int mouseY, final int button) {
        if (this.isMouseOnButton(mouseX, mouseY) && button == 0) {
            this.mod.toggle();
        }
        if (this.isMouseOnButton(mouseX, mouseY) && button == 1) {
            this.open = !this.open;
            this.parent.refresh();
        }
        for (final Component comp : this.subcomponents) {
            comp.mouseClicked(mouseX, mouseY, button);
        }
    }
    
    @Override
    public void mouseReleased(final int mouseX, final int mouseY, final int mouseButton) {
        for (final Component comp : this.subcomponents) {
            comp.mouseReleased(mouseX, mouseY, mouseButton);
        }
    }
    
    @Override
    public void keyTyped(final char typedChar, final int key) {
        for (final Component comp : this.subcomponents) {
            comp.keyTyped(typedChar, key);
        }
    }
    
    public boolean isMouseOnButton(final int x, final int y) {
        return x > this.parent.getX() && x < this.parent.getX() + this.parent.getWidth() && y > this.parent.getY() + this.offset && y < this.parent.getY() + 16 + this.offset;
    }
    
    public void drawOpenRender(final int x, final int y) {
        GlStateManager.enableAlpha();
        this.mc.getTextureManager().bindTexture(Buttons.opengui);
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        GL11.glPushMatrix();
        Gui.drawScaledCustomSizeModalRect(x, y, 0.0f, 0.0f, 256, 256, 10, 10, 256.0f, 256.0f);
        GL11.glPopMatrix();
        GlStateManager.disableAlpha();
        GlStateManager.clear(256);
    }
    
    public void drawClosedRender(final int x, final int y) {
        GlStateManager.enableAlpha();
        this.mc.getTextureManager().bindTexture(Buttons.closedgui);
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        GL11.glPushMatrix();
        Gui.drawScaledCustomSizeModalRect(x, y, 0.0f, 0.0f, 256, 256, 10, 10, 256.0f, 256.0f);
        GL11.glPopMatrix();
        GlStateManager.disableAlpha();
        GlStateManager.clear(256);
    }
    
    static {
        opengui = new ResourceLocation("minecraft:opengui.png");
        closedgui = new ResourceLocation("minecraft:closedgui.png");
    }
}
