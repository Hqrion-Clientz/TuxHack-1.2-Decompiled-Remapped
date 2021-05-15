//Deobfuscated with https://github.com/PetoPetko/Minecraft-Deobfuscator3000 using mappings "1.12 stable mappings"!

// 
// Decompiled by Procyon v0.5.36
// 

package me.ka1.vulcan.module.modules.combat;

import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextFormatting;
import me.ka1.vulcan.Vulcan;
import net.minecraft.entity.Entity;
import me.ka1.vulcan.module.ModuleManager;
import net.minecraft.item.ItemSword;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.Comparator;
import me.ka1.vulcan.util.friend.Friends;
import net.minecraft.entity.player.EntityPlayer;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketUseEntity;
import me.zero.alpine.listener.EventHandler;
import me.ka1.vulcan.event.events.PacketEvent;
import me.zero.alpine.listener.Listener;
import me.ka1.vulcan.setting.Setting;
import me.ka1.vulcan.module.Module;

public class KillAura extends Module
{
    private Setting.Boolean swordOnly;
    private Setting.Boolean caCheck;
    private Setting.Boolean criticals;
    private Setting.Double range;
    private String targetName;
    private boolean isAttacking;
    @EventHandler
    private final Listener<PacketEvent.Send> listener;
    
    public KillAura() {
        super("KillAura", "Automatically smacks people", Category.Combat);
        this.isAttacking = false;
        this.listener = new Listener<PacketEvent.Send>(event -> {
            if (event.getPacket() instanceof CPacketUseEntity && this.criticals.getValue() && ((CPacketUseEntity)event.getPacket()).getAction() == CPacketUseEntity.Action.ATTACK && KillAura.mc.player.onGround && this.isAttacking) {
                KillAura.mc.player.connection.sendPacket((Packet)new CPacketPlayer.Position(KillAura.mc.player.posX, KillAura.mc.player.posY + 0.10000000149011612, KillAura.mc.player.posZ, false));
                KillAura.mc.player.connection.sendPacket((Packet)new CPacketPlayer.Position(KillAura.mc.player.posX, KillAura.mc.player.posY, KillAura.mc.player.posZ, false));
            }
        }, (Predicate<PacketEvent.Send>[])new Predicate[0]);
    }
    
    @Override
    public void setup() {
        this.range = this.registerDouble("Range", "Range", 5.0, 0.0, 10.0);
        this.swordOnly = this.registerBoolean("Sword Only", "SwordOnly", true);
        this.criticals = this.registerBoolean("Criticals", "Criticals", true);
        this.caCheck = this.registerBoolean("AC Check", "ACCheck", false);
    }
    
    @Override
    public int onUpdate() {
        if (KillAura.mc.player == null || KillAura.mc.player.isDead) {
            return 0;
        }
        final List<Entity> targets = (List<Entity>)KillAura.mc.world.loadedEntityList.stream().filter(entity -> entity != KillAura.mc.player).filter(entity -> KillAura.mc.player.getDistance(entity) <= this.range.getValue()).filter(entity -> !entity.isDead).filter(entity -> entity instanceof EntityPlayer).filter(entity -> entity.getHealth() > 0.0f).filter(entity -> !Friends.isFriend(entity.getName())).sorted(Comparator.comparing(e -> KillAura.mc.player.getDistance(e))).collect(Collectors.toList());
        targets.forEach(target -> {
            if (this.swordOnly.getValue() && !(KillAura.mc.player.getHeldItemMainhand().getItem() instanceof ItemSword)) {
                return;
            }
            else if (this.caCheck.getValue() && ((AutoCrystal)ModuleManager.getModuleByName("AutoCrystal")).isActive) {
                return;
            }
            else {
                if (target.getDistance((Entity)KillAura.mc.player) < this.range.getValue() && target != null) {
                    this.targetName = target.getName();
                }
                else if (target == null || target.getDistance((Entity)KillAura.mc.player) > this.range.getValue()) {
                    this.targetName = "No target!";
                }
                this.attack(target);
                return;
            }
        });
        return 0;
    }
    
    public void onEnable() {
        Vulcan.EVENT_BUS.subscribe(this);
    }
    
    public void onDisable() {
        Vulcan.EVENT_BUS.unsubscribe(this);
    }
    
    @Override
    public String getHudInfo() {
        final String s = TextFormatting.GRAY + "[" + TextFormatting.WHITE + this.targetName + TextFormatting.GRAY + "]";
        return s;
    }
    
    public void attack(final Entity e) {
        if (KillAura.mc.player.getCooledAttackStrength(0.0f) >= 1.0f) {
            this.isAttacking = true;
            KillAura.mc.playerController.attackEntity((EntityPlayer)KillAura.mc.player, e);
            KillAura.mc.player.swingArm(EnumHand.MAIN_HAND);
            this.isAttacking = false;
        }
    }
}
