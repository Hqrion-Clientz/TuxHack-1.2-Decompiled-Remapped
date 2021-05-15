//Deobfuscated with https://github.com/PetoPetko/Minecraft-Deobfuscator3000 using mappings "1.12 stable mappings"!

// 
// Decompiled by Procyon v0.5.36
// 

package me.ka1.vulcan.module.modules.misc;

import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.GuiScreen;
import me.ka1.vulcan.module.Module;

public class BackPack extends Module
{
    private GuiScreen echestScreen;
    
    public BackPack() {
        super("BackPack", "Allows to open your echest later.", Category.Misc);
        this.echestScreen = null;
    }
    
    @Override
    public int onUpdate() {
        final Container container;
        final InventoryBasic basic;
        if (BackPack.mc.currentScreen instanceof GuiContainer && (container = ((GuiContainer)BackPack.mc.currentScreen).inventorySlots) instanceof ContainerChest && ((ContainerChest)container).getLowerChestInventory() instanceof InventoryBasic && (basic = (InventoryBasic)((ContainerChest)container).getLowerChestInventory()).getName().equalsIgnoreCase("Ender Chest")) {
            this.echestScreen = BackPack.mc.currentScreen;
            BackPack.mc.currentScreen = null;
        }
        return 0;
    }
    
    public void onDisable() {
        if (!fullNullCheck() && this.echestScreen != null) {
            BackPack.mc.displayGuiScreen(this.echestScreen);
        }
        this.echestScreen = null;
    }
    
    private static boolean fullNullCheck() {
        return false;
    }
}
