//Deobfuscated with https://github.com/PetoPetko/Minecraft-Deobfuscator3000 using mappings "1.12 stable mappings"!

// 
// Decompiled by Procyon v0.5.36
// 

package me.ka1.vulcan.module.modules.Hud;

import java.util.Iterator;
import net.minecraft.init.Blocks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.Entity;
import me.ka1.vulcan.Vulcan;
import me.ka1.vulcan.clickgui.ClickGUI;
import me.ka1.vulcan.module.modules.client.ClickGuiModule;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import me.ka1.vulcan.util.friend.Friends;
import java.util.function.ToIntFunction;
import net.minecraft.item.ItemStack;
import net.minecraft.init.Items;
import java.util.Comparator;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import java.util.List;
import java.util.ArrayList;
import me.ka1.vulcan.module.ModuleManager;
import me.ka1.vulcan.module.modules.combat.AutoCrystal;
import net.minecraft.util.math.BlockPos;
import java.awt.Color;
import me.ka1.vulcan.setting.Setting;
import me.ka1.vulcan.module.Module;

public class CombatInfo extends Module
{
    public static Setting.Mode CombatInfoWatermark;
    Setting.Integer infoX;
    Setting.Integer infoY;
    Setting.Mode GUIColorSync;
    Setting.Integer colorRed;
    Setting.Integer colorGreen;
    Setting.Integer colorBlue;
    Setting.Integer colorAlpha;
    Setting.Mode Mode;
    Color c;
    int totems;
    int color;
    private BlockPos[] surroundOffset;
    final AutoCrystal a;
    
    public CombatInfo() {
        super("CombatInfo", Category.Hud);
        this.a = (AutoCrystal)ModuleManager.getModuleByName("AutoCrystal");
    }
    
    @Override
    public void setup() {
        this.infoX = this.registerInteger("Combat info X", "Combat info X", 0, 0, 1280);
        this.infoY = this.registerInteger("Combat info Y", "Combat info Y", 150, 0, 960);
        final ArrayList<String> Modes = new ArrayList<String>();
        Modes.add("VULCAN");
        Modes.add("KAiKLiENT");
        Modes.add("YoshiWare");
        this.Mode = this.registerMode("Mode", "combatInfoMode", Modes, "VULCAN");
    }
    
    public int getPing() {
        int p = -1;
        if (CombatInfo.mc.player != null && CombatInfo.mc.getConnection() != null) {
            if (CombatInfo.mc.getConnection().getPlayerInfo(CombatInfo.mc.player.getName()) != null) {
                p = CombatInfo.mc.getConnection().getPlayerInfo(CombatInfo.mc.player.getName()).getResponseTime();
            }
        }
        return p;
    }
    
