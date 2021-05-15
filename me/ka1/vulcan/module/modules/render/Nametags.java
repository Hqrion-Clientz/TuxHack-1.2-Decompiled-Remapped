//Deobfuscated with https://github.com/PetoPetko/Minecraft-Deobfuscator3000 using mappings "1.12 stable mappings"!

// 
// Decompiled by Procyon v0.5.36
// 

package me.ka1.vulcan.module.modules.render;

import me.ka1.vulcan.util.friend.Friends;
import me.ka1.vulcan.util.colors.ColourUtilities;
import java.awt.Color;
import net.minecraft.init.Items;
import net.minecraft.enchantment.EnchantmentHelper;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.util.math.Vec3d;
import java.util.Iterator;
import java.util.List;
import java.util.Collections;
import net.minecraft.item.ItemStack;
import me.ka1.vulcan.util.font.FontUtils;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import me.ka1.vulcan.util.maths.MathUtil;
import me.ka1.vulcan.util.RenderUtil;
import me.ka1.vulcan.util.entities.EntityUtil;
import net.minecraft.entity.player.EntityPlayer;
import java.util.ArrayList;
import me.ka1.vulcan.event.events.RenderEvent;
import java.util.function.Predicate;
import me.zero.alpine.fork.listener.EventHandler;
import me.ka1.vulcan.event.events.EventRenderEntityName;
import me.zero.alpine.fork.listener.Listener;
import me.ka1.vulcan.setting.Setting;
import me.ka1.vulcan.module.Module;

public class Nametags extends Module
{
    Setting.Double Scaling;
    Setting.Boolean customFont;
    @EventHandler
    private Listener<EventRenderEntityName> OnRenderEntityName;
    
    @Override
    public void setup() {
        this.Scaling = this.registerDouble("Scaling", "Scaling", 3.0, 1.0, 10.0);
        this.customFont = this.registerBoolean("CustomFont", "Font", true);
    }
    
    public Nametags() {
        super("Nametags", Category.Render);
        this.OnRenderEntityName = new Listener<EventRenderEntityName>(event -> event.cancel(), (Predicate<EventRenderEntityName>[])new Predicate[0]);
    }
    
