//Deobfuscated with https://github.com/PetoPetko/Minecraft-Deobfuscator3000 using mappings "1.12 stable mappings"!

// 
// Decompiled by Procyon v0.5.36
// 

package me.ka1.vulcan.util;

import java.util.Arrays;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.Vec3i;
import net.minecraft.block.Block;
import net.minecraft.util.EnumHand;
import java.util.ArrayList;
import net.minecraft.world.World;
import net.minecraft.entity.projectile.EntityEgg;
import net.minecraft.item.ItemBlock;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import java.util.List;
import net.minecraft.client.Minecraft;

public class BlockInteractionHelper
{
    private static final Minecraft mc;
    public static final List blackList;
    public static final List shulkerList;
    
    private static float[] getLegitRotations(final Vec3d var0) {
        final Vec3d var = getEyesPos();
        final double var2 = var0.x - var.x;
        final double var3 = var0.y - var.y;
        final double var4 = var0.z - var.z;
        final double var5 = Math.sqrt(var2 * var2 + var4 * var4);
        final float var6 = (float)Math.toDegrees(Math.atan2(var4, var2)) - 90.0f;
        final float var7 = (float)(-Math.toDegrees(Math.atan2(var3, var5)));
        return new float[] { Wrapper.getPlayer().rotationYaw + MathHelper.wrapDegrees(var6 - Wrapper.getPlayer().rotationYaw), Wrapper.getPlayer().rotationPitch + MathHelper.wrapDegrees(var7 - Wrapper.getPlayer().rotationPitch) };
    }
    
    public static boolean canBeClicked(final BlockPos var0) {
        return getBlock(var0).canCollideCheck(getState(var0), false);
    }
    
    private static IBlockState getState(final BlockPos var0) {
        return Wrapper.getWorld().getBlockState(var0);
    }
    
    public static EnumFacing getPlaceableSide(final BlockPos var0) {
        for (final EnumFacing var4 : EnumFacing.values()) {
            final BlockPos var5 = var0.offset(var4);
            if (BlockInteractionHelper.mc.world.getBlockState(var5).getBlock().canCollideCheck(BlockInteractionHelper.mc.world.getBlockState(var5), false)) {
                final IBlockState var6 = BlockInteractionHelper.mc.world.getBlockState(var5);
                if (!var6.getMaterial().isReplaceable()) {
                    return var4;
                }
            }
        }
        return null;
    }
    
    public static float getYaw(final Entity var0) {
        final double var = var0.posX - BlockInteractionHelper.mc.player.posX;
        final double var2 = var0.posZ - BlockInteractionHelper.mc.player.posZ;
        double var3;
        if (var2 < 0.0 && var < 0.0) {
            var3 = 90.0 + Math.toDegrees(Math.atan(var2 / var));
        }
        else if (var2 < 0.0 && var > 0.0) {
            var3 = -90.0 + Math.toDegrees(Math.atan(var2 / var));
        }
        else {
            var3 = Math.toDegrees(-Math.atan(var / var2));
        }
        return MathHelper.wrapDegrees(-(BlockInteractionHelper.mc.player.rotationYaw - (float)var3));
    }
    
    public static void faceVectorPacketInstant(final Vec3d var0) {
        final float[] var = getLegitRotations(var0);
        Wrapper.getPlayer().connection.sendPacket((Packet)new CPacketPlayer.Rotation(var[0], var[1], Wrapper.getPlayer().onGround));
    }
    
    public static boolean hotbarSlotCheckEmpty(final ItemStack var0) {
        return var0 != ItemStack.EMPTY;
    }
    
    public static double[] calculateLookAt(final double var0, final double var2, final double var4, final EntityPlayer var6) {
        double var7 = var6.posX - var0;
        double var8 = var6.posY - var2;
        double var9 = var6.posZ - var4;
        final double var10 = Math.sqrt(var7 * var7 + var8 * var8 + var9 * var9);
        var7 /= var10;
        var8 /= var10;
        var9 /= var10;
        double var11 = Math.asin(var8);
        double var12 = Math.atan2(var9, var7);
        var11 = var11 * 180.0 / 3.141592653589793;
        var12 = var12 * 180.0 / 3.141592653589793;
        var12 += 90.0;
        return new double[] { var12, var11 };
    }
    
    public static void rotate(final float var0, final float var1) {
        Minecraft.getMinecraft().player.rotationYaw = var0;
        Minecraft.getMinecraft().player.rotationPitch = var1;
    }
    
