// 
// Decompiled by Procyon v0.5.36
// 

package me.ka1.vulcan.ClickGui2.buttons;

import me.ka1.vulcan.Vulcan;
import me.ka1.vulcan.module.ModuleManager;
import me.ka1.vulcan.util.font.FontUtils;
import me.ka1.vulcan.ClickGui2.frame.Renderer;
import me.ka1.vulcan.ClickGui2.frame.Buttons;
import me.ka1.vulcan.setting.Setting;
import me.ka1.vulcan.ClickGui2.frame.Component;

public class BooleanComponent extends Component
{
    private boolean hovered;
    private final Setting.Boolean op;
    private final Buttons parent;
    private int offset;
    private int x;
    private int y;
    int nameWidth;
    int centeredNameCoords;
    
    public BooleanComponent(final Setting.Boolean option, final Buttons button, final int offset) {
        this.op = option;
        this.parent = button;
        this.x = button.parent.getX() + button.parent.getWidth();
        this.y = button.parent.getY() + button.offset;
        this.offset = offset;
        this.nameWidth = 0;
        this.centeredNameCoords = 0;
    }
    
    @Override
    public void renderComponent() {
        Renderer.drawRectStatic(this.parent.parent.getX(), this.parent.parent.getY() + this.offset + 1, this.parent.parent.getX() + this.parent.parent.getWidth(), this.parent.parent.getY() + 12 + this.offset, this.op.getValue() ? Renderer.getMainColor() : Renderer.getTransColor(this.hovered));
        Renderer.drawRectStatic(this.parent.parent.getX(), this.parent.parent.getY() + this.offset, this.parent.parent.getX() + this.parent.parent.getWidth(), this.parent.parent.getY() + this.offset + 1, Renderer.getTransColor(false));
        this.nameWidth = FontUtils.getStringWidth(false, this.op.getName());
        this.centeredNameCoords = (this.parent.parent.getWidth() - this.nameWidth) / 2;
        if (ModuleManager.isModuleEnabled("CustomFont")) {
            Vulcan.fontRenderer.drawStringWithShadow(this.op.getName(), this.parent.parent.getX() + this.centeredNameCoords, this.parent.parent.getY() + this.offset + 2, Renderer.getFontColor(true, 0).getRGB());
        }
        else {
            FontUtils.drawStringWithShadow(false, this.op.getName(), this.parent.parent.getX() + this.centeredNameCoords, this.parent.parent.getY() + this.offset + 2, Renderer.getFontColor(true, 0).getRGB());
        }
    }
    
    @Override
    public void setOff(final int newOff) {
        this.offset = newOff;
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
            this.op.setValue(!this.op.getValue());
        }
    }
    
    public boolean isMouseOnButton(final int x, final int y) {
        return x > this.x && x < this.x + 100 && y > this.y && y < this.y + 12;
    }
}
