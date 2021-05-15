//Deobfuscated with https://github.com/PetoPetko/Minecraft-Deobfuscator3000 using mappings "1.12 stable mappings"!

// 
// Decompiled by Procyon v0.5.36
// 

package me.ka1.vulcan.module.modules.render;

import me.ka1.vulcan.util.RenderUtil;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.math.BlockPos;
import me.ka1.vulcan.setting.Setting;
import me.ka1.vulcan.module.Module;

public class HoleESP extends Module
{
    Setting.Mode mode;
    static Setting.Integer range;
    Setting.Integer r;
    Setting.Integer g;
    Setting.Integer b;
    Setting.Integer a;
    BlockPos blockPos;
    List<BlockPos> validBlockPos;
    
    public HoleESP() {
        super("HoleESP", "Higlights holes in the ground", Category.Render);
    }
    
    @Override
    public void setup() {
        final ArrayList<String> modes = new ArrayList<String>();
        modes.add("Gradient");
        modes.add("FlatOutline");
        modes.add("Flat");
        modes.add("Outline");
        this.mode = this.registerMode("Mode", "mode", modes, "Gradient");
        HoleESP.range = this.registerInteger("range", "range", 8, 4, 14);
        this.r = this.registerInteger("Red", "red", 100, 0, 255);
        this.g = this.registerInteger("Green", "green", 100, 0, 255);
        this.b = this.registerInteger("Blue", "blue", 100, 0, 255);
        this.a = this.registerInteger("Alpha", "alpha", 100, 0, 255);
    }
    
    @Override
    public int onUpdate() {
        if (!RenderUtil.worldCheck()) {
            return 0;
        }
        return 0;
    }
    
    List findHoles() {
        return this.validBlockPos;
    }
    
    public static BlockPos getPlayerPos() {
        return new BlockPos(Math.floor(HoleESP.mc.player.posX), Math.floor(HoleESP.mc.player.posY), Math.floor(HoleESP.mc.player.posZ));
    }
    
    static boolean IsHole(final BlockPos blockPos) {
        return true;
    }
}
