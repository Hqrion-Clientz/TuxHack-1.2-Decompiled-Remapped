//Deobfuscated with https://github.com/PetoPetko/Minecraft-Deobfuscator3000 using mappings "1.12 stable mappings"!

// 
// Decompiled by Procyon v0.5.36
// 

package me.ka1.vulcan.module.modules.combat;

import java.util.Iterator;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.util.EnumHand;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayer;
import me.ka1.vulcan.command.Command;
import me.ka1.vulcan.util.BurrowUtil;
import net.minecraft.block.BlockFence;
import me.ka1.vulcan.util.friend.Friends;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.Entity;
import me.ka1.vulcan.util.MovementUtils;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import me.ka1.vulcan.setting.Setting;
import me.ka1.vulcan.module.Module;

public class BurrowBypass extends Module
{
    Setting.Boolean rotate;
    Setting.Double offset;
    Setting.Boolean smart;
    Setting.Double smartDistance;
    private BlockPos originalPos;
    private int oldSlot;
    
    public BurrowBypass() {
        super("BurrowBypass", "Bypasses NCP's burrow patch", Category.Combat);
        this.oldSlot = -1;
    }
    
    @Override
    public void setup() {
        this.rotate = this.registerBoolean("Rotate", "RotateBurrow", false);
        this.offset = this.registerDouble("Offset", "offsetBurrow", 2.0, -5.0, 5.0);
        this.smart = this.registerBoolean("Smart", "smartBurrow", false);
        this.smartDistance = this.registerDouble("Distance", "distanceBurrow", 2.5, 1.0, 10.0);
    }
    
    public void onEnable() {
        super.onEnable();
        this.originalPos = new BlockPos(BurrowBypass.mc.player.posX, BurrowBypass.mc.player.posY, BurrowBypass.mc.player.posZ);
        if (BurrowBypass.mc.world.getBlockState(new BlockPos(BurrowBypass.mc.player.posX, BurrowBypass.mc.player.posY, BurrowBypass.mc.player.posZ)).getBlock().equals(Blocks.SPRUCE_FENCE) || this.intersectsWithEntity(this.originalPos)) {
            this.toggle();
            return;
        }
        this.oldSlot = BurrowBypass.mc.player.inventory.currentItem;
    }
    
    @Override
    public int onUpdate() {
        if (this.smart.getValue() && MovementUtils.isInHole((Entity)BurrowBypass.mc.player)) {
            BurrowBypass.mc.world.loadedEntityList.stream().filter(e -> e instanceof EntityPlayer).filter(e -> e != BurrowBypass.mc.player).forEach(e -> {
                if (Friends.isFriend(e.getName())) {
                    return;
                }
                else {
                    if (e.getDistance((Entity)BurrowBypass.mc.player) + 0.22f <= this.smartDistance.getValue()) {
                        this.doShit();
                    }
                    return;
                }
            });
        }
        else {
            this.doShit();
        }
        return 0;
    }
    
    public void doShit() {
        if (BurrowUtil.findHotbarBlock(BlockFence.class) == -1) {
            Command.sendClientMessage("NO FENCES!");
            this.toggle();
            return;
        }
        BurrowUtil.switchToSlot(BurrowUtil.findHotbarBlock(BlockFence.class));
        BurrowBypass.mc.player.connection.sendPacket((Packet)new CPacketPlayer.Position(BurrowBypass.mc.player.posX, BurrowBypass.mc.player.posY + 0.41999998688698, BurrowBypass.mc.player.posZ, true));
        BurrowBypass.mc.player.connection.sendPacket((Packet)new CPacketPlayer.Position(BurrowBypass.mc.player.posX, BurrowBypass.mc.player.posY + 0.7531999805211997, BurrowBypass.mc.player.posZ, true));
        BurrowBypass.mc.player.connection.sendPacket((Packet)new CPacketPlayer.Position(BurrowBypass.mc.player.posX, BurrowBypass.mc.player.posY + 1.00133597911214, BurrowBypass.mc.player.posZ, true));
        BurrowBypass.mc.player.connection.sendPacket((Packet)new CPacketPlayer.Position(BurrowBypass.mc.player.posX, BurrowBypass.mc.player.posY + 1.16610926093821, BurrowBypass.mc.player.posZ, true));
        BurrowUtil.placeBlock(this.originalPos, EnumHand.MAIN_HAND, this.rotate.getValue(), true, false);
        BurrowBypass.mc.player.connection.sendPacket((Packet)new CPacketPlayer.Position(BurrowBypass.mc.player.posX, BurrowBypass.mc.player.posY + this.offset.getValue(), BurrowBypass.mc.player.posZ, false));
        BurrowUtil.switchToSlot(this.oldSlot);
        this.toggle();
    }
    
    private boolean intersectsWithEntity(final BlockPos pos) {
        for (final Entity entity : BurrowBypass.mc.world.loadedEntityList) {
            if (entity.equals((Object)BurrowBypass.mc.player)) {
                continue;
            }
            if (entity instanceof EntityItem) {
                continue;
            }
            if (new AxisAlignedBB(pos).intersects(entity.getEntityBoundingBox())) {
                return true;
            }
        }
        return false;
    }
}
