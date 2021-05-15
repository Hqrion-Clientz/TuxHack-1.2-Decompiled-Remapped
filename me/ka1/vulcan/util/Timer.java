// 
// Decompiled by Procyon v0.5.36
// 

package me.ka1.vulcan.util;

public class Timer
{
    private long time;
    private long current;
    
    public Timer() {
        this.current = System.currentTimeMillis();
    }
    
    public void reset() {
        this.current = System.currentTimeMillis();
    }
    
    public boolean hasReached(final long var1) {
        return System.currentTimeMillis() - this.current >= var1;
    }
    
    public boolean passed(final double ms) {
        return System.currentTimeMillis() - this.time >= ms;
    }
    
    public boolean sleep(final long var1) {
        if (this.time() >= var1) {
            this.reset();
            return true;
        }
        return false;
    }
    
    public long getTimePassed() {
        return System.currentTimeMillis() - this.current;
    }
    
    public void setTime(final long time) {
        this.time = time;
    }
    
    public long time() {
        return System.currentTimeMillis() - this.current;
    }
    
    public boolean hasReached(final long var1, final boolean var3) {
        if (var3) {
            this.reset();
        }
        return System.currentTimeMillis() - this.current >= var1;
    }
}
