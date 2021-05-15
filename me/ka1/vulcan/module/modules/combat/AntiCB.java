//Deobfuscated with https://github.com/PetoPetko/Minecraft-Deobfuscator3000 using mappings "1.12 stable mappings"!

// 
// Decompiled by Procyon v0.5.36
// 

package me.ka1.vulcan.module.modules.combat;

import net.minecraft.block.BlockObsidian;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import java.util.Iterator;
import net.minecraft.block.Block;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3i;
import me.ka1.vulcan.util.BlockInteractionHelper;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockAir;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import me.ka1.vulcan.setting.Setting;
import me.ka1.vulcan.module.Module;

public class AntiCB extends Module
{
    Setting.Boolean sneak;
    Setting.Boolean rotate;
    Setting.Boolean triggerable;
    Setting.Integer timeoutTicks;
    Setting.Boolean autoCenter;
    Setting.Integer blocksPerTick;
    Setting.Integer tickDelay;
    Setting.Boolean jumpDisable;
    private int totalTicksRunning;
    private int playerHotbarSlot;
    private int lastHotbarSlot;
    private int offsetStep;
    private int delayStep;
    private boolean firstRun;
    private boolean isSneaking;
    private Vec3d playerPos;
    private BlockPos basePos;
    
    public AntiCB() {
        super("Anti-cityboss", "makes it hard to city you", Category.Combat);
        this.totalTicksRunning = 0;
        this.playerHotbarSlot = -1;
        this.lastHotbarSlot = -1;
        this.offsetStep = 0;
        this.delayStep = 0;
        this.isSneaking = false;
    }
    
    @Override
    public void setup() {
        this.sneak = this.registerBoolean("Sneak", "sneak", false);
        this.rotate = this.registerBoolean("Rotate", "rotate", false);
        this.triggerable = this.registerBoolean("AutoDisable", "AutoDisable", false);
        this.blocksPerTick = this.registerInteger("BlocksPerTick", "blocksPerTick", 4, 1, 8);
        this.tickDelay = this.registerInteger("Delay", "delay", 0, 0, 10);
        this.timeoutTicks = this.registerInteger("Timeout", "timeout", 2, 0, 20);
        this.autoCenter = this.registerBoolean("Centre", "autoCentre", true);
        this.jumpDisable = this.registerBoolean("GroundCheck", "jumpDisable", true);
    }
    
    private void centerPlayer(final double x, final double y, final double z) {
        AntiCB.mc.player.connection.sendPacket((Packet)new CPacketPlayer.Position(x, y, z, true));
        AntiCB.mc.player.setPosition(x, y, z);
    }
    
    double getDst(final Vec3d vec) {
        return this.playerPos.distanceTo(vec);
    }
    
    @Override
    protected void onEnable() {
        if (AntiCB.mc.player == null) {
            this.disable();
            return;
        }
        final BlockPos centerPos = AntiCB.mc.player.getPosition();
        this.playerPos = AntiCB.mc.player.getPositionVector();
        final double y = centerPos.getY();
        double x = centerPos.getX();
        double z = centerPos.getZ();
        final Vec3d plusPlus = new Vec3d(x + 0.5, y, z + 0.5);
        final Vec3d plusMinus = new Vec3d(x + 0.5, y, z - 0.5);
        final Vec3d minusMinus = new Vec3d(x - 0.5, y, z - 0.5);
        final Vec3d minusPlus = new Vec3d(x - 0.5, y, z + 0.5);
        if (this.autoCenter.getValue()) {
            if (this.getDst(plusPlus) < this.getDst(plusMinus) && this.getDst(plusPlus) < this.getDst(minusMinus) && this.getDst(plusPlus) < this.getDst(minusPlus)) {
                x = centerPos.getX() + 0.5;
                z = centerPos.getZ() + 0.5;
                this.centerPlayer(x, y, z);
            }
            if (this.getDst(plusMinus) < this.getDst(plusPlus) && this.getDst(plusMinus) < this.getDst(minusMinus) && this.getDst(plusMinus) < this.getDst(minusPlus)) {
                x = centerPos.getX() + 0.5;
                z = centerPos.getZ() - 0.5;
                this.centerPlayer(x, y, z);
            }
            if (this.getDst(minusMinus) < this.getDst(plusPlus) && this.getDst(minusMinus) < this.getDst(plusMinus) && this.getDst(minusMinus) < this.getDst(minusPlus)) {
                x = centerPos.getX() - 0.5;
                z = centerPos.getZ() - 0.5;
                this.centerPlayer(x, y, z);
            }
            if (this.getDst(minusPlus) < this.getDst(plusPlus) && this.getDst(minusPlus) < this.getDst(plusMinus) && this.getDst(minusPlus) < this.getDst(minusMinus)) {
                x = centerPos.getX() - 0.5;
                z = centerPos.getZ() + 0.5;
                this.centerPlayer(x, y, z);
            }
        }
        this.firstRun = true;
        this.playerHotbarSlot = AntiCB.mc.player.inventory.currentItem;
        this.lastHotbarSlot = -1;
    }
    
