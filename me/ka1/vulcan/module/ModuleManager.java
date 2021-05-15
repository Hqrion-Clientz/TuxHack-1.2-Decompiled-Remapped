//Deobfuscated with https://github.com/PetoPetko/Minecraft-Deobfuscator3000 using mappings "1.12 stable mappings"!

// 
// Decompiled by Procyon v0.5.36
// 

package me.ka1.vulcan.module;

import java.util.stream.Collector;
import java.util.stream.Collectors;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.renderer.Tessellator;
import me.ka1.vulcan.event.events.RenderEvent;
import me.ka1.vulcan.util.RenderUtil;
import net.minecraft.entity.Entity;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import me.ka1.vulcan.module.modules.chat.AutoTPA;
import me.ka1.vulcan.module.modules.chat.ChatEncrypt;
import me.ka1.vulcan.module.modules.chat.ChatSuffix;
import me.ka1.vulcan.module.modules.render.HoleESP;
import me.ka1.vulcan.module.modules.render.FOVSlider;
import me.ka1.vulcan.module.modules.render.Zoom;
import me.ka1.vulcan.module.modules.render.Fullbright;
import me.ka1.vulcan.module.modules.render.Nametags;
import me.ka1.vulcan.module.modules.render.Esp;
import me.ka1.vulcan.module.modules.render.LowHands;
import me.ka1.vulcan.module.modules.Hud.Logo;
import me.ka1.vulcan.module.modules.render.BlockHighlight;
import me.ka1.vulcan.module.modules.Hud.TextRadar;
import me.ka1.vulcan.module.modules.render.Tracers;
import me.ka1.vulcan.module.modules.Hud.HudEditor;
import me.ka1.vulcan.module.modules.Hud.ping;
import me.ka1.vulcan.module.modules.Hud.FPS;
import me.ka1.vulcan.module.modules.Hud.TPS;
import me.ka1.vulcan.module.modules.Hud.CombatInfo;
import me.ka1.vulcan.module.modules.Hud.Watermark;
import me.ka1.vulcan.module.modules.Hud.Coordinates;
import me.ka1.vulcan.module.modules.Hud.Welcomer;
import me.ka1.vulcan.module.modules.Hud.Time;
import me.ka1.vulcan.module.modules.Hud.Inventory;
import me.ka1.vulcan.module.modules.client.CommandColor;
import me.ka1.vulcan.module.modules.client.Rainbow;
import me.ka1.vulcan.module.modules.client.CustomFont;
import me.ka1.vulcan.module.modules.client.ClickGuiModule;
import me.ka1.vulcan.module.modules.client.DiscordRPC;
import me.ka1.vulcan.module.modules.render.FpsLimiter;
import me.ka1.vulcan.module.modules.client.Capes;
import me.ka1.vulcan.module.modules.client.LoadConfig;
import me.ka1.vulcan.module.modules.client.TeamTux;
import me.ka1.vulcan.module.modules.misc.ArmorNotification;
import me.ka1.vulcan.module.modules.misc.FakePlayer;
import me.ka1.vulcan.module.modules.misc.AntiAim;
import me.ka1.vulcan.module.modules.misc.BackPack;
import me.ka1.vulcan.module.modules.misc.GhastNotifier;
import me.ka1.vulcan.module.modules.misc.RageQuit;
import me.ka1.vulcan.module.modules.misc.PotionDetect;
import me.ka1.vulcan.module.modules.player.Xcarry;
import me.ka1.vulcan.module.modules.player.MountBypass;
import me.ka1.vulcan.module.modules.player.Suicide;
import me.ka1.vulcan.module.modules.player.KeyPortal;
import me.ka1.vulcan.module.modules.player.FastEXP;
import me.ka1.vulcan.module.modules.player.FastCrystal;
import me.ka1.vulcan.module.modules.movement.Sprint;
import me.ka1.vulcan.module.modules.movement.Step;
import me.ka1.vulcan.module.modules.movement.ReverseStep;
import me.ka1.vulcan.module.modules.movement.MoonWalk;
import me.ka1.vulcan.module.modules.combat.spiderman;
import me.ka1.vulcan.module.modules.combat.KillAura;
import me.ka1.vulcan.module.modules.combat.Surround;
import me.ka1.vulcan.module.modules.combat.EasyPearl;
import me.ka1.vulcan.module.modules.combat.BedAura;
import me.ka1.vulcan.module.modules.combat.BurrowBypass;
import me.ka1.vulcan.module.modules.combat.Burrow;
import me.ka1.vulcan.module.modules.combat.AutoTotem;
import me.ka1.vulcan.module.modules.combat.AutoMend;
import me.ka1.vulcan.module.modules.combat.AntiCB;
import me.ka1.vulcan.module.modules.combat.SelfTrap;
import me.ka1.vulcan.module.modules.combat.AutoCrystal;
import java.util.ArrayList;

public class ModuleManager
{
    public static ArrayList<Module> modules;
    
