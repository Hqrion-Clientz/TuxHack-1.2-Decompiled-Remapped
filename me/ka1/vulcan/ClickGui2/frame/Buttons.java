//Deobfuscated with https://github.com/PetoPetko/Minecraft-Deobfuscator3000 using mappings "1.12 stable mappings"!

// 
// Decompiled by Procyon v0.5.36
// 

package me.ka1.vulcan.ClickGui2.frame;

import net.minecraft.client.gui.Gui;
import org.lwjgl.opengl.GL11;
import me.ka1.vulcan.module.modules.client.ClickGuiModule;
import me.ka1.vulcan.module.ModuleManager;
import me.ka1.vulcan.util.font.FontUtils;
import java.awt.Color;
import net.minecraft.client.renderer.GlStateManager;
import java.util.Iterator;
import me.ka1.vulcan.ClickGui2.buttons.KeybindComponent;
import me.ka1.vulcan.ClickGui2.buttons.IntegerComponent;
import me.ka1.vulcan.ClickGui2.buttons.DoubleComponent;
import me.ka1.vulcan.ClickGui2.buttons.BooleanComponent;
import me.ka1.vulcan.ClickGui2.buttons.ModeComponent;
import me.ka1.vulcan.setting.Setting;
import me.ka1.vulcan.Vulcan;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.Minecraft;
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
    int nameWidth;
    int centeredNameCoords;
    public int settingHeight;
    public int animating;
    boolean hovering;
    Minecraft mc;
    private static final ResourceLocation opengui;
    
    public Buttons(final Module mod, final Frames parent, final int offset) {
        this.mc = Minecraft.getMinecraft();
        this.mod = mod;
        this.parent = parent;
        this.offset = offset;
        this.subcomponents = new ArrayList<Component>();
        this.open = false;
        int opY = offset + 12;
        this.nameWidth = 0;
        this.centeredNameCoords = 0;
        this.settingHeight = 12;
        this.hovering = false;
        this.animating = 0;
        if (Vulcan.getInstance().settingsManager.getSettingsForMod(mod) != null && !Vulcan.getInstance().settingsManager.getSettingsForMod(mod).isEmpty()) {
            for (final Setting s : Vulcan.getInstance().settingsManager.getSettingsForMod(mod)) {
                switch (s.getType()) {
                    case MODE: {
                        this.subcomponents.add(new ModeComponent((Setting.Mode)s, this, mod, opY));
                        break;
                    }
                    case BOOLEAN: {
                        this.subcomponents.add(new BooleanComponent((Setting.Boolean)s, this, opY));
                        break;
                    }
                    case DOUBLE: {
                        this.subcomponents.add(new DoubleComponent((Setting.Double)s, this, opY));
                        break;
                    }
                    case INT: {
                        this.subcomponents.add(new IntegerComponent((Setting.Integer)s, this, opY));
                        break;
                    }
                }
                opY += 12;
                this.settingHeight += 12;
            }
        }
        this.subcomponents.add(new KeybindComponent(this, opY));
        this.height = opY + 12 - offset;
    }
    
    @Override
    public void setOff(final int newOff) {
        this.offset = newOff;
        int opY = this.offset + 12;
        for (final Component comp : this.subcomponents) {
            comp.setOff(opY);
            opY += 12;
        }
    }
    
    @Override
    public void renderComponent() {
        if (this.mod.isEnabled()) {
            GlStateManager.pushMatrix();
            this.mc.renderEngine.bindTexture(new ResourceLocation("minecraft:rainbow.png"));
            Renderer.renderImage(this.parent.getX(), this.parent.getY() + this.offset, this.parent.getX(), this.parent.getY() + this.offset + this.parent.getRainbowOffset(), this.parent.getWidth(), 12, 1920.0f, 1080.0f);
            Renderer.RenderBoxOutline(1.5, this.parent.getX(), this.parent.getY() + this.offset, this.parent.getX() + this.parent.getWidth(), this.parent.getY() + 12 + this.offset, new Color(0, 0, 0));
            GlStateManager.popMatrix();
        }
        else {
            Renderer.drawRectStatic(this.parent.getX(), this.parent.getY() + this.offset, this.parent.getX() + this.parent.getWidth(), this.parent.getY() + 12 + this.offset, Renderer.getTransColor(false));
        }
        this.nameWidth = FontUtils.getStringWidth(false, this.mod.getName());
        this.centeredNameCoords = (this.parent.getWidth() - this.nameWidth) / 2;
        if (ModuleManager.isModuleEnabled("CustomFont")) {
            Vulcan.fontRenderer.drawStringWithShadow(this.mod.getName(), this.parent.getX() + 2, this.parent.getY() + this.offset + 2, Renderer.getFontColor(this.parent.isHovering(), this.parent.getAnimation()).getRGB());
        }
        else {
            FontUtils.drawStringWithShadow(false, this.mod.getName(), this.parent.getX() + 2, this.parent.getY() + this.offset + 2, Renderer.getFontColor(this.parent.isHovering(), this.parent.getAnimation()).getRGB());
        }
        this.drawOpenRender(this.parent.getX() + this.parent.getWidth() - 12, this.parent.getY() + this.offset + 2);
        if (this.isMouseOnButton(this.parent.getMouseX(), this.parent.getMouseY()) && this.mod.getDescription() != null) {
            if (ModuleManager.isModuleEnabled("CustomFont")) {
                Renderer.drawRectStatic(0, 0, FontUtils.getStringWidth(true, this.mod.getDescription()) + 5, 12, new Color(0, 0, 0, 150));
                Vulcan.fontRenderer.drawStringWithShadow(this.mod.getDescription(), 2.0, 2.0, ClickGuiModule.color);
            }
            else if (!ModuleManager.isModuleEnabled("CustomFont")) {
                Renderer.drawRectStatic(0, 0, FontUtils.getStringWidth(false, this.mod.getDescription()) + 5, 12, new Color(0, 0, 0, 150));
                FontUtils.drawStringWithShadow(false, this.mod.getDescription(), 2, 2, ClickGuiModule.color);
            }
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
            return this.height;
        }
        return 12;
    }
    
    @Override
    public int getButtonHeight() {
        if (this.open) {
            return this.height;
        }
        return 12;
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
        return x > this.parent.getX() && x < this.parent.getX() + this.parent.getWidth() && y > this.parent.getY() + this.offset && y < this.parent.getY() + 12 + this.offset;
    }
    
    public void drawOpenRender(final int x, final int y) {
        GlStateManager.enableAlpha();
        this.mc.getTextureManager().bindTexture(Buttons.opengui);
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        GL11.glPushMatrix();
        Gui.drawScaledCustomSizeModalRect(x, y, 0.0f, 0.0f, 512, 512, 8, 8, 512.0f, 512.0f);
        GL11.glPopMatrix();
        GlStateManager.disableAlpha();
        GlStateManager.clear(256);
    }
    
    static {
        opengui = new ResourceLocation("minecraft:opengui.png");
    }
}
