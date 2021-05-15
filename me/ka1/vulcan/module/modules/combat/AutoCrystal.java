//Deobfuscated with https://github.com/PetoPetko/Minecraft-Deobfuscator3000 using mappings "1.12 stable mappings"!

// 
// Decompiled by Procyon v0.5.36
// 

package me.ka1.vulcan.module.modules.combat;

import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.potion.Potion;
import net.minecraft.util.math.MathHelper;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.util.CombatRules;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraft.world.Explosion;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.init.Blocks;
import net.minecraft.entity.EntityLivingBase;
import me.ka1.vulcan.Vulcan;
import net.minecraft.client.renderer.GlStateManager;
import me.ka1.vulcan.util.RenderUtil;
import java.awt.Color;
import me.ka1.vulcan.event.events.RenderEvent;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.math.Vec3d;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import me.ka1.vulcan.util.friend.Friends;
import java.util.Collection;
import net.minecraft.init.Items;
import net.minecraft.util.EnumHand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemTool;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemStack;
import net.minecraft.init.MobEffects;
import java.util.Comparator;
import java.util.List;
import java.util.Iterator;
import net.minecraft.network.Packet;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.client.Minecraft;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundCategory;
import net.minecraft.network.play.server.SPacketSoundEffect;
import java.util.function.Predicate;
import net.minecraft.network.play.client.CPacketPlayer;
import me.zero.alpine.listener.EventHandler;
import me.ka1.vulcan.event.events.PacketEvent;
import me.zero.alpine.listener.Listener;
import java.util.ArrayList;
import me.ka1.vulcan.setting.Setting;
import net.minecraft.util.EnumFacing;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import me.ka1.vulcan.module.Module;

public class AutoCrystal extends Module
{
    private BlockPos render;
    private Entity renderEnt;
    private boolean switchCooldown;
    private boolean isAttacking;
    private boolean isPlacing;
    private boolean isBreaking;
    private int oldSlot;
    private int newSlot;
    private int waitCounter;
    EnumFacing f;
    private static boolean togglePitch;
    Setting.Boolean explode;
    Setting.Boolean dynamic;
    Setting.Boolean antiWeakness;
    Setting.Boolean place;
    Setting.Boolean raytrace;
    Setting.Boolean rotate;
    Setting.Boolean spoofRotations;
    Setting.Boolean showDamage;
    Setting.Boolean cacheBreak;
    Setting.Boolean singlePlace;
    Setting.Boolean antiSuicide;
    Setting.Boolean autoSwitch;
    Setting.Boolean endCrystalMode;
    Setting.Integer placeDelay;
    Setting.Integer antiSuicideValue;
    Setting.Integer facePlace;
    Setting.Integer attackSpeed;
    Setting.Double maxSelfDmg;
    Setting.Double minBreakDmg;
    Setting.Double enemyRange;
    Setting.Double walls;
    Setting.Double minDmg;
    Setting.Mode renderMode;
    Setting.Boolean singleTick;
    public static String targetName;
    public static Setting.Double range;
    public static Setting.Double placeRange;
    Setting.Mode handBreak;
    Setting.Mode breakMode;
    Setting.Integer colorRed;
    Setting.Integer colorGreen;
    Setting.Integer colorBlue;
    Setting.Integer colorAlpha;
    Setting.Integer outlineAlpha;
    Setting.Double outlineWidth;
    Setting.Boolean cancelCrystal;
    private final ArrayList<BlockPos> PlacedCrystals;
    public boolean isActive;
    private long breakSystemTime;
    private static boolean isSpoofingAngles;
    private static double yaw;
    private static double pitch;
    @EventHandler
    private final Listener<PacketEvent.Send> packetSendListener;
    @EventHandler
    private final Listener<PacketEvent.Receive> packetReceiveListener;
    
