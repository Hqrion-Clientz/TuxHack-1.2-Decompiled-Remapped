//Deobfuscated with https://github.com/PetoPetko/Minecraft-Deobfuscator3000 using mappings "1.12 stable mappings"!

// 
// Decompiled by Procyon v0.5.36
// 

package me.ka1.vulcan.module.modules.misc;

import java.util.Iterator;
import me.ka1.vulcan.command.Command;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.init.MobEffects;
import java.util.Map;
import java.util.Collections;
import java.util.WeakHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import java.util.Set;
import me.ka1.vulcan.module.Module;

public class PotionDetect extends Module
{
    private final Set<EntityPlayer> str;
    public static final Minecraft mc;
    
    public PotionDetect() {
        super("Potiondetect", "Detects potions drunk", Category.Misc);
        this.str = Collections.newSetFromMap(new WeakHashMap<EntityPlayer, Boolean>());
    }
    
    @Override
    public int onUpdate() {
        for (final EntityPlayer player : PotionDetect.mc.world.playerEntities) {
            if (player.equals((Object)PotionDetect.mc.player)) {
                continue;
            }
            if (player.isPotionActive(MobEffects.STRENGTH) && !this.str.contains(player)) {
                Command.sendRawMessage(TextFormatting.DARK_RED + player.getDisplayNameString() + " Has Strength");
                this.str.add(player);
            }
            if (!this.str.contains(player)) {
                continue;
            }
            if (player.isPotionActive(MobEffects.STRENGTH)) {
                continue;
            }
            Command.sendRawMessage(TextFormatting.DARK_RED + player.getDisplayNameString() + " Has Ran Out Of Strength");
            this.str.remove(player);
        }
        return 0;
    }
    
    static {
        mc = Minecraft.getMinecraft();
    }
}
