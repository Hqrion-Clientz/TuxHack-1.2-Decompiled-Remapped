//Deobfuscated with https://github.com/PetoPetko/Minecraft-Deobfuscator3000 using mappings "1.12 stable mappings"!

// 
// Decompiled by Procyon v0.5.36
// 

package me.ka1.vulcan.module.modules.client;

import java.util.HashMap;
import java.util.Arrays;
import java.util.UUID;
import java.util.Iterator;
import net.minecraft.client.entity.AbstractClientPlayer;
import java.util.Map;
import net.minecraft.util.ResourceLocation;
import me.ka1.vulcan.module.Module;

public class Capes extends Module
{
    public static final ResourceLocation TUX_CAPE;
    public static final ResourceLocation KA1_CAPE;
    public static Map<String, String[]> UUIDs;
    private static Capes instance;
    
    public Capes() {
        super("Capes", "Renders the client's capes", Category.Client);
        Capes.UUIDs.put("Tux", new String[] { "2a158b00-dbd9-4197-8c69-2c08cf545462" });
        Capes.UUIDs.put("_KA1", new String[] { "fd277a5b-6558-4050-9ca5-e27f67db8277" });
        Capes.instance = this;
    }
    
    public static Capes getInstance() {
        if (Capes.instance == null) {
            Capes.instance = new Capes();
        }
        return Capes.instance;
    }
    
    public static ResourceLocation getCapeResource(final AbstractClientPlayer player) {
        for (final String name : Capes.UUIDs.keySet()) {
            for (final String uuid : Capes.UUIDs.get(name)) {
                if (name.equalsIgnoreCase("tuxiscool") && player.getUniqueID().toString().equals(uuid)) {
                    return Capes.TUX_CAPE;
                }
                if (name.equalsIgnoreCase("Ethenos") && player.getUniqueID().toString().equals(uuid)) {
                    return Capes.TUX_CAPE;
                }
                if (name.equalsIgnoreCase("_ka1") && player.getUniqueID().toString().equals(uuid)) {
                    return Capes.KA1_CAPE;
                }
            }
        }
        return null;
    }
    
    public static boolean hasCape(final UUID uuid) {
        final Iterator<String> iterator = Capes.UUIDs.keySet().iterator();
        if (iterator.hasNext()) {
            final String name = iterator.next();
            return Arrays.asList((String[])Capes.UUIDs.get(name)).contains(uuid.toString());
        }
        return false;
    }
    
    static {
        TUX_CAPE = new ResourceLocation("textures/tux.png");
        KA1_CAPE = new ResourceLocation("textures/ka1.png");
        Capes.UUIDs = new HashMap<String, String[]>();
    }
}