    public AutoCrystal() {
        super("AutoCrystal", "Automatically places and breaks crystals to do damage", Category.Combat);
        this.switchCooldown = false;
        this.isAttacking = false;
        this.isPlacing = false;
        this.isBreaking = false;
        this.oldSlot = -1;
        this.PlacedCrystals = new ArrayList<BlockPos>();
        this.isActive = false;
        final Packet packet;
        this.packetSendListener = new Listener<PacketEvent.Send>(event -> {
            packet = event.getPacket();
            if (packet instanceof CPacketPlayer && this.spoofRotations.getValue() && AutoCrystal.isSpoofingAngles) {
                ((CPacketPlayer)packet).yaw = (float)AutoCrystal.yaw;
                ((CPacketPlayer)packet).pitch = (float)AutoCrystal.pitch;
            }
            return;
        }, (Predicate<PacketEvent.Send>[])new Predicate[0]);
        SPacketSoundEffect packet2;
        final Iterator<Entity> iterator;
        Entity e;
        this.packetReceiveListener = new Listener<PacketEvent.Receive>(event -> {
            if (event.getPacket() instanceof SPacketSoundEffect) {
                packet2 = (SPacketSoundEffect)event.getPacket();
                if (packet2.getCategory() == SoundCategory.BLOCKS && packet2.getSound() == SoundEvents.ENTITY_GENERIC_EXPLODE) {
                    Minecraft.getMinecraft().world.loadedEntityList.iterator();
                    while (iterator.hasNext()) {
                        e = iterator.next();
                        if (e instanceof EntityEnderCrystal && e.getDistance(packet2.getX(), packet2.getY(), packet2.getZ()) <= 6.0) {
                            e.setDead();
                        }
                    }
                }
            }
        }, (Predicate<PacketEvent.Receive>[])new Predicate[0]);
    }
    
    @Override
    public void setup() {
        final ArrayList<String> hands = new ArrayList<String>();
        hands.add("Main");
        hands.add("Offhand");
        hands.add("Both");
        final ArrayList<String> breakModes = new ArrayList<String>();
        breakModes.add("All");
        breakModes.add("Smart");
        breakModes.add("Only Own");
        final ArrayList<String> renderModes = new ArrayList<String>();
        renderModes.add("Gradient");
        renderModes.add("Full");
        renderModes.add("Outline");
        renderModes.add("OutlineFill");
        this.cacheBreak = this.registerBoolean("Cache Break", "cacheBreak", false);
        this.singleTick = this.registerBoolean("Single Tick", "singleTick", false);
        this.dynamic = this.registerBoolean("Dynamic", "dynamic", true);
        this.explode = this.registerBoolean("Break", "Break", true);
        this.place = this.registerBoolean("Place", "Place", true);
        this.breakMode = this.registerMode("Break Modes", "BreakModes", breakModes, "All");
        this.handBreak = this.registerMode("SwingHand", "Hand", hands, "Main");
        this.antiSuicide = this.registerBoolean("Anti Suicide", "AntiSuicide", false);
        this.antiSuicideValue = this.registerInteger("Pause Health", "PauseHealth", 8, 0, 20);
        this.attackSpeed = this.registerInteger("Attack Speed", "AttackSpeed", 12, 0, 20);
        this.placeDelay = this.registerInteger("Place Delay", "PlaceDelay", 0, 0, 20);
        AutoCrystal.placeRange = this.registerDouble("Place Range", "PlaceRange", 6.0, 0.0, 6.0);
        AutoCrystal.range = this.registerDouble("Hit Range", "HitRange", 5.0, 0.0, 10.0);
        this.walls = this.registerDouble("Walls Range", "WallsRange", 3.2, 0.0, 4.0);
        this.enemyRange = this.registerDouble("Enemy Range", "EnemyRange", 6.0, 0.5, 12.0);
        this.antiWeakness = this.registerBoolean("Anti Weakness", "AntiWeakness", true);
        this.endCrystalMode = this.registerBoolean("1.13 Mode", "1.13Mode", false);
        this.singlePlace = this.registerBoolean("MultiPlace", "MultiPlace", false);
        this.autoSwitch = this.registerBoolean("Auto Switch", "AutoSwitch", false);
        this.minDmg = this.registerDouble("Min Damage", "MinDamage", 5.0, 0.0, 36.0);
        this.minBreakDmg = this.registerDouble("Min Break Dmg", "MinBreakDmg", 3.5, 1.0, 20.0);
        this.maxSelfDmg = this.registerDouble("Max Self Dmg", "MaxSelfDmg", 8.0, 1.0, 20.0);
        this.facePlace = this.registerInteger("FacePlace HP", "FacePlaceHP", 8, 0, 36);
        this.raytrace = this.registerBoolean("Raytrace", "Raytrace", false);
        this.rotate = this.registerBoolean("Rotate", "Rotate", false);
        this.spoofRotations = this.registerBoolean("Spoof Angles", "SpoofAngles", true);
        this.cancelCrystal = this.registerBoolean("Cancel Crystal", "Cancel Crystal", true);
        this.showDamage = this.registerBoolean("Show Damage", "ShowDamage", false);
        this.renderMode = this.registerMode("Render Mode", "renderMode", renderModes, "Gradient");
        this.colorRed = this.registerInteger("Red", "colorRed", 255, 0, 255);
        this.colorGreen = this.registerInteger("Green", "colorGreen", 255, 0, 255);
        this.colorBlue = this.registerInteger("Blue", "colorBlue", 255, 0, 255);
        this.colorAlpha = this.registerInteger("Alpha", "colorAlpha", 80, 0, 255);
        this.outlineAlpha = this.registerInteger("Outline Alpha", "outlineAlpha", 200, 0, 255);
        this.outlineWidth = this.registerDouble("Outline Width", "outlineWidth", 1.0, 0.0, 5.0);
    }
    
