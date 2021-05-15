//Deobfuscated with https://github.com/PetoPetko/Minecraft-Deobfuscator3000 using mappings "1.12 stable mappings"!

// 
// Decompiled by Procyon v0.5.36
// 

package me.ka1.vulcan.module.modules.combat;

import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.util.EnumHand;
import net.minecraft.init.Items;
import me.ka1.vulcan.module.Module;

public class EasyPearl extends Module
{
    int oldSlot;
    int tick;
    
    public EasyPearl() {
        super("EasyPearl", "Throws a pearl on toggle", Category.Combat);
        this.oldSlot = 0;
        this.tick = 0;
    }
    
    @Override
    protected void onEnable() {
        this.oldSlot = EasyPearl.mc.player.inventory.currentItem;
        EasyPearl.mc.player.inventory.currentItem = this.findPearlInHotbar();
    }
    
    @Override
    public int onUpdate() {
        ++this.tick;
        if (EasyPearl.mc.player.inventory.getStackInSlot(EasyPearl.mc.player.inventory.currentItem).getItem() == Items.ENDER_PEARL && this.tick == 3) {
            EasyPearl.mc.player.connection.sendPacket((Packet)new CPacketPlayerTryUseItem(EnumHand.MAIN_HAND));
        }
        else if (EasyPearl.mc.player.inventory.getStackInSlot(EasyPearl.mc.player.inventory.currentItem).getItem() != Items.ENDER_PEARL && this.tick == 3) {
            EasyPearl.mc.player.inventory.currentItem = this.findPearlInHotbar();
            EasyPearl.mc.player.connection.sendPacket((Packet)new CPacketPlayerTryUseItem(EnumHand.MAIN_HAND));
        }
        if (EasyPearl.mc.player.inventory.getStackInSlot(EasyPearl.mc.player.inventory.currentItem).getItem() == Items.ENDER_PEARL && this.tick == 6) {
            this.disable();
        }
        return 0;
    }
    
    @Override
    protected void onDisable() {
        EasyPearl.mc.player.inventory.currentItem = this.oldSlot;
        this.tick = 0;
    }
    
    private int findPearlInHotbar() {
        int slot = 0;
        for (int i = 0; i < 9; ++i) {
            if (EasyPearl.mc.player.inventory.getStackInSlot(i).getItem() == Items.ENDER_PEARL) {
                slot = i;
                break;
            }
        }
        return slot;
    }
}