    public ModuleManager() {
        ModuleManager.modules = new ArrayList<Module>();
        addMod(new AutoCrystal());
        addMod(new SelfTrap());
        addMod(new AntiCB());
        addMod(new AutoMend());
        addMod(new AutoTotem());
        addMod(new Burrow());
        addMod(new BurrowBypass());
        addMod(new BedAura());
        addMod(new EasyPearl());
        addMod(new Surround());
        addMod(new KillAura());
        addMod(new spiderman());
        addMod(new MoonWalk());
        addMod(new ReverseStep());
        addMod(new Step());
        addMod(new Sprint());
        addMod(new FastCrystal());
        addMod(new FastEXP());
        addMod(new KeyPortal());
        addMod(new Suicide());
        addMod(new MountBypass());
        addMod(new Xcarry());
        addMod(new PotionDetect());
        addMod(new RageQuit());
        addMod(new GhastNotifier());
        addMod(new BackPack());
        addMod(new AntiAim());
        addMod(new FakePlayer());
        addMod(new ArmorNotification());
        addMod(new TeamTux());
        addMod(new LoadConfig());
        addMod(new Capes());
        addMod(new FpsLimiter());
        addMod(new DiscordRPC());
        addMod(new ClickGuiModule());
        addMod(new CustomFont());
        addMod(new Rainbow());
        addMod(new CommandColor());
        addMod(new Inventory());
        addMod(new me.ka1.vulcan.module.modules.Hud.ArrayList());
        addMod(new Time());
        addMod(new Welcomer());
        addMod(new Coordinates());
        addMod(new Watermark());
        addMod(new CombatInfo());
        addMod(new TPS());
        addMod(new FPS());
        addMod(new ping());
        addMod(new HudEditor());
        addMod(new Tracers());
        addMod(new TextRadar());
        addMod(new BlockHighlight());
        addMod(new Logo());
        addMod(new LowHands());
        addMod(new Esp());
        addMod(new Nametags());
        addMod(new Fullbright());
        addMod(new Zoom());
        addMod(new FOVSlider());
        addMod(new HoleESP());
        addMod(new ChatSuffix());
        addMod(new ChatEncrypt());
        addMod(new AutoTPA());
    }
    
    public static void addMod(final Module m) {
        ModuleManager.modules.add(m);
    }
    
    public static void onUpdate() {
        ModuleManager.modules.stream().filter(Module::isEnabled).forEach(Module::onUpdate);
    }
    
    public static void onRender() {
        ModuleManager.modules.stream().filter(Module::isEnabled).forEach(Module::onRender);
    }
    
    public static void onWorldRender(final RenderWorldLastEvent event) {
        Minecraft.getMinecraft().profiler.startSection("Vulcan");
        Minecraft.getMinecraft().profiler.startSection("setup");
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.shadeModel(7425);
        GlStateManager.disableDepth();
        GlStateManager.glLineWidth(1.0f);
        final Vec3d renderPos = getInterpolatedPos((Entity)Minecraft.getMinecraft().player, event.getPartialTicks());
        final RenderEvent e = new RenderEvent(RenderUtil.INSTANCE, renderPos, event.getPartialTicks());
        e.resetTranslation();
        Minecraft.getMinecraft().profiler.endSection();
        final RenderEvent event2;
        ModuleManager.modules.stream().filter(module -> module.isEnabled()).forEach(module -> {
            Minecraft.getMinecraft().profiler.startSection(module.getName());
            module.onWorldRender(event2);
            Minecraft.getMinecraft().profiler.endSection();
            return;
        });
        Minecraft.getMinecraft().profiler.startSection("release");
        GlStateManager.glLineWidth(1.0f);
        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
        GlStateManager.enableDepth();
        GlStateManager.enableCull();
        RenderUtil.releaseGL();
        Minecraft.getMinecraft().profiler.endSection();
        Minecraft.getMinecraft().profiler.endSection();
    }
    
    public static ArrayList<Module> getModules() {
        return ModuleManager.modules;
    }
    
    public static ArrayList<Module> getModulesInCategory(final Module.Category c) {
        final ArrayList<Module> list = getModules().stream().filter(m -> m.getCategory().equals(c)).collect((Collector<? super Object, ?, ArrayList<Module>>)Collectors.toList());
        return list;
    }
    
    public static void onBind(final int key) {
        if (key == 0 || key == 0) {
            return;
        }
        ModuleManager.modules.forEach(module -> {
            if (module.getBind() == key) {
                module.toggle();
            }
        });
    }
    
    public static Module getModuleByName(final String name) {
        final Module m = getModules().stream().filter(mm -> mm.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
        return m;
    }
    
    public static boolean isModuleEnabled(final String name) {
        final Module m = getModules().stream().filter(mm -> mm.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
        return m.isEnabled();
    }
    
    public static boolean isModuleEnabled(final Module m) {
        return m.isEnabled();
    }
    
    public static Vec3d getInterpolatedPos(final Entity entity, final float ticks) {
        return new Vec3d(entity.lastTickPosX, entity.lastTickPosY, entity.lastTickPosZ).add(getInterpolatedAmount(entity, ticks));
    }
    
    public static Vec3d getInterpolatedAmount(final Entity entity, final double ticks) {
        return getInterpolatedAmount(entity, ticks, ticks, ticks);
    }
    
    public static Vec3d getInterpolatedAmount(final Entity entity, final double x, final double y, final double z) {
        return new Vec3d((entity.posX - entity.lastTickPosX) * x, (entity.posY - entity.lastTickPosY) * y, (entity.posZ - entity.lastTickPosZ) * z);
    }
}
