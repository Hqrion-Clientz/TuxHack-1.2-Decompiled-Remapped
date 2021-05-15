//Deobfuscated with https://github.com/PetoPetko/Minecraft-Deobfuscator3000 using mappings "1.12 stable mappings"!

// 
// Decompiled by Procyon v0.5.36
// 

package me.ka1.vulcan.module.modules.movement;

import com.mojang.realmsclient.gui.ChatFormatting;
import java.text.DecimalFormat;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayer;
import me.ka1.vulcan.util.MovementUtils;
import net.minecraft.entity.Entity;
import me.ka1.vulcan.util.entities.EntityUtil;
import java.util.List;
import java.util.ArrayList;
import me.ka1.vulcan.setting.Setting;
import me.ka1.vulcan.module.Module;

public class Step extends Module
{
    private int ticks;
    Setting.Double height;
    Setting.Boolean timer;
    Setting.Boolean reverse;
    Setting.Mode mode;
    
    public Step() {
        super("Step", "Lets you step up blocks", Category.Movement);
        this.ticks = 0;
    }
    
    @Override
    public void setup() {
        final ArrayList<String> modes = new ArrayList<String>();
        modes.add("Normal");
        modes.add("Vanilla");
        this.height = this.registerDouble("Height", "stepHeight", 2.5, 0.5, 2.5);
        this.timer = this.registerBoolean("Timer", "stepTimer", false);
        this.reverse = this.registerBoolean("Reverse", "stepRev", false);
        this.mode = this.registerMode("Modes", "stepMode", modes, "Normal");
    }
    