    @Override
    public int onUpdate() {
        this.isActive = false;
        this.isBreaking = false;
        this.isPlacing = false;
        final BlockPos hitBlock = null;
        if (AutoCrystal.mc.player == null || AutoCrystal.mc.player.isDead) {
            return 0;
        }
        final EntityEnderCrystal crystal = (EntityEnderCrystal)AutoCrystal.mc.world.loadedEntityList.stream().filter(entity -> entity instanceof EntityEnderCrystal).filter(e -> AutoCrystal.mc.player.getDistance(e) <= AutoCrystal.range.getValue()).filter(e -> this.crystalCheck(e)).map(entity -> entity).min(Comparator.comparing(c -> AutoCrystal.mc.player.getDistance(c))).orElse(null);
        if (this.explode.getValue() && crystal != null) {
            if (this.antiSuicide.getValue() && AutoCrystal.mc.player.getHealth() + AutoCrystal.mc.player.getAbsorptionAmount() < this.antiSuicideValue.getValue()) {
                return 0;
            }
            if (!AutoCrystal.mc.player.canEntityBeSeen((Entity)crystal) && AutoCrystal.mc.player.getDistance((Entity)crystal) > this.walls.getValue()) {
                return 0;
            }
            if (this.antiWeakness.getValue() && AutoCrystal.mc.player.isPotionActive(MobEffects.WEAKNESS)) {
                if (!this.isAttacking) {
                    this.oldSlot = AutoCrystal.mc.player.inventory.currentItem;
                    this.isAttacking = true;
                }
                this.newSlot = -1;
                for (int i = 0; i < 9; ++i) {
                    final ItemStack stack = AutoCrystal.mc.player.inventory.getStackInSlot(i);
                    if (stack != ItemStack.EMPTY) {
                        if (stack.getItem() instanceof ItemSword) {
                            this.newSlot = i;
                            break;
                        }
                        if (stack.getItem() instanceof ItemTool) {
                            this.newSlot = i;
                            break;
                        }
                    }
                }
                if (this.newSlot != -1) {
                    AutoCrystal.mc.player.inventory.currentItem = this.newSlot;
                    this.switchCooldown = true;
                }
            }
            if (System.nanoTime() / 1000000L - this.breakSystemTime >= 420 - this.attackSpeed.getValue() * 20) {
                this.isActive = true;
                this.isBreaking = true;
                if (this.rotate.getValue()) {
                    this.lookAtPacket(crystal.posX, crystal.posY, crystal.posZ, (EntityPlayer)AutoCrystal.mc.player);
                }
                AutoCrystal.mc.playerController.attackEntity((EntityPlayer)AutoCrystal.mc.player, (Entity)crystal);
                if (this.handBreak.getValue().equalsIgnoreCase("Both")) {
                    AutoCrystal.mc.player.swingArm(EnumHand.MAIN_HAND);
                    AutoCrystal.mc.player.swingArm(EnumHand.OFF_HAND);
                    if (this.cancelCrystal.getValue()) {
                        crystal.setDead();
                        AutoCrystal.mc.world.removeAllEntities();
                        AutoCrystal.mc.world.getLoadedEntityList();
                    }
                }
                else if (this.handBreak.getValue().equalsIgnoreCase("Offhand") && !AutoCrystal.mc.player.getHeldItemOffhand().isEmpty) {
                    AutoCrystal.mc.player.swingArm(EnumHand.OFF_HAND);
                    if (this.cancelCrystal.getValue()) {
                        crystal.setDead();
                        AutoCrystal.mc.world.removeAllEntities();
                        AutoCrystal.mc.world.getLoadedEntityList();
                    }
                }
                else {
                    AutoCrystal.mc.player.swingArm(EnumHand.MAIN_HAND);
                    if (this.cancelCrystal.getValue()) {
                        crystal.setDead();
                        AutoCrystal.mc.world.removeAllEntities();
                        AutoCrystal.mc.world.getLoadedEntityList();
                    }
                }
                if (this.cancelCrystal.getValue()) {
                    crystal.setDead();
                    AutoCrystal.mc.world.removeAllEntities();
                    AutoCrystal.mc.world.getLoadedEntityList();
                }
                this.breakSystemTime = System.nanoTime() / 1000000L;
                this.isActive = false;
                this.isBreaking = false;
            }
            if (!this.singlePlace.getValue()) {
                return 0;
            }
        }
        else {
            resetRotation();
            if (this.oldSlot != -1) {
                AutoCrystal.mc.player.inventory.currentItem = this.oldSlot;
                this.oldSlot = -1;
            }
            this.isAttacking = false;
            this.isActive = false;
            this.isBreaking = false;
        }
        int crystalSlot = (AutoCrystal.mc.player.getHeldItemMainhand().getItem() == Items.END_CRYSTAL) ? AutoCrystal.mc.player.inventory.currentItem : -1;
        if (crystalSlot == -1) {
            for (int l = 0; l < 9; ++l) {
                if (AutoCrystal.mc.player.inventory.getStackInSlot(l).getItem() == Items.END_CRYSTAL) {
                    crystalSlot = l;
                    break;
                }
            }
        }
        boolean offhand = false;
        if (AutoCrystal.mc.player.getHeldItemOffhand().getItem() == Items.END_CRYSTAL) {
            offhand = true;
        }
        else if (crystalSlot == -1) {
            return crystalSlot;
        }
        final List<BlockPos> blocks = this.findCrystalBlocks();
        final List<Entity> entities = new ArrayList<Entity>();
        entities.addAll((Collection<? extends Entity>)AutoCrystal.mc.world.playerEntities.stream().filter(entityPlayer -> !Friends.isFriend(entityPlayer.getName())).sorted(Comparator.comparing(e -> AutoCrystal.mc.player.getDistance(e))).collect(Collectors.toList()));
        BlockPos q = null;
        double damage = 0.5;
        final Iterator var9 = entities.iterator();
        while (true) {
            if (!var9.hasNext()) {
                if (damage == 0.5) {
                    this.render = null;
                    this.renderEnt = null;
                    resetRotation();
                    return crystalSlot;
                }
                this.render = q;
                if (this.place.getValue()) {
                    if (this.antiSuicide.getValue() && AutoCrystal.mc.player.getHealth() + AutoCrystal.mc.player.getAbsorptionAmount() < this.antiSuicideValue.getValue()) {
                        return crystalSlot;
                    }
                    if (!offhand && AutoCrystal.mc.player.inventory.currentItem != crystalSlot) {
                        if (this.autoSwitch.getValue()) {
                            AutoCrystal.mc.player.inventory.currentItem = crystalSlot;
                            resetRotation();
                            this.switchCooldown = true;
                        }
                        return crystalSlot;
                    }
                    if (this.rotate.getValue()) {
                        this.lookAtPacket(q.getX() + 0.5, q.getY() - 0.5, q.getZ() + 0.5, (EntityPlayer)AutoCrystal.mc.player);
                    }
                    final RayTraceResult result = AutoCrystal.mc.world.rayTraceBlocks(new Vec3d(AutoCrystal.mc.player.posX, AutoCrystal.mc.player.posY + AutoCrystal.mc.player.getEyeHeight(), AutoCrystal.mc.player.posZ), new Vec3d(q.getX() + 0.5, q.getY() - 0.5, q.getZ() + 0.5));
                    if (this.raytrace.getValue()) {
                        if (result == null || result.sideHit == null) {
                            q = null;
                            this.f = null;
                            this.render = null;
                            resetRotation();
                            this.isActive = false;
                            this.isPlacing = false;
                            return crystalSlot;
                        }
                        this.f = result.sideHit;
                    }
                    if (this.switchCooldown) {
                        this.switchCooldown = false;
                        return crystalSlot;
                    }
                    if (q != null && AutoCrystal.mc.player != null) {
                        this.isActive = true;
                        this.isPlacing = true;
                        if (this.raytrace.getValue() && this.f != null) {
                            AutoCrystal.mc.player.connection.sendPacket((Packet)new CPacketPlayerTryUseItemOnBlock(q, this.f, offhand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, 0.0f, 0.0f, 0.0f));
                        }
                        else if (q.getY() == 255) {
                            AutoCrystal.mc.player.connection.sendPacket((Packet)new CPacketPlayerTryUseItemOnBlock(q, EnumFacing.DOWN, offhand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, 0.0f, 0.0f, 0.0f));
                        }
                        else {
                            AutoCrystal.mc.player.connection.sendPacket((Packet)new CPacketPlayerTryUseItemOnBlock(q, EnumFacing.UP, offhand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, 0.0f, 0.0f, 0.0f));
                        }
                        final EntityEnderCrystal cry = (EntityEnderCrystal)AutoCrystal.mc.world.loadedEntityList.stream().filter(ent -> ent instanceof EntityEnderCrystal).filter(e -> AutoCrystal.mc.player.getDistance(e) <= AutoCrystal.range.getValue()).filter(e -> this.crystalCheck(e)).map(ent -> ent).min(Comparator.comparing(c -> AutoCrystal.mc.player.getDistance(c))).orElse(null);
                        AutoCrystal.mc.player.connection.sendPacket((Packet)new CPacketAnimation(EnumHand.MAIN_HAND));
                        this.PlacedCrystals.add(q);
                    }
                    if (AutoCrystal.isSpoofingAngles) {
                        if (AutoCrystal.togglePitch) {
                            final EntityPlayerSP var10 = AutoCrystal.mc.player;
                            var10.rotationPitch += (float)4.0E-4;
                            AutoCrystal.togglePitch = false;
                        }
                        else {
                            final EntityPlayerSP var10 = AutoCrystal.mc.player;
                            var10.rotationPitch -= (float)4.0E-4;
                            AutoCrystal.togglePitch = true;
                        }
                    }
                    return crystalSlot;
                }
            }
            final EntityPlayer entity2 = var9.next();
            if (entity2 != AutoCrystal.mc.player && entity2.getHealth() > 0.0f) {
                for (final BlockPos blockPos : blocks) {
                    final double b = entity2.getDistanceSq(blockPos);
                    final double x = blockPos.getX() + 0.0;
                    final double y = blockPos.getY() + 1.0;
                    final double z = blockPos.getZ() + 0.0;
                    if (entity2.getDistanceSq(x, y, z) < this.enemyRange.getValue() * this.enemyRange.getValue()) {
                        final double d = calculateDamage(blockPos.getX() + 0.5, blockPos.getY() + 1, blockPos.getZ() + 0.5, (Entity)entity2);
                        if (d <= damage) {
                            continue;
                        }
                        final double targetDamage = calculateDamage(blockPos.getX() + 0.5, blockPos.getY() + 1, blockPos.getZ() + 0.5, (Entity)entity2);
                        final float targetHealth = entity2.getHealth() + entity2.getAbsorptionAmount();
                        if (targetDamage < this.minDmg.getValue() && targetHealth > this.facePlace.getValue()) {
                            continue;
                        }
                        final double self = calculateDamage(blockPos.getX() + 0.5, blockPos.getY() + 1, blockPos.getZ() + 0.5, (Entity)AutoCrystal.mc.player);
                        if (self >= this.maxSelfDmg.getValue() || self >= AutoCrystal.mc.player.getHealth() + AutoCrystal.mc.player.getAbsorptionAmount()) {
                            continue;
                        }
                        damage = d;
                        q = blockPos;
                        this.renderEnt = (Entity)entity2;
                    }
                }
            }
        }
    }
    
