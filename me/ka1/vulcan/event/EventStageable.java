// 
// Decompiled by Procyon v0.5.36
// 

package me.ka1.vulcan.event;

public class EventStageable
{
    private EventStage stage;
    
    public EventStageable() {
    }
    
    public EventStageable(final EventStage stage) {
        this.stage = stage;
    }
    
    public EventStage getStage() {
        return this.stage;
    }
    
    public enum EventStage
    {
        PRE, 
        POST;
    }
}