    private static PlayerControllerMP getPlayerController() {
        return Minecraft.getMinecraft().playerController;
    }
    
    private static Vec3d getEyesPos() {
        return new Vec3d(Wrapper.getPlayer().posX, Wrapper.getPlayer().posY + Wrapper.getPlayer().getEyeHeight(), Wrapper.getPlayer().posZ);
    }
    
    public static boolean blockCheckNonBlock(final ItemStack var0) {
        return var0.getItem() instanceof ItemBlock;
    }
    
    public static float[] getDirectionToBlock(final int var0, final int var1, final int var2, final EnumFacing var3) {
        final EntityEgg var4 = new EntityEgg((World)BlockInteractionHelper.mc.world);
        var4.posX = var0 + 0.5;
        var4.posY = var1 + 0.5;
        var4.posZ = var2 + 0.5;
        final EntityEgg entityEgg = var4;
        entityEgg.posX += var3.getDirectionVec().getX() * 0.25;
        final EntityEgg entityEgg2 = var4;
        entityEgg2.posY += var3.getDirectionVec().getY() * 0.25;
        final EntityEgg entityEgg3 = var4;
        entityEgg3.posZ += var3.getDirectionVec().getZ() * 0.25;
        return getDirectionToEntity((Entity)var4);
    }
    
    public static List getSphere(final BlockPos var0, final float var1, final int var2, final boolean var3, final boolean var4, final int var5) {
        final ArrayList var6 = new ArrayList();
        final int var7 = var0.getX();
        final int var8 = var0.getY();
        final int var9 = var0.getZ();
        for (int var10 = var7 - (int)var1; var10 <= var7 + var1; ++var10) {
            for (int var11 = var9 - (int)var1; var11 <= var9 + var1; ++var11) {
                for (int var12 = var4 ? (var8 - (int)var1) : var8; var12 < (var4 ? (var8 + var1) : ((float)(var8 + var2))); ++var12) {
                    final double var13 = (var7 - var10) * (var7 - var10) + (var9 - var11) * (var9 - var11) + (var4 ? ((var8 - var12) * (var8 - var12)) : 0);
                    if (var13 < var1 * var1 && (!var3 || var13 >= (var1 - 1.0f) * (var1 - 1.0f))) {
                        final BlockPos var14 = new BlockPos(var10, var12 + var5, var11);
                        var6.add(var14);
                    }
                }
            }
        }
        return var6;
    }
    
    private static void processRightClickBlock(final BlockPos var0, final EnumFacing var1, final Vec3d var2) {
        getPlayerController().processRightClickBlock(Wrapper.getPlayer(), BlockInteractionHelper.mc.world, var0, var1, var2, EnumHand.MAIN_HAND);
    }
    
    public static float[] getRotationNeededForBlock(final EntityPlayer var0, final BlockPos var1) {
        final double var2 = var1.getX() - var0.posX;
        final double var3 = var1.getY() + 0.5 - (var0.posY + var0.getEyeHeight());
        final double var4 = var1.getZ() - var0.posZ;
        final double var5 = Math.sqrt(var2 * var2 + var4 * var4);
        final float var6 = (float)(Math.atan2(var4, var2) * 180.0 / 3.141592653589793) - 90.0f;
        final float var7 = (float)(-(Math.atan2(var3, var5) * 180.0 / 3.141592653589793));
        return new float[] { var6, var7 };
    }
    
    private static Block getBlock(final BlockPos var0) {
        return getState(var0).getBlock();
    }
    
    public static void placeBlockScaffold(final BlockPos var0) {
        final Vec3d var = new Vec3d(Wrapper.getPlayer().posX, Wrapper.getPlayer().posY + Wrapper.getPlayer().getEyeHeight(), Wrapper.getPlayer().posZ);
        for (final EnumFacing var5 : EnumFacing.values()) {
            final BlockPos var6 = var0.offset(var5);
            final EnumFacing var7 = var5.getOpposite();
            if (canBeClicked(var6)) {
                final Vec3d var8 = new Vec3d((Vec3i)var6).add(0.5, 0.5, 0.5).add(new Vec3d(var7.getDirectionVec()).rotatePitch(0.5f));
                if (var.squareDistanceTo(var8) <= 18.0625) {
                    faceVectorPacketInstant(var8);
                    processRightClickBlock(var6, var7, var8);
                    Wrapper.getPlayer().swingArm(EnumHand.MAIN_HAND);
                    BlockInteractionHelper.mc.rightClickDelayTimer = 4;
                    return;
                }
            }
        }
    }
    