    @Override
    public void onWorldRender(final RenderEvent event) {
        if (this.render != null) {
            if (this.renderMode.getValue().equalsIgnoreCase("Gradient")) {
                RenderUtil.drawGradientFilledBox(this.render, new Color(this.colorRed.getValue(), this.colorGreen.getValue(), this.colorBlue.getValue(), 45), new Color(this.colorRed.getValue(), this.colorGreen.getValue(), this.colorBlue.getValue(), 6));
                RenderUtil.prepare(7);
                RenderUtil.drawBoundingBoxBottom2(this.render, (float)this.outlineWidth.getValue(), this.colorRed.getValue(), this.colorGreen.getValue(), this.colorBlue.getValue(), this.outlineAlpha.getValue());
                RenderUtil.release();
            }
            else if (this.renderMode.getValue().equalsIgnoreCase("full")) {
                RenderUtil.prepare(7);
                RenderUtil.drawBox(this.render, new Color(this.colorRed.getValue(), this.colorGreen.getValue(), this.colorBlue.getValue(), this.colorAlpha.getValue()).getRGB(), 63);
                RenderUtil.release();
            }
            else if (this.renderMode.getValue().equalsIgnoreCase("Outline")) {
                RenderUtil.prepare(7);
                RenderUtil.drawBoundingBoxBlockPos(this.render, (float)this.outlineWidth.getValue(), this.colorRed.getValue(), this.colorGreen.getValue(), this.colorBlue.getValue(), this.outlineAlpha.getValue());
                RenderUtil.release();
            }
            else {
                if (!this.renderMode.getValue().equalsIgnoreCase("OutlineFill")) {
                    return;
                }
                RenderUtil.prepare(7);
                RenderUtil.drawBox(this.render, new Color(this.colorRed.getValue(), this.colorGreen.getValue(), this.colorBlue.getValue(), this.colorAlpha.getValue()).getRGB(), 63);
                RenderUtil.release();
                RenderUtil.prepare(7);
                RenderUtil.drawBoundingBoxBlockPos(this.render, (float)this.outlineWidth.getValue(), this.colorRed.getValue(), this.colorGreen.getValue(), this.colorBlue.getValue(), this.outlineAlpha.getValue());
                RenderUtil.release();
            }
        }
        if (this.showDamage.getValue() && this.render != null && this.renderEnt != null) {
            GlStateManager.pushMatrix();
            RenderUtil.glBillboardDistanceScaled(this.render.getX() + 0.5f, this.render.getY() + 0.5f, this.render.getZ() + 0.5f, (EntityPlayer)AutoCrystal.mc.player, 1.0f);
            final double d = calculateDamage(this.render.getX() + 0.5, this.render.getY() + 1, this.render.getZ() + 0.5, this.renderEnt);
            final String damageText = ((Math.floor(d) == d) ? Integer.valueOf((int)d) : String.format("%.1f", d)) + "";
            GlStateManager.disableDepth();
            GlStateManager.translate(-(AutoCrystal.mc.fontRenderer.getStringWidth(damageText) / 2.0), 0.0, 0.0);
            Vulcan.fontRenderer.drawStringWithShadow(damageText, 0.0, 0.0, new Color(255, 255, 255).getRGB());
            GlStateManager.popMatrix();
        }
    }
    
