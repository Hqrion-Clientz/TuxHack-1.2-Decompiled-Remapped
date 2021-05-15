//Deobfuscated with https://github.com/PetoPetko/Minecraft-Deobfuscator3000 using mappings "1.12 stable mappings"!

// 
// Decompiled by Procyon v0.5.36
// 

package me.ka1.vulcan.module.modules.combat;

import java.util.Iterator;
import net.minecraft.item.ItemStack;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.Vec3i;
import net.minecraft.init.Items;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.util.EnumHand;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayer;
import me.ka1.vulcan.module.ModuleManager;
import net.minecraft.util.math.BlockPos;
import me.ka1.vulcan.setting.Setting;
import me.ka1.vulcan.module.Module;

public class AutoMend extends Module
{
    int delay;
    Setting.Integer ThrowDelay;
    private final BlockPos[] surroundOffset;
    
    public AutoMend() {
        super("AutoMend", "Automatically mends your armor", Category.Combat);
        this.delay = 0;
        this.surroundOffset = new BlockPos[] { new BlockPos(0, -1, 0), new BlockPos(0, 0, -1), new BlockPos(1, 0, 0), new BlockPos(0, 0, 1), new BlockPos(-1, 0, 0) };
    }
    
    @Override
    public void setup() {
        this.ThrowDelay = this.registerInteger("Throw Delay", "throwDelay", 2, 1, 10);
    }
    
    @Override
    public int onUpdate() {
        final int ArmorDurability = this.getArmorDurability();
        final boolean safe = this.isSafe();
        final boolean AutoCrystal = ModuleManager.isModuleEnabled("AutoCrystal");
        final BlockPos q = AutoMend.mc.player.getPosition();
        if (!AutoCrystal && AutoMend.mc.player.isSneaking() && safe && 0 < ArmorDurability) {
            ++this.delay;
            if (this.delay % this.ThrowDelay.getValue() == 0) {
                AutoMend.mc.player.inventory.currentItem = this.findExpInHotbar();
                AutoMend.mc.player.connection.sendPacket((Packet)new CPacketPlayer.Rotation(0.0f, 90.0f, true));
                AutoMend.mc.player.connection.sendPacket((Packet)new CPacketPlayerTryUseItem(EnumHand.MAIN_HAND));
            }
        }
        else {
            this.delay = 0;
        }
        super.onUpdate();
        return ArmorDurability;
    }
    
    private int findExpInHotbar() {
        int slot = 0;
        for (int i = 0; i < 9; ++i) {
            if (AutoMend.mc.player.inventory.getStackInSlot(i).getItem() == Items.EXPERIENCE_BOTTLE) {
                slot = i;
                break;
            }
        }
        return slot;
    }
    
    private boolean isSafe() {
        boolean safe = true;
        final BlockPos playerPos = getPlayerPos();
        for (final BlockPos offset : this.surroundOffset) {
            final Block block = AutoMend.mc.world.getBlockState(playerPos.add((Vec3i)offset)).getBlock();
            if (block != Blocks.BEDROCK && block != Blocks.OBSIDIAN && block != Blocks.ENDER_CHEST && block != Blocks.ANVIL) {
                safe = false;
                break;
            }
        }
        return safe;
    }
    
    private static BlockPos getPlayerPos() {
        return new BlockPos(Math.floor(AutoMend.mc.player.posX), Math.floor(AutoMend.mc.player.posY), Math.floor(AutoMend.mc.player.posZ));
    }
    
    private int getArmorDurability() {
        int TotalDurability = 0;
        for (final ItemStack itemStack : AutoMend.mc.player.inventory.armorInventory) {
            TotalDurability += itemStack.getItemDamage();
        }
        return TotalDurability;
    }
}
