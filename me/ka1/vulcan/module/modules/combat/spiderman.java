//Deobfuscated with https://github.com/PetoPetko/Minecraft-Deobfuscator3000 using mappings "1.12 stable mappings"!

// 
// Decompiled by Procyon v0.5.36
// 

package me.ka1.vulcan.module.modules.combat;

import java.util.Iterator;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.util.EnumHand;
import me.ka1.vulcan.command.Command;
import me.ka1.vulcan.util.BurrowUtil;
import net.minecraft.block.BlockWeb;
import me.ka1.vulcan.util.friend.Friends;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.Entity;
import me.ka1.vulcan.util.MovementUtils;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import me.ka1.vulcan.setting.Setting;
import me.ka1.vulcan.module.Module;

public class spiderman extends Module
{
    Setting.Boolean rotate;
    Setting.Double offset;
    Setting.Boolean smart;
    Setting.Double smartDistance;
    private BlockPos originalPos;
    private int oldSlot;
    
    public spiderman() {
        super("Spiderman", "places a web at your feet.", Category.Combat);
        this.oldSlot = -1;
    }
    
    @Override
    public void setup() {
        this.rotate = this.registerBoolean("Rotate", "Rotateweb", false);
        this.offset = this.registerDouble("Offset", "offsetweb", 2.0, -2.0, 4.0);
        this.smart = this.registerBoolean("Smart", "smartBurrow", false);
        this.smartDistance = this.registerDouble("Distance", "distanceBurrow", 2.5, 1.0, 7.0);
    }
    
    public void onEnable() {
        super.onEnable();
        spiderman.mc.player.sendChatMessage("webbed!");
        this.originalPos = new BlockPos(spiderman.mc.player.posX, spiderman.mc.player.posY, spiderman.mc.player.posZ);
        if (spiderman.mc.world.getBlockState(new BlockPos(spiderman.mc.player.posX, spiderman.mc.player.posY, spiderman.mc.player.posZ)).getBlock().equals(Blocks.WEB) || this.intersectsWithEntity(this.originalPos)) {
            this.toggle();
            return;
        }
        this.oldSlot = spiderman.mc.player.inventory.currentItem;
    }
    
    @Override
    public int onUpdate() {
        if (this.smart.getValue() && MovementUtils.isInHole((Entity)spiderman.mc.player)) {
            spiderman.mc.world.loadedEntityList.stream().filter(e -> e instanceof EntityPlayer).filter(e -> e != spiderman.mc.player).forEach(e -> {
                if (Friends.isFriend(e.getName())) {
                    return;
                }
                else {
                    if (e.getDistance((Entity)spiderman.mc.player) + 0.22f <= this.smartDistance.getValue()) {
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
        if (BurrowUtil.findHotbarBlock(BlockWeb.class) == -1) {
            Command.sendClientMessage("NO WEBS DUMBASS");
            this.toggle();
            return;
        }
        BurrowUtil.switchToSlot(BurrowUtil.findHotbarBlock(BlockWeb.class));
        BurrowUtil.placeBlock(this.originalPos, EnumHand.MAIN_HAND, this.rotate.getValue(), true, false);
        BurrowUtil.switchToSlot(this.oldSlot);
        this.toggle();
    }
    
    private boolean intersectsWithEntity(final BlockPos pos) {
        for (final Entity entity : spiderman.mc.world.loadedEntityList) {
            if (entity.equals((Object)spiderman.mc.player)) {
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