    private void lookAtPacket(final double px, final double py, final double pz, final EntityPlayer me) {
        final double[] v = calculateLookAt(px, py, pz, me);
        setYawAndPitch((float)v[0], (float)v[1]);
    }
    
    private boolean crystalCheck(final Entity crystal) {
        if (!(crystal instanceof EntityEnderCrystal)) {
            return false;
        }
        if (this.breakMode.getValue().equalsIgnoreCase("All")) {
            return true;
        }
        if (this.breakMode.getValue().equalsIgnoreCase("Only Own")) {
            for (final BlockPos pos : new ArrayList<BlockPos>(this.PlacedCrystals)) {
                if (pos != null && pos.getDistance((int)crystal.posX, (int)crystal.posY, (int)crystal.posZ) <= AutoCrystal.range.getValue()) {
                    return true;
                }
            }
        }
        if (!this.breakMode.getValue().equalsIgnoreCase("Smart")) {
            return false;
        }
        final EntityLivingBase target = (EntityLivingBase)((this.renderEnt != null) ? this.renderEnt : this.GetNearTarget(crystal));
        if (target.getDistance((Entity)AutoCrystal.mc.player) <= this.enemyRange.getValue() + 0.5 && target != null) {
            AutoCrystal.targetName = target.getDisplayName().getFormattedText();
        }
        else if (target.getDistance((Entity)AutoCrystal.mc.player) > this.enemyRange.getValue() || target == null) {
            AutoCrystal.targetName = "No target!";
        }
        if (target == null) {
            return false;
        }
        final float targetDmg = calculateDamage(crystal.posX + 0.5, crystal.posY + 1.0, crystal.posZ + 0.5, (Entity)target);
        return targetDmg >= this.minBreakDmg.getValue() || (targetDmg > this.minBreakDmg.getValue() && target.getHealth() > this.facePlace.getValue());
    }
    
