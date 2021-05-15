// 
// Decompiled by Procyon v0.5.36
// 

package me.ka1.vulcan.event;

public class EventCancellable extends EventStageable
{
    private boolean canceled;
    
    public EventCancellable() {
    }
    
    public EventCancellable(final EventStage stage) {
        super(stage);
    }
    
    public boolean isCanceled() {
        return this.canceled;
    }
    
    public void setCanceled(final boolean canceled) {
        this.canceled = canceled;
    }
}
