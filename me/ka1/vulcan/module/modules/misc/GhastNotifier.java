//Deobfuscated with https://github.com/PetoPetko/Minecraft-Deobfuscator3000 using mappings "1.12 stable mappings"!

// 
// Decompiled by Procyon v0.5.36
// 

package me.ka1.vulcan.module.modules.misc;

import java.util.Iterator;
import net.minecraft.init.SoundEvents;
import me.ka1.vulcan.command.Command;
import net.minecraft.entity.monster.EntityGhast;
import java.util.HashSet;
import net.minecraft.entity.Entity;
import java.util.Set;
import me.ka1.vulcan.module.Module;

public class GhastNotifier extends Module
{
    private static GhastNotifier instance;
    private Set<Entity> entities;
    
    public GhastNotifier() {
        super("GhastNotifier", "tells you when ghasts spawn for EZ crystals", Category.Misc);
        this.entities = new HashSet<Entity>();
        GhastNotifier.instance = this;
    }
    
    public void onEnable() {
        this.entities.clear();
    }
    
    @Override
    public int onUpdate() {
        for (final Entity entity : GhastNotifier.mc.world.loadedEntityList) {
            if (entity instanceof EntityGhast) {
                if (this.entities.contains(entity)) {
                    continue;
                }
                Command.sendRawMessage("Ghast @: X: " + entity.posX + " Y: " + entity.posY + " Z: " + entity.posZ);
                this.entities.add(entity);
                entity.glowing = true;
                GhastNotifier.mc.player.playSound(SoundEvents.ENTITY_ARROW_HIT_PLAYER, 1.0f, 1.0f);
            }
        }
        return 0;
    }
}