    private boolean validTarget(final Entity entity) {
        return entity != null && entity instanceof EntityLivingBase && !Friends.isFriend(entity.getName()) && !entity.isDead && ((EntityLivingBase)entity).getHealth() > 0.0f && entity instanceof EntityPlayer && entity != AutoCrystal.mc.player;
    }
    
    private EntityLivingBase GetNearTarget(final Entity distanceTarget) {
        return (EntityLivingBase)AutoCrystal.mc.world.loadedEntityList.stream().filter(entity -> this.validTarget(entity)).map(entity -> entity).min(Comparator.comparing(entity -> distanceTarget.getDistance(entity))).orElse(null);
    }
    
    public boolean canPlaceCrystal(final BlockPos blockPos) {
        final BlockPos boost = blockPos.add(0, 1, 0);
        final BlockPos boost2 = blockPos.add(0, 2, 0);
        if (!this.endCrystalMode.getValue()) {
            return (AutoCrystal.mc.world.getBlockState(blockPos).getBlock() == Blocks.BEDROCK || AutoCrystal.mc.world.getBlockState(blockPos).getBlock() == Blocks.OBSIDIAN) && AutoCrystal.mc.world.getBlockState(boost).getBlock() == Blocks.AIR && AutoCrystal.mc.world.getBlockState(boost2).getBlock() == Blocks.AIR && AutoCrystal.mc.world.getEntitiesWithinAABB((Class)Entity.class, new AxisAlignedBB(boost)).isEmpty() && AutoCrystal.mc.world.getEntitiesWithinAABB((Class)Entity.class, new AxisAlignedBB(boost2)).isEmpty();
        }
        return (AutoCrystal.mc.world.getBlockState(blockPos).getBlock() == Blocks.BEDROCK || AutoCrystal.mc.world.getBlockState(blockPos).getBlock() == Blocks.OBSIDIAN) && AutoCrystal.mc.world.getEntitiesWithinAABB((Class)Entity.class, new AxisAlignedBB(boost)).isEmpty() && AutoCrystal.mc.world.getEntitiesWithinAABB((Class)Entity.class, new AxisAlignedBB(boost2)).isEmpty();
    }
    
