// 
// Decompiled by Procyon v0.5.36
// 

package me.ka1.vulcan.event.events;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import me.ka1.vulcan.event.MinecraftEvent;

public class EventPlayerDamageBlock extends MinecraftEvent
{
    private BlockPos BlockPos;
    private EnumFacing Direction;
    
    public EventPlayerDamageBlock(final BlockPos posBlock, final EnumFacing directionFacing) {
        this.BlockPos = posBlock;
        this.setDirection(directionFacing);
    }
    
    public BlockPos getPos() {
        return this.BlockPos;
    }
    
    public EnumFacing getDirection() {
        return this.Direction;
    }
    
    public void setDirection(final EnumFacing direction) {
        this.Direction = direction;
    }
}
