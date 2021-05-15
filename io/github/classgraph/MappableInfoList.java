// 
// Decompiled by Procyon v0.5.36
// 

package io.github.classgraph;

import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;
import java.util.Collection;

public class MappableInfoList<T extends HasName> extends InfoList<T>
{
    private static final long serialVersionUID = 1L;
    
    MappableInfoList() {
    }
    
    MappableInfoList(final int sizeHint) {
        super(sizeHint);
    }
    
    MappableInfoList(final Collection<T> infoCollection) {
        super(infoCollection);
    }
    
    public Map<String, T> asMap() {
        final Map<String, T> nameToInfoObject = new HashMap<String, T>();
        for (final T i : this) {
            if (i != null) {
                nameToInfoObject.put(i.getName(), i);
            }
        }
        return nameToInfoObject;
    }
    
    public boolean containsName(final String name) {
        for (final T i : this) {
            if (i != null && i.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }
    
    public T get(final String name) {
        for (final T i : this) {
            if (i != null && i.getName().equals(name)) {
                return i;
            }
        }
        return null;
    }
}