    @Override
    public void onRender() {
        final EntityOtherPlayerMP players = (EntityOtherPlayerMP)CombatInfo.mc.world.loadedEntityList.stream().filter(entity -> entity instanceof EntityOtherPlayerMP).filter(e -> CombatInfo.mc.player.getDistance(e) <= AutoCrystal.placeRange.getValue()).map(entity -> entity).min(Comparator.comparing(c -> CombatInfo.mc.player.getDistance(c))).orElse(null);
        this.totems = CombatInfo.mc.player.inventory.mainInventory.stream().filter(itemStack -> itemStack.getItem() == Items.TOTEM_OF_UNDYING).mapToInt(ItemStack::getCount).sum();
        if (CombatInfo.mc.player.getHeldItemOffhand().getItem() == Items.TOTEM_OF_UNDYING) {
            ++this.totems;
        }
        this.surroundOffset = new BlockPos[] { new BlockPos(0, 0, -1), new BlockPos(1, 0, 0), new BlockPos(0, 0, 1), new BlockPos(-1, 0, 0) };
        final List<EntityPlayer> entities = (List<EntityPlayer>)CombatInfo.mc.world.playerEntities.stream().filter(entityPlayer -> !Friends.isFriend(entityPlayer.getName())).collect(Collectors.toList());
        final Color on = new Color(0, 255, 0);
        final Color off = new Color(255, 0, 0);
        ClickGUI.color = new Color(ClickGuiModule.red.getValue(), ClickGuiModule.green.getValue(), ClickGuiModule.blue.getValue()).getRGB();
        Vulcan.fontRenderer.drawStringWithShadow(this.Mode.getValue(), this.infoX.getValue(), this.infoY.getValue(), ClickGUI.color);
        if (players != null && CombatInfo.mc.player.getDistance((Entity)players) <= AutoCrystal.range.getValue()) {
            Vulcan.fontRenderer.drawStringWithShadow("HTR", this.infoX.getValue(), this.infoY.getValue() + 10, on.getRGB());
        }
        else {
            Vulcan.fontRenderer.drawStringWithShadow("HTR", this.infoX.getValue(), this.infoY.getValue() + 10, off.getRGB());
        }
        if (players != null && CombatInfo.mc.player.getDistance((Entity)players) <= AutoCrystal.placeRange.getValue()) {
            Vulcan.fontRenderer.drawStringWithShadow("PLR", this.infoX.getValue(), this.infoY.getValue() + 20, on.getRGB());
        }
        else {
            Vulcan.fontRenderer.drawStringWithShadow("PLR", this.infoX.getValue(), this.infoY.getValue() + 20, off.getRGB());
        }
        if (this.totems > 1) {
            Vulcan.fontRenderer.drawStringWithShadow(this.totems + "", this.infoX.getValue(), this.infoY.getValue() + 30, on.getRGB());
        }
        else {
            Vulcan.fontRenderer.drawStringWithShadow(this.totems + "", this.infoX.getValue(), this.infoY.getValue() + 30, off.getRGB());
        }
        if (this.getPing() > 100) {
            Vulcan.fontRenderer.drawStringWithShadow("PING " + this.getPing(), this.infoX.getValue(), this.infoY.getValue() + 40, off.getRGB());
        }
        else {
            Vulcan.fontRenderer.drawStringWithShadow("PING " + this.getPing(), this.infoX.getValue(), this.infoY.getValue() + 40, on.getRGB());
        }
        for (final EntityPlayer e2 : entities) {
            int i = 0;
            for (final BlockPos add : this.surroundOffset) {
                ++i;
                final BlockPos o = new BlockPos(e2.getPositionVector().x, e2.getPositionVector().y, e2.getPositionVector().z).add(add.getX(), add.getY(), add.getZ());
                if (CombatInfo.mc.world.getBlockState(o).getBlock() == Blocks.OBSIDIAN) {
                    if (i == 1 && this.a.canPlaceCrystal(o.north(1).down())) {
                        Vulcan.fontRenderer.drawStringWithShadow("LBY", this.infoX.getValue(), this.infoY.getValue() + 50, on.getRGB());
                    }
                    if (i == 2 && this.a.canPlaceCrystal(o.east(1).down())) {
                        Vulcan.fontRenderer.drawStringWithShadow("LBY", this.infoX.getValue(), this.infoY.getValue() + 50, on.getRGB());
                    }
                    if (i == 3 && this.a.canPlaceCrystal(o.south(1).down())) {
                        Vulcan.fontRenderer.drawStringWithShadow("LBY", this.infoX.getValue(), this.infoY.getValue() + 50, on.getRGB());
                    }
                    if (i == 4 && this.a.canPlaceCrystal(o.west(1).down())) {
                        Vulcan.fontRenderer.drawStringWithShadow("LBY", this.infoX.getValue(), this.infoY.getValue() + 50, on.getRGB());
                    }
                }
                else {
                    Vulcan.fontRenderer.drawStringWithShadow("LBY", this.infoX.getValue(), this.infoY.getValue() + 50, off.getRGB());
                }
            }
        }
    }
}