    public static BlockPos getPlayerPos() {
        return new BlockPos(Math.floor(AutoCrystal.mc.player.posX), Math.floor(AutoCrystal.mc.player.posY), Math.floor(AutoCrystal.mc.player.posZ));
    }
    
    private List<BlockPos> findCrystalBlocks() {
        final NonNullList<BlockPos> positions = (NonNullList<BlockPos>)NonNullList.create();
        positions.addAll((Collection)this.getSphere(getPlayerPos(), (float)AutoCrystal.placeRange.getValue(), (int)AutoCrystal.placeRange.getValue(), false, true, 0).stream().filter((Predicate<? super Object>)this::canPlaceCrystal).collect((Collector<? super Object, ?, List<? super Object>>)Collectors.toList()));
        return (List<BlockPos>)positions;
    }
    
    public List<BlockPos> getSphere(final BlockPos loc, final float r, final int h, final boolean hollow, final boolean sphere, final int plus_y) {
        final List<BlockPos> circleblocks = new ArrayList<BlockPos>();
        final int cx = loc.getX();
        final int cy = loc.getY();
        final int cz = loc.getZ();
        for (int x = cx - (int)r; x <= cx + r; ++x) {
            for (int z = cz - (int)r; z <= cz + r; ++z) {
                for (int y = sphere ? (cy - (int)r) : cy; y < (sphere ? (cy + r) : ((float)(cy + h))); ++y) {
                    final double dist = (cx - x) * (cx - x) + (cz - z) * (cz - z) + (sphere ? ((cy - y) * (cy - y)) : 0);
                    if (dist < r * r && (!hollow || dist >= (r - 1.0f) * (r - 1.0f))) {
                        final BlockPos l = new BlockPos(x, y + plus_y, z);
                        circleblocks.add(l);
                    }
                }
            }
        }
        return circleblocks;
    }
    
