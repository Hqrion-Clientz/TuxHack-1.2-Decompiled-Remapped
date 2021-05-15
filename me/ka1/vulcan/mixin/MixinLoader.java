// 
// Decompiled by Procyon v0.5.36
// 

package me.ka1.vulcan.mixin;

import java.util.Map;
import javax.annotation.Nullable;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.launch.MixinBootstrap;
import me.ka1.vulcan.Vulcan;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

public class MixinLoader implements IFMLLoadingPlugin
{
    private static boolean isObfuscatedEnvironment;
    
    public MixinLoader() {
        Vulcan.log.info("Mixins initialized");
        MixinBootstrap.init();
        Mixins.addConfiguration("mixins.vulcan.json");
        MixinEnvironment.getDefaultEnvironment().setObfuscationContext("searge");
        Vulcan.log.info(MixinEnvironment.getDefaultEnvironment().getObfuscationContext());
    }
    
    public String[] getASMTransformerClass() {
        return new String[0];
    }
    
    public String getModContainerClass() {
        return null;
    }
    
    @Nullable
    public String getSetupClass() {
        return null;
    }
    
    public void injectData(final Map<String, Object> data) {
        MixinLoader.isObfuscatedEnvironment = data.get("runtimeDeobfuscationEnabled");
    }
    
    public String getAccessTransformerClass() {
        return null;
    }
    
    static {
        MixinLoader.isObfuscatedEnvironment = false;
    }
}
