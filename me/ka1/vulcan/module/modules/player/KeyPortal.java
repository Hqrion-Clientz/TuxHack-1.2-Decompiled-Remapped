//Deobfuscated with https://github.com/PetoPetko/Minecraft-Deobfuscator3000 using mappings "1.12 stable mappings"!

// 
// Decompiled by Procyon v0.5.36
// 

package me.ka1.vulcan.module.modules.player;

import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.util.EnumHand;
import net.minecraft.init.Items;
import me.ka1.vulcan.module.Module;

public class KeyPortal extends Module
{
    int oldSlot;
    int tick;
    
    public KeyPortal() {
        super("KeyPortal", "lights a portal on toggle", Category.Player);
        this.oldSlot = 0;
        this.tick = 0;
    }
    
    @Override
    protected void onEnable() {
        this.oldSlot = KeyPortal.mc.player.inventory.currentItem;
        KeyPortal.mc.player.inventory.currentItem = this.findPearlInHotbar();
    }
    
    @Override
    public int onUpdate() {
        ++this.tick;
        if (KeyPortal.mc.player.inventory.getStackInSlot(KeyPortal.mc.player.inventory.currentItem).getItem() == Items.FLINT_AND_STEEL && this.tick == 3) {
            KeyPortal.mc.player.connection.sendPacket((Packet)new CPacketPlayerTryUseItem(EnumHand.MAIN_HAND));
        }
        else if (KeyPortal.mc.player.inventory.getStackInSlot(KeyPortal.mc.player.inventory.currentItem).getItem() != Items.FLINT_AND_STEEL && this.tick == 3) {
            KeyPortal.mc.player.inventory.currentItem = this.findPearlInHotbar();
            KeyPortal.mc.player.connection.sendPacket((Packet)new CPacketPlayerTryUseItem(EnumHand.MAIN_HAND));
        }
        if (KeyPortal.mc.player.inventory.getStackInSlot(KeyPortal.mc.player.inventory.currentItem).getItem() == Items.FLINT_AND_STEEL && this.tick == 10) {
            this.disable();
        }
        return 0;
    }
    
    @Override
    protected void onDisable() {
        KeyPortal.mc.player.inventory.currentItem = this.oldSlot;
        this.tick = 0;
    }
    
    private int findPearlInHotbar() {
        int slot = 0;
        for (int i = 0; i < 9; ++i) {
            if (KeyPortal.mc.player.inventory.getStackInSlot(i).getItem() == Items.FLINT_AND_STEEL) {
                slot = i;
                break;
            }
        }
        return slot;
    }
}
