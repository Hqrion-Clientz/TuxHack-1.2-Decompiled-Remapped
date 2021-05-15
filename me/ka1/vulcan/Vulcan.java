// 
// Decompiled by Procyon v0.5.36
// 

package me.ka1.vulcan;

import org.apache.logging.log4j.LogManager;
import org.lwjgl.opengl.Display;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import me.ka1.vulcan.command.CommandManager;
import me.ka1.vulcan.util.config.Stopper;
import me.ka1.vulcan.util.TpsUtils;
import java.awt.Font;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import me.zero.alpine.EventBus;
import me.zero.alpine.EventManager;
import me.ka1.vulcan.util.enemy.Enemies;
import me.ka1.vulcan.util.font.CFontRenderer;
import me.ka1.vulcan.event.EventProcessor;
import me.ka1.vulcan.module.ModuleManager;
import me.ka1.vulcan.setting.SettingsManager;
import me.ka1.vulcan.util.friend.Friends;
import me.ka1.vulcan.ClickGui2.ClickGUI2;
import me.ka1.vulcan.util.config.LoadModules;
import me.ka1.vulcan.util.config.SaveModules;
import me.ka1.vulcan.util.config.LoadConfiguration;
import me.ka1.vulcan.util.config.SaveConfiguration;
import me.ka1.vulcan.macro.MacroManager;
import org.apache.logging.log4j.Logger;
import net.minecraftforge.fml.common.Mod;

@Mod(modid = "tuxhack", name = "TuxHack", version = "1")
public class Vulcan
{
    public static final String MOD_ID = "tuxhack";
    public static final String MOD_NAME = "TuxHack";
    public static final String VERSION = "1";
    public static final Logger log;
    public final boolean verified = false;
    public MacroManager macroManager;
    public SaveConfiguration saveConfiguration;
    public static LoadConfiguration loadConfiguration;
    public SaveModules saveModules;
    public static LoadModules loadModules;
    public ClickGUI2 clickGUI;
    public Friends friends;
    public SettingsManager settingsManager;
    public ModuleManager moduleManager;
    EventProcessor eventProcessor;
    public static CFontRenderer fontRenderer;
    public static Enemies enemies;
    private EventManager eventManager;
    public static final EventBus EVENT_BUS;
    @Mod.Instance
    private static Vulcan INSTANCE;
    
    public Vulcan() {
        Vulcan.INSTANCE = this;
    }
    
    public EventManager getEventManager() {
        if (this.eventManager == null) {
            this.eventManager = new EventManager();
        }
        return this.eventManager;
    }
    
    @Mod.EventHandler
    public void preinit(final FMLPreInitializationEvent event) {
    }
    
    @Mod.EventHandler
    public void init(final FMLInitializationEvent event) {
        (this.eventProcessor = new EventProcessor()).init();
        Vulcan.fontRenderer = new CFontRenderer(new Font("Arial", 0, 18), true, false);
        final TpsUtils tpsUtils = new TpsUtils();
        this.settingsManager = new SettingsManager();
        this.friends = new Friends();
        Vulcan.enemies = new Enemies();
        Vulcan.log.info("Friends and enemies initialized!");
        this.moduleManager = new ModuleManager();
        Vulcan.log.info("Settings initialized!");
        Vulcan.log.info("Modules initialized!");
        this.clickGUI = new ClickGUI2();
        Vulcan.log.info("ClickGUI initialized!");
        this.macroManager = new MacroManager();
        Vulcan.log.info("Macros initialized!");
        this.saveConfiguration = new SaveConfiguration();
        Runtime.getRuntime().addShutdownHook(new Stopper());
        Vulcan.log.info("Config Saved!");
        Vulcan.loadConfiguration = new LoadConfiguration();
        Vulcan.log.info("Config Loaded!");
        this.saveModules = new SaveModules();
        Runtime.getRuntime().addShutdownHook(new Stopper());
        Vulcan.log.info("Modules Saved!");
        Vulcan.loadModules = new LoadModules();
        Vulcan.log.info("Modules Loaded!");
        CommandManager.initCommands();
        Vulcan.log.info("Commands initialized!");
        Vulcan.log.info("Initialization complete!\n");
    }
    
    @Mod.EventHandler
    public void postinit(final FMLPostInitializationEvent event) {
        Display.setTitle("TuxHack 1");
        Vulcan.log.info("PostInitialization of tuxhack complete!\n");
    }
    
    public static Vulcan getInstance() {
        return Vulcan.INSTANCE;
    }
    
    static {
        log = LogManager.getLogger("TuxHack");
        EVENT_BUS = new EventManager();
    }
}