    @Override
    public void onWorldRender(final RenderEvent event) {
        if (Nametags.mc.world == null || Nametags.mc.renderEngine == null || Nametags.mc.getRenderManager() == null || Nametags.mc.getRenderManager().options == null) {
            return;
        }
        final List<EntityPlayer> players = new ArrayList<EntityPlayer>();
        final List<EntityPlayer> list;
        Nametags.mc.world.playerEntities.stream().filter(entity -> entity instanceof EntityPlayer && EntityUtil.isLiving(entity) && entity != Nametags.mc.getRenderViewEntity()).forEach(e -> {
            RenderUtil.camera.setPosition(Nametags.mc.getRenderViewEntity().posX, Nametags.mc.getRenderViewEntity().posY, Nametags.mc.getRenderViewEntity().posZ);
            if (RenderUtil.camera.isBoundingBoxInFrustum(e.getEntityBoundingBox())) {
                list.add(e);
            }
            return;
        });
        players.sort((p1, p2) -> Double.compare(p2.getDistance(Nametags.mc.getRenderViewEntity()), p1.getDistance(Nametags.mc.getRenderViewEntity())));
        for (final EntityPlayer player : players) {
            final Entity entity2 = Nametags.mc.getRenderViewEntity();
            Vec3d pos = MathUtil.interpolateEntityClose((Entity)player, event.getPartialTicks());
            final double n = pos.x;
            double distance = pos.y + 0.65;
            final double n2 = pos.z;
            final double n3 = distance + (player.isSneaking() ? 0.0 : 0.07999999821186066);
            pos = MathUtil.interpolateEntityClose(entity2, event.getPartialTicks());
            final double posX = entity2.posX;
            final double posY = entity2.posY;
            final double posZ = entity2.posZ;
            entity2.posX = pos.x;
            entity2.posY = pos.y;
            entity2.posZ = pos.z;
            distance = entity2.getDistance(n, distance, n2);
            double scale = 0.04;
            if (distance > 0.0) {
                scale = 0.02 + (float)this.Scaling.getValue() / 1000.0f * distance;
            }
            GlStateManager.pushMatrix();
            RenderHelper.enableStandardItemLighting();
            GlStateManager.enablePolygonOffset();
            GlStateManager.doPolygonOffset(1.0f, -1500000.0f);
            GlStateManager.disableLighting();
            GlStateManager.translate((float)n, (float)n3 + 1.4f, (float)n2);
            final float n4 = -Nametags.mc.getRenderManager().playerViewY;
            final float n5 = 1.0f;
            final float n6 = 0.0f;
            GlStateManager.rotate(n4, 0.0f, 1.0f, 0.0f);
            GlStateManager.rotate(Nametags.mc.getRenderManager().playerViewX, (Nametags.mc.gameSettings.thirdPersonView == 2) ? -1.0f : 1.0f, 0.0f, 0.0f);
            GlStateManager.scale(-scale, -scale, scale);
            GlStateManager.disableDepth();
            GlStateManager.enableBlend();
            final String nameTag = this.generateNameTag(player);
            final float width = (float)(FontUtils.getStringWidth(this.customFont.getValue(), nameTag) / 2);
            final float height = (float)FontUtils.getFontHeight(this.customFont.getValue());
            GlStateManager.enableBlend();
            RenderUtil.drawRect(-width - 1.0f, -(height + 1.0f), width + 2.0f, 2.0f, 1594493450);
            GlStateManager.disableBlend();
            FontUtils.drawKeyStringWithShadow(this.customFont.getValue(), nameTag, (int)(-width) + 1, (int)(-height) + 3, -1);
            GlStateManager.pushMatrix();
            final Iterator<ItemStack> items = player.getArmorInventoryList().iterator();
            final ArrayList<ItemStack> stacks = new ArrayList<ItemStack>();
            stacks.add(player.getHeldItemOffhand());
            while (items.hasNext()) {
                final ItemStack stack = items.next();
                if (!stack.isEmpty()) {
                    stacks.add(stack);
                }
            }
            stacks.add(player.getHeldItemMainhand());
            Collections.reverse(stacks);
            int x = (int)(-width);
            final int y = -32;
            final int z = 0;
            for (final ItemStack stack2 : stacks) {
                this.RenderItemStack(stack2, x, y, z);
                this.RenderItemEnchantments(stack2, x, -62);
                x += 16;
            }
            GlStateManager.popMatrix();
            GlStateManager.enableDepth();
            GlStateManager.disableBlend();
            GlStateManager.disablePolygonOffset();
            GlStateManager.doPolygonOffset(1.0f, 1500000.0f);
            GlStateManager.popMatrix();
            entity2.posX = posX;
            entity2.posY = posY;
            entity2.posZ = posZ;
        }
    }
    
    private String GetEnchantName(final Enchantment enchantment, final int n) {
        if (enchantment.getTranslatedName(n).contains("Vanish")) {
            return ChatFormatting.RED + "Van";
        }
        if (enchantment.getTranslatedName(n).contains("Bind")) {
            return ChatFormatting.RED + "Bind";
        }
        String substring = enchantment.getTranslatedName(n);
        final int n2 = (n > 1) ? 2 : 3;
        if (substring.length() > n2) {
            substring = substring.substring(0, n2);
        }
        final StringBuilder sb = new StringBuilder();
        final String s = substring;
        final int n3 = 0;
        String s2 = sb.insert(0, s.substring(0, 1).toUpperCase()).append(substring.substring(1)).toString();
        if (n > 1) {
            s2 = new StringBuilder().insert(0, s2).append(n).toString();
        }
        return s2;
    }
    
    private void RenderItemEnchantments(final ItemStack itemStack, final int n, int n2) {
        GlStateManager.scale(0.5f, 0.5f, 0.5f);
        final int n3 = -1;
        Iterator<Enchantment> iterator3;
        for (Iterator<Enchantment> iterator2 = iterator3 = EnchantmentHelper.getEnchantments(itemStack).keySet().iterator(); iterator3.hasNext(); iterator3 = iterator2) {
            final Enchantment enchantment;
            if ((enchantment = iterator2.next()) != null) {
                FontUtils.drawKeyStringWithShadow(this.customFont.getValue(), this.GetEnchantName(enchantment, EnchantmentHelper.getEnchantmentLevel(enchantment, itemStack)), n * 2, n2, -1);
                n2 += 8;
            }
        }
        if (itemStack.getItem().equals(Items.GOLDEN_APPLE) && itemStack.hasEffect()) {
            FontUtils.drawKeyStringWithShadow(this.customFont.getValue(), ChatFormatting.DARK_RED + "God", n * 2, n2, -1);
        }
        GlStateManager.scale(2.0f, 2.0f, 2.0f);
    }
    