    public static float calculateDamage(final double posX, final double posY, final double posZ, final Entity entity) {
        final float doubleExplosionSize = 12.0f;
        final double distancedsize = entity.getDistance(posX, posY, posZ) / doubleExplosionSize;
        final Vec3d vec3d = new Vec3d(posX, posY, posZ);
        final double blockDensity = entity.world.getBlockDensity(vec3d, entity.getEntityBoundingBox());
        final double v = (1.0 - distancedsize) * blockDensity;
        final float damage = (float)(int)((v * v + v) / 2.0 * 7.0 * doubleExplosionSize + 1.0);
        double finald = 1.0;
        if (entity instanceof EntityLivingBase) {
            finald = getBlastReduction((EntityLivingBase)entity, getDamageMultiplied(damage), new Explosion((World)AutoCrystal.mc.world, (Entity)null, posX, posY, posZ, 6.0f, false, true));
        }
        return (float)finald;
    }
    
    public static float getBlastReduction(final EntityLivingBase entity, float damage, final Explosion explosion) {
        if (entity instanceof EntityPlayer) {
            final EntityPlayer ep = (EntityPlayer)entity;
            final DamageSource ds = DamageSource.causeExplosionDamage(explosion);
            damage = CombatRules.getDamageAfterAbsorb(damage, (float)ep.getTotalArmorValue(), (float)ep.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue());
            final int k = EnchantmentHelper.getEnchantmentModifierDamage(ep.getArmorInventoryList(), ds);
            final float f = MathHelper.clamp((float)k, 0.0f, 20.0f);
            damage *= 1.0f - f / 25.0f;
            if (entity.isPotionActive(Potion.getPotionById(11))) {
                damage -= damage / 4.0f;
            }
            damage = Math.max(damage, 0.0f);
            return damage;
        }
        damage = CombatRules.getDamageAfterAbsorb(damage, (float)entity.getTotalArmorValue(), (float)entity.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue());
        return damage;
    }
    
    private static float getDamageMultiplied(final float damage) {
        final int diff = AutoCrystal.mc.world.getDifficulty().getId();
        return damage * ((diff == 0) ? 0.0f : ((diff == 2) ? 1.0f : ((diff == 1) ? 0.5f : 1.5f)));
    }
    
    private static void setYawAndPitch(final float yaw1, final float pitch1) {
        AutoCrystal.yaw = yaw1;
        AutoCrystal.pitch = pitch1;
        AutoCrystal.isSpoofingAngles = true;
    }
    
    private static void resetRotation() {
        if (AutoCrystal.isSpoofingAngles) {
            AutoCrystal.yaw = AutoCrystal.mc.player.rotationYaw;
            AutoCrystal.pitch = AutoCrystal.mc.player.rotationPitch;
            AutoCrystal.isSpoofingAngles = false;
        }
    }
    
    public static double[] calculateLookAt(final double px, final double py, final double pz, final EntityPlayer me) {
        double dirx = me.posX - px;
        double diry = me.posY - py;
        double dirz = me.posZ - pz;
        final double len = Math.sqrt(dirx * dirx + diry * diry + dirz * dirz);
        dirx /= len;
        diry /= len;
        dirz /= len;
        double pitch = Math.asin(diry);
        double yaw = Math.atan2(dirz, dirx);
        pitch = pitch * 180.0 / 3.141592653589793;
        yaw = yaw * 180.0 / 3.141592653589793;
        yaw += 90.0;
        return new double[] { yaw, pitch };
    }
    
    public void onEnable() {
        Vulcan.EVENT_BUS.subscribe(this);
        this.PlacedCrystals.clear();
        this.isActive = false;
        this.isPlacing = false;
        this.isBreaking = false;
    }
    
    public void onDisable() {
        Vulcan.EVENT_BUS.unsubscribe(this);
        this.render = null;
        this.renderEnt = null;
        resetRotation();
        this.PlacedCrystals.clear();
        this.isActive = false;
        this.isPlacing = false;
        this.isBreaking = false;
    }
    
    @Override
    public String getHudInfo() {
        String t = "";
        if (this.breakMode.getValue().equalsIgnoreCase("All")) {
            t = ChatFormatting.GRAY + "[" + ChatFormatting.WHITE + "All" + ChatFormatting.GRAY + "]";
        }
        if (this.breakMode.getValue().equalsIgnoreCase("Smart")) {
            t = ChatFormatting.GRAY + "[" + ChatFormatting.WHITE + AutoCrystal.targetName + ChatFormatting.GRAY + "]";
        }
        if (this.breakMode.getValue().equalsIgnoreCase("Only Own")) {
            t = ChatFormatting.GRAY + "[" + ChatFormatting.WHITE + "Own" + ChatFormatting.GRAY + "]";
        }
        return t;
    }
    
    static {
        AutoCrystal.togglePitch = false;
    }
}
