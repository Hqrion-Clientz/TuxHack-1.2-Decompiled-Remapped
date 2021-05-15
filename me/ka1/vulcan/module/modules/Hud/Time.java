// 
// Decompiled by Procyon v0.5.36
// 

package me.ka1.vulcan.module.modules.Hud;

import java.text.DateFormat;
import java.awt.Color;
import me.ka1.vulcan.Vulcan;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ArrayList;
import me.ka1.vulcan.setting.Setting;
import me.ka1.vulcan.module.Module;

public class Time extends Module
{
    Setting.Mode timeFormat;
    Setting.Integer r;
    Setting.Integer g;
    Setting.Integer b;
    Setting.Integer x;
    Setting.Integer y;
    
    public Time() {
        super("Time", "Displays the time", Category.Hud);
    }
    
    @Override
    public void setup() {
        final ArrayList<String> timeFormats = new ArrayList<String>();
        timeFormats.add("24hr");
        timeFormats.add("12hr");
        this.timeFormat = this.registerMode("Time Format", "Format", timeFormats, "24hr");
        this.r = this.registerInteger("Red", "red", 255, 0, 255);
        this.g = this.registerInteger("Green", "green", 255, 0, 255);
        this.b = this.registerInteger("Blue", "blue", 255, 0, 255);
        this.x = this.registerInteger("X", "x", 0, 0, 1280);
        this.y = this.registerInteger("Y", "y", 40, 0, 960);
    }
    
    @Override
    public void onRender() {
        final DateFormat twoFour = new SimpleDateFormat("hh:mm aa");
        final DateFormat oneTwo = new SimpleDateFormat("HH:mm");
        final Date dt = new Date();
        final int hours = dt.getHours();
        final int minutes = dt.getMinutes();
        if (this.timeFormat.getValue().equalsIgnoreCase("24hr")) {
            final String time = twoFour.format(dt);
            Vulcan.fontRenderer.drawStringWithShadow(time, this.x.getValue(), this.y.getValue(), new Color(this.r.getValue(), this.g.getValue(), this.b.getValue(), 255).getRGB());
        }
        else if (this.timeFormat.getValue().equalsIgnoreCase("12hr")) {
            final String time = oneTwo.format(dt);
            Vulcan.fontRenderer.drawStringWithShadow(time, this.x.getValue(), this.y.getValue(), new Color(this.r.getValue(), this.g.getValue(), this.b.getValue(), 255).getRGB());
        }
    }
}
