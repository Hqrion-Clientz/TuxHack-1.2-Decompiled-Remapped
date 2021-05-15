// 
// Decompiled by Procyon v0.5.36
// 

package me.ka1.vulcan.event.events;

import me.ka1.vulcan.event.Event;

public class PlayerLeaveEvent extends Event
{
    private final String name;
    
    public PlayerLeaveEvent(final String n) {
        this.name = n;
    }
    
    public String getName() {
        return this.name;
    }
}
