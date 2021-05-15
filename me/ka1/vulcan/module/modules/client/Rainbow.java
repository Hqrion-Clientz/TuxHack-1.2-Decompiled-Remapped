// 
// Decompiled by Procyon v0.5.36
// 

package me.ka1.vulcan.module.modules.client;

import me.ka1.vulcan.setting.Setting;
import me.ka1.vulcan.module.Module;

public class Rainbow extends Module
{
    Setting.Integer speed;
    static int RainbowOffset;
    
    public Rainbow() {
        super("Rainbow", Category.Client);
    }
    
    @Override
    public void setup() {
        this.speed = this.registerInteger("Speed", "speed", 2, 1, 12);
        super.setup();
    }
    
    @Override
    public void onRender() {
        Rainbow.RainbowOffset += this.speed.getValue();
        super.onRender();
    }
    
    public static int getRainbowOffset() {
        return Rainbow.RainbowOffset;
    }
    
    static {
        Rainbow.RainbowOffset = 0;
    }
}
