// 
// Decompiled by Procyon v0.5.36
// 

package me.ka1.vulcan.module.modules.misc;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import me.ka1.vulcan.module.Module;

public class RageQuit extends Module
{
    public static final Logger log;
    
    public RageQuit() {
        super("Rage Quit", "Currently coping??", Category.Misc);
    }
    
    @Override
    public int onUpdate() {
        if (this.isEnabled()) {
            this.disable();
        }
        RageQuit.log.info("Stay mad kid");
        System.exit(0);
        return 0;
    }
    
    static {
        log = LogManager.getLogger();
    }
}