    public static boolean checkForNeighbours(final BlockPos var0) {
        if (!hasNeighbour(var0)) {
            for (final EnumFacing var4 : EnumFacing.values()) {
                final BlockPos var5 = var0.offset(var4);
                if (hasNeighbour(var5)) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }
    
    public static void rotate(final double[] var0) {
        Minecraft.getMinecraft().player.rotationYaw = (float)var0[0];
        Minecraft.getMinecraft().player.rotationPitch = (float)var0[1];
    }
    
    private static boolean hasNeighbour(final BlockPos var0) {
        for (final EnumFacing var4 : EnumFacing.values()) {
            final BlockPos var5 = var0.offset(var4);
            if (!Wrapper.getWorld().getBlockState(var5).getMaterial().isReplaceable()) {
                return true;
            }
        }
        return false;
    }
    
    public static float getPitch(final Entity var0) {
        final double var = var0.posX - BlockInteractionHelper.mc.player.posX;
        final double var2 = var0.posZ - BlockInteractionHelper.mc.player.posZ;
        final double var3 = var0.posY - 1.6 + var0.getEyeHeight() - BlockInteractionHelper.mc.player.posY;
        final double var4 = MathHelper.sqrt(var * var + var2 * var2);
        final double var5 = -Math.toDegrees(Math.atan(var3 / var4));
        return -MathHelper.wrapDegrees(BlockInteractionHelper.mc.player.rotationPitch - (float)var5);
    }
    
    private static float[] getDirectionToEntity(final Entity var0) {
        return new float[] { getYaw(var0) + BlockInteractionHelper.mc.player.rotationYaw, getPitch(var0) + BlockInteractionHelper.mc.player.rotationPitch };
    }
    
    private static float wrapAngleTo180(float var0) {
        for (var0 %= 360.0f; var0 >= 180.0f; var0 -= 360.0f) {}
        while (var0 < -180.0f) {
            var0 += 360.0f;
        }
        return var0;
    }
    
    public static void lookAtBlock(final BlockPos var0) {
        rotate(calculateLookAt(var0.getX(), var0.getY(), var0.getZ(), (EntityPlayer)Minecraft.getMinecraft().player));
    }
    
    public static List getCircle(final BlockPos var0, final int var1, final float var2, final boolean var3) {
        final ArrayList var4 = new ArrayList();
        final int var5 = var0.getX();
        final int var6 = var0.getZ();
        for (int var7 = var5 - (int)var2; var7 <= var5 + var2; ++var7) {
            for (int var8 = var6 - (int)var2; var8 <= var6 + var2; ++var8) {
                final double var9 = (var5 - var7) * (var5 - var7) + (var6 - var8) * (var6 - var8);
                if (var9 < var2 * var2 && (!var3 || var9 >= (var2 - 1.0f) * (var2 - 1.0f))) {
                    final BlockPos var10 = new BlockPos(var7, var1, var8);
                    var4.add(var10);
                }
            }
        }
        return var4;
    }
    
    static {
        blackList = Arrays.asList(Blocks.ENDER_CHEST, (Block)Blocks.CHEST, Blocks.TRAPPED_CHEST, Blocks.CRAFTING_TABLE, Blocks.ANVIL, Blocks.BREWING_STAND, (Block)Blocks.HOPPER, Blocks.DROPPER, Blocks.DISPENSER);
        shulkerList = Arrays.asList(Blocks.WHITE_SHULKER_BOX, Blocks.ORANGE_SHULKER_BOX, Blocks.MAGENTA_SHULKER_BOX, Blocks.LIGHT_BLUE_SHULKER_BOX, Blocks.YELLOW_SHULKER_BOX, Blocks.LIME_SHULKER_BOX, Blocks.PINK_SHULKER_BOX, Blocks.GRAY_SHULKER_BOX, Blocks.SILVER_SHULKER_BOX, Blocks.CYAN_SHULKER_BOX, Blocks.PURPLE_SHULKER_BOX, Blocks.BLUE_SHULKER_BOX, Blocks.BROWN_SHULKER_BOX, Blocks.GREEN_SHULKER_BOX, Blocks.RED_SHULKER_BOX, Blocks.BLACK_SHULKER_BOX);
        mc = Minecraft.getMinecraft();
    }
}