    @Override
    protected void onDisable() {
        if (AntiCB.mc.player == null) {
            return;
        }
        if (this.lastHotbarSlot != this.playerHotbarSlot && this.playerHotbarSlot != -1) {
            AntiCB.mc.player.inventory.currentItem = this.playerHotbarSlot;
        }
        if (this.isSneaking) {
            AntiCB.mc.player.connection.sendPacket((Packet)new CPacketEntityAction((Entity)AntiCB.mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
            this.isSneaking = false;
        }
        this.playerHotbarSlot = -1;
        this.lastHotbarSlot = -1;
    }
    
    @Override
    public int onUpdate() {
        if (this.sneak.getValue() && !AntiCB.mc.gameSettings.keyBindSneak.isKeyDown()) {
            return 0;
        }
        if (this.triggerable.getValue() && this.totalTicksRunning >= this.timeoutTicks.getValue()) {
            this.totalTicksRunning = 0;
            this.disable();
            return 0;
        }
        if (this.jumpDisable.getValue() && !AntiCB.mc.player.onGround) {
            this.disable();
            return 0;
        }
        if (!this.firstRun) {
            if (this.delayStep < this.tickDelay.getValue()) {
                ++this.delayStep;
                return 0;
            }
            this.delayStep = 0;
        }
        if (this.firstRun) {
            this.firstRun = false;
        }
        int blocksPlaced = 0;
        while (blocksPlaced < this.blocksPerTick.getValue()) {
            Vec3d[] offsetPattern = new Vec3d[0];
            int maxSteps = 0;
            offsetPattern = Offsets.SURROUND;
            maxSteps = Offsets.SURROUND.length;
            if (this.offsetStep >= maxSteps) {
                this.offsetStep = 0;
                break;
            }
            final BlockPos offsetPos = new BlockPos(offsetPattern[this.offsetStep]);
            final BlockPos targetPos = new BlockPos(AntiCB.mc.player.getPositionVector()).add(offsetPos.getX(), offsetPos.getY(), offsetPos.getZ());
            if (this.placeBlock(targetPos)) {
                ++blocksPlaced;
            }
            ++this.offsetStep;
        }
        if (blocksPlaced > 0) {
            if (this.lastHotbarSlot != this.playerHotbarSlot && this.playerHotbarSlot != -1) {
                AntiCB.mc.player.inventory.currentItem = this.playerHotbarSlot;
                this.lastHotbarSlot = this.playerHotbarSlot;
            }
            if (this.isSneaking) {
                AntiCB.mc.player.connection.sendPacket((Packet)new CPacketEntityAction((Entity)AntiCB.mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
                this.isSneaking = false;
            }
        }
        ++this.totalTicksRunning;
        return blocksPlaced;
    }
    
    private boolean placeBlock(final BlockPos pos) {
        final Block block = AntiCB.mc.world.getBlockState(pos).getBlock();
        if (!(block instanceof BlockAir) && !(block instanceof BlockLiquid)) {
            return false;
        }
        for (final Entity entity : AntiCB.mc.world.getEntitiesWithinAABBExcludingEntity((Entity)null, new AxisAlignedBB(pos))) {
            if (!(entity instanceof EntityItem) && !(entity instanceof EntityXPOrb)) {
                return false;
            }
        }
        final EnumFacing side = BlockInteractionHelper.getPlaceableSide(pos);
        if (side == null) {
            return false;
        }
        final BlockPos neighbour = pos.offset(side);
        final EnumFacing opposite = side.getOpposite();
        if (!BlockInteractionHelper.canBeClicked(neighbour)) {
            return false;
        }
        final Vec3d hitVec = new Vec3d((Vec3i)neighbour).add(0.5, 0.5, 0.5).add(new Vec3d(opposite.getDirectionVec()).scale(0.5));
        final Block neighbourBlock = AntiCB.mc.world.getBlockState(neighbour).getBlock();
        final int obiSlot = this.findObiInHotbar();
        if (obiSlot == -1) {
            this.disable();
        }
        if (this.lastHotbarSlot != obiSlot) {
            AntiCB.mc.player.inventory.currentItem = obiSlot;
            this.lastHotbarSlot = obiSlot;
        }
        if ((!this.isSneaking && BlockInteractionHelper.blackList.contains(neighbourBlock)) || BlockInteractionHelper.shulkerList.contains(neighbourBlock)) {
            AntiCB.mc.player.connection.sendPacket((Packet)new CPacketEntityAction((Entity)AntiCB.mc.player, CPacketEntityAction.Action.START_SNEAKING));
            this.isSneaking = true;
        }
        if (this.rotate.getValue()) {
            BlockInteractionHelper.faceVectorPacketInstant(hitVec);
        }
        AntiCB.mc.playerController.processRightClickBlock(AntiCB.mc.player, AntiCB.mc.world, neighbour, opposite, hitVec, EnumHand.MAIN_HAND);
        AntiCB.mc.player.swingArm(EnumHand.MAIN_HAND);
        AntiCB.mc.rightClickDelayTimer = 4;
        AntiCB.mc.player.connection.sendPacket((Packet)new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, neighbour, opposite));
        return true;
    }
    
    private int findObiInHotbar() {
        int slot = -1;
        for (int i = 0; i < 9; ++i) {
            final ItemStack stack = AntiCB.mc.player.inventory.getStackInSlot(i);
            if (stack != ItemStack.EMPTY) {
                if (stack.getItem() instanceof ItemBlock) {
                    final Block block = ((ItemBlock)stack.getItem()).getBlock();
                    if (block instanceof BlockObsidian) {
                        slot = i;
                        break;
                    }
                }
            }
        }
        return slot;
    }
    
    private enum Mode
    {
        SURROUND, 
        FULL;
    }
    
    private static class Offsets
    {
        private static final Vec3d[] SURROUND;
        private static final Vec3d[] FULL;
        
        static {
            SURROUND = new Vec3d[] { new Vec3d(1.0, 0.0, 0.0), new Vec3d(2.0, 0.0, 0.0), new Vec3d(1.0, 1.0, 0.0), new Vec3d(2.0, 1.0, 0.0) };
            FULL = new Vec3d[] { new Vec3d(1.0, 0.0, 0.0), new Vec3d(0.0, 0.0, 1.0), new Vec3d(-1.0, 0.0, 0.0), new Vec3d(0.0, 0.0, -1.0), new Vec3d(1.0, -1.0, 0.0), new Vec3d(0.0, -1.0, 1.0), new Vec3d(-1.0, -1.0, 0.0), new Vec3d(0.0, -1.0, -1.0), new Vec3d(0.0, -1.0, 0.0) };
        }
    }
}
