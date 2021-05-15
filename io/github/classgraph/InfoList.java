// 
// Decompiled by Procyon v0.5.36
// 

package io.github.classgraph;

import java.util.ListIterator;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Collection;

public class InfoList<T extends HasName> extends PotentiallyUnmodifiableList<T>
{
    static final long serialVersionUID = 1L;
    
    InfoList() {
    }
    
    InfoList(final int sizeHint) {
        super(sizeHint);
    }
    
    InfoList(final Collection<T> infoCollection) {
        super(infoCollection);
    }
    
    @Override
    public boolean equals(final Object o) {
        return super.equals(o);
    }
    
    @Override
    public int hashCode() {
        return super.hashCode();
    }
    
    public List<String> getNames() {
        if (this.isEmpty()) {
            return Collections.emptyList();
        }
        final List<String> names = new ArrayList<String>(this.size());
        for (final T i : this) {
            if (i != null) {
                names.add(i.getName());
            }
        }
        return names;
    }
    
    public List<String> getAsStrings() {
        if (this.isEmpty()) {
            return Collections.emptyList();
        }
        final List<String> toStringVals = new ArrayList<String>(this.size());
        for (final T i : this) {
            toStringVals.add((i == null) ? "null" : i.toString());
        }
        return toStringVals;
    }
}
