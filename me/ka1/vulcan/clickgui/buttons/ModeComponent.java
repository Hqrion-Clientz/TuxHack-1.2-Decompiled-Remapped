//Deobfuscated with https://github.com/PetoPetko/Minecraft-Deobfuscator3000 using mappings "1.12 stable mappings"!

// 
// Decompiled by Procyon v0.5.36
// 

package me.ka1.vulcan.clickgui.buttons;

import me.ka1.vulcan.util.font.FontUtils;
import com.mojang.realmsclient.gui.ChatFormatting;
import me.ka1.vulcan.module.ModuleManager;
import net.minecraft.client.gui.Gui;
import java.awt.Color;
import me.ka1.vulcan.module.modules.client.ClickGuiModule;
import me.ka1.vulcan.module.Module;
import me.ka1.vulcan.setting.Setting;
import me.ka1.vulcan.clickgui.Buttons;
import me.ka1.vulcan.clickgui.Component;

public class ModeComponent extends Component
{
    private boolean hovered;
    private final Buttons parent;
    private final Setting.Mode set;
    private int offset;
    private int x;
    private int y;
    private final Module mod;
    private int modeIndex;
    
    public ModeComponent(final Setting.Mode set, final Buttons button, final Module mod, final int offset) {
        this.set = set;
        this.parent = button;
        this.mod = mod;
        this.x = button.parent.getX() + button.parent.getWidth();
        this.y = button.parent.getY() + button.offset;
        this.offset = offset;
        this.modeIndex = 0;
    }
    
    @Override
    public void setOff(final int newOff) {
        this.offset = newOff;
    }
    
    @Override
    public void renderComponent() {
        Gui.drawRect(this.parent.parent.getX(), this.parent.parent.getY() + this.offset + 1, this.parent.parent.getX() + this.parent.parent.getWidth(), this.parent.parent.getY() + this.offset + 16, this.hovered ? new Color(0, 0, 0, ClickGuiModule.opacity.getValue() - 50).darker().darker().getRGB() : new Color(0, 0, 0, ClickGuiModule.opacity.getValue() - 50).getRGB());
        Gui.drawRect(this.parent.parent.getX(), this.parent.parent.getY() + this.offset, this.parent.parent.getX() + this.parent.parent.getWidth(), this.parent.parent.getY() + this.offset + 1, new Color(0, 0, 0, ClickGuiModule.opacity.getValue() - 50).getRGB());
        FontUtils.drawStringWithShadow(ModuleManager.isModuleEnabled("CustomFont"), this.set.getName() + " " + ChatFormatting.GRAY + this.set.getValue(), this.parent.parent.getX() + 2, this.parent.parent.getY() + this.offset + 4, -1);
    }
    
    @Override
    public void updateComponent(final int mouseX, final int mouseY) {
        this.hovered = this.isMouseOnButton(mouseX, mouseY);
        this.y = this.parent.parent.getY() + this.offset;
        this.x = this.parent.parent.getX();
    }
    
    @Override
    public void mouseClicked(final int mouseX, final int mouseY, final int button) {
        if (this.isMouseOnButton(mouseX, mouseY) && button == 0 && this.parent.open) {
            final int maxIndex = this.set.getModes().size() - 1;
            ++this.modeIndex;
            if (this.modeIndex > maxIndex) {
                this.modeIndex = 0;
            }
            this.set.setValue(this.set.getModes().get(this.modeIndex));
        }
    }
    
    public boolean isMouseOnButton(final int x, final int y) {
        return x > this.x && x < this.x + 88 && y > this.y && y < this.y + 16;
    }
}