    private void RenderItemDamage(final ItemStack itemStack, final int n, final int n2) {
        final float n3 = (itemStack.getMaxDamage() - itemStack.getItemDamage()) / (float)itemStack.getMaxDamage() * 100.0f;
        int color = 2096896;
        if (n3 > 30.0f && n3 < 70.0f) {
            color = 16776960;
        }
        else if (n3 <= 30.0f) {
            color = 16711680;
        }
        GlStateManager.scale(0.5f, 0.5f, 0.5f);
        GlStateManager.disableDepth();
        FontUtils.drawKeyStringWithShadow(this.customFont.getValue(), new StringBuilder().insert(0, String.valueOf((int)n3)).append('%').toString(), n * 2, n2, color);
        GlStateManager.enableDepth();
        GlStateManager.scale(2.0f, 2.0f, 2.0f);
    }
    
    private void RenderItemStack(final ItemStack itemStack, final int n, final int n2, final int n3) {
        GlStateManager.pushMatrix();
        GlStateManager.depthMask(true);
        GlStateManager.clear(256);
        RenderHelper.enableStandardItemLighting();
        Nametags.mc.getRenderItem().zLevel = -150.0f;
        GlStateManager.disableAlpha();
        GlStateManager.enableDepth();
        GlStateManager.disableCull();
        final int n4 = (n3 > 4) ? ((n3 - 4) * 8 / 2) : 0;
        Nametags.mc.getRenderItem().renderItemAndEffectIntoGUI(itemStack, n, n2 + n4);
        Nametags.mc.getRenderItem().renderItemOverlays(Nametags.mc.fontRenderer, itemStack, n, n2 + n4);
        Nametags.mc.getRenderItem().zLevel = 0.0f;
        RenderHelper.disableStandardItemLighting();
        GlStateManager.enableCull();
        GlStateManager.enableAlpha();
        final float n5 = 0.5f;
        final float n6 = 0.5f;
        GlStateManager.scale(0.5f, 0.5f, 0.5f);
        GlStateManager.disableDepth();
        if (itemStack.getMaxDamage() > 1) {
            this.RenderItemDamage(itemStack, n * 2, n2 - 100);
        }
        GlStateManager.enableDepth();
        final float n7 = 2.0f;
        final int n8 = 2;
        GlStateManager.scale(2.0f, 2.0f, 2.0f);
        GlStateManager.popMatrix();
    }
    
    private int getColorByHealth(final float maxHealth, final float health) {
        final Color green = new Color(30, 199, 49);
        final Color yellow = new Color(255, 250, 57);
        final Color red = new Color(255, 35, 40);
        final float middleHealth = maxHealth / 2.0f;
        if (health <= middleHealth) {
            return ColourUtilities.blend(yellow, red, health / middleHealth).getRGB();
        }
        if (health <= middleHealth * 2.0f) {
            return ColourUtilities.blend(green, yellow, (health - middleHealth) / middleHealth).getRGB();
        }
        return green.getRGB();
    }
    
    private String generateNameTag(final EntityPlayer player) {
        String namestring = player.getName();
        if (Friends.isFriend(player.getName())) {
            namestring = ChatFormatting.AQUA + namestring + ChatFormatting.RESET;
        }
        else if (player.isSneaking()) {
            namestring = ChatFormatting.DARK_PURPLE + namestring + ChatFormatting.RESET;
        }
        else {
            namestring = ChatFormatting.GRAY + namestring + ChatFormatting.RESET;
        }
        int responseTime = -1;
        try {
            responseTime = (int)MathUtil.clamp((float)Nametags.mc.getConnection().getPlayerInfo(player.getUniqueID()).getResponseTime(), 0.0f, 10000.0f);
        }
        catch (NullPointerException ex) {}
        String string = new String();
        if (responseTime > 200) {
            string += ChatFormatting.GRAY;
        }
        else if (responseTime <= 200 && responseTime >= 100) {
            string += ChatFormatting.GRAY;
        }
        else if (responseTime < 100) {
            string += ChatFormatting.GRAY;
        }
        string = string + responseTime + "ms " + namestring + ChatFormatting.RESET + "  ";
        return string + Math.floor(player.getHealth() + player.getAbsorptionAmount());
    }
}