    @Override
    public int onUpdate() {
        if (Step.mc.world == null || Step.mc.player == null || Step.mc.player.isInWater() || Step.mc.player.isInLava() || Step.mc.player.isOnLadder() || Step.mc.gameSettings.keyBindJump.isKeyDown()) {
            return 0;
        }
        if (this.mode.getValue().equalsIgnoreCase("Normal")) {
            if (this.timer.getValue()) {
                if (this.ticks == 0) {
                    EntityUtil.resetTimer();
                }
                else {
                    --this.ticks;
                }
            }
            if (Step.mc.player != null && Step.mc.player.onGround && !Step.mc.player.isInWater() && !Step.mc.player.isOnLadder() && this.reverse.getValue()) {
                for (double y = 0.0; y < this.height.getValue() + 0.5; y += 0.01) {
                    if (!Step.mc.world.getCollisionBoxes((Entity)Step.mc.player, Step.mc.player.getEntityBoundingBox().offset(0.0, -y, 0.0)).isEmpty()) {
                        Step.mc.player.motionY = -10.0;
                        break;
                    }
                }
            }
            final double[] dir = MovementUtils.forward(0.1);
            boolean twofive = false;
            boolean two = false;
            boolean onefive = false;
            boolean one = false;
            if (Step.mc.world.getCollisionBoxes((Entity)Step.mc.player, Step.mc.player.getEntityBoundingBox().offset(dir[0], 2.6, dir[1])).isEmpty() && !Step.mc.world.getCollisionBoxes((Entity)Step.mc.player, Step.mc.player.getEntityBoundingBox().offset(dir[0], 2.4, dir[1])).isEmpty()) {
                twofive = true;
            }
            if (Step.mc.world.getCollisionBoxes((Entity)Step.mc.player, Step.mc.player.getEntityBoundingBox().offset(dir[0], 2.1, dir[1])).isEmpty() && !Step.mc.world.getCollisionBoxes((Entity)Step.mc.player, Step.mc.player.getEntityBoundingBox().offset(dir[0], 1.9, dir[1])).isEmpty()) {
                two = true;
            }
            if (Step.mc.world.getCollisionBoxes((Entity)Step.mc.player, Step.mc.player.getEntityBoundingBox().offset(dir[0], 1.6, dir[1])).isEmpty() && !Step.mc.world.getCollisionBoxes((Entity)Step.mc.player, Step.mc.player.getEntityBoundingBox().offset(dir[0], 1.4, dir[1])).isEmpty()) {
                onefive = true;
            }
            if (Step.mc.world.getCollisionBoxes((Entity)Step.mc.player, Step.mc.player.getEntityBoundingBox().offset(dir[0], 1.0, dir[1])).isEmpty() && !Step.mc.world.getCollisionBoxes((Entity)Step.mc.player, Step.mc.player.getEntityBoundingBox().offset(dir[0], 0.6, dir[1])).isEmpty()) {
                one = true;
            }
            if (Step.mc.player.collidedHorizontally && (Step.mc.player.moveForward != 0.0f || Step.mc.player.moveStrafing != 0.0f) && Step.mc.player.onGround) {
                if (one && this.height.getValue() >= 1.0) {
                    final double[] oneOffset = { 0.42, 0.753 };
                    for (int i = 0; i < oneOffset.length; ++i) {
                        Step.mc.player.connection.sendPacket((Packet)new CPacketPlayer.Position(Step.mc.player.posX, Step.mc.player.posY + oneOffset[i], Step.mc.player.posZ, Step.mc.player.onGround));
                    }
                    if (this.timer.getValue()) {
                        EntityUtil.setTimer(0.6f);
                    }
                    Step.mc.player.setPosition(Step.mc.player.posX, Step.mc.player.posY + 1.0, Step.mc.player.posZ);
                    this.ticks = 1;
                }
                if (onefive && this.height.getValue() >= 1.5) {
                    final double[] oneFiveOffset = { 0.42, 0.75, 1.0, 1.16, 1.23, 1.2 };
                    for (int i = 0; i < oneFiveOffset.length; ++i) {
                        Step.mc.player.connection.sendPacket((Packet)new CPacketPlayer.Position(Step.mc.player.posX, Step.mc.player.posY + oneFiveOffset[i], Step.mc.player.posZ, Step.mc.player.onGround));
                    }
                    if (this.timer.getValue()) {
                        EntityUtil.setTimer(0.35f);
                    }
                    Step.mc.player.setPosition(Step.mc.player.posX, Step.mc.player.posY + 1.5, Step.mc.player.posZ);
                    this.ticks = 1;
                }
                if (two && this.height.getValue() >= 2.0) {
                    final double[] twoOffset = { 0.42, 0.78, 0.63, 0.51, 0.9, 1.21, 1.45, 1.43 };
                    for (int i = 0; i < twoOffset.length; ++i) {
                        Step.mc.player.connection.sendPacket((Packet)new CPacketPlayer.Position(Step.mc.player.posX, Step.mc.player.posY + twoOffset[i], Step.mc.player.posZ, Step.mc.player.onGround));
                    }
                    if (this.timer.getValue()) {
                        EntityUtil.setTimer(0.25f);
                    }
                    Step.mc.player.setPosition(Step.mc.player.posX, Step.mc.player.posY + 2.0, Step.mc.player.posZ);
                    this.ticks = 2;
                }
                if (twofive && this.height.getValue() >= 2.5) {
                    final double[] twoFiveOffset = { 0.425, 0.821, 0.699, 0.599, 1.022, 1.372, 1.652, 1.869, 2.019, 1.907 };
                    for (int i = 0; i < twoFiveOffset.length; ++i) {
                        Step.mc.player.connection.sendPacket((Packet)new CPacketPlayer.Position(Step.mc.player.posX, Step.mc.player.posY + twoFiveOffset[i], Step.mc.player.posZ, Step.mc.player.onGround));
                    }
                    if (this.timer.getValue()) {
                        EntityUtil.setTimer(0.15f);
                    }
                    Step.mc.player.setPosition(Step.mc.player.posX, Step.mc.player.posY + 2.5, Step.mc.player.posZ);
                    this.ticks = 2;
                }
            }
        }
        if (this.mode.getValue().equalsIgnoreCase("Vanilla")) {
            final DecimalFormat df = new DecimalFormat("#");
            Step.mc.player.stepHeight = Float.parseFloat(df.format(this.height.getValue()));
        }
        return 0;
    }
    
    public void onDisable() {
        Step.mc.player.stepHeight = 0.5f;
    }
    
    @Override
    public String getHudInfo() {
        String t = "";
        if (this.mode.getValue().equalsIgnoreCase("Normal")) {
            t = "[" + ChatFormatting.WHITE + "Normal" + ChatFormatting.GRAY + "]";
        }
        if (this.mode.getValue().equalsIgnoreCase("Vanilla")) {
            t = "[" + ChatFormatting.WHITE + "Vanilla" + ChatFormatting.GRAY + "]";
        }
        return t;
    }
}
