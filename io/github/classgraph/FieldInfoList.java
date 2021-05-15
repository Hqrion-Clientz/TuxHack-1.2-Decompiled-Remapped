// 
// Decompiled by Procyon v0.5.36
// 

package io.github.classgraph;

import java.util.Iterator;
import java.util.Set;
import java.util.Map;
import java.util.Collection;

public class FieldInfoList extends MappableInfoList<FieldInfo>
{
    private static final long serialVersionUID = 1L;
    static final FieldInfoList EMPTY_LIST;
    
    public static FieldInfoList emptyList() {
        return FieldInfoList.EMPTY_LIST;
    }
    
    public FieldInfoList() {
    }
    
    public FieldInfoList(final int sizeHint) {
        super(sizeHint);
    }
    
    public FieldInfoList(final Collection<FieldInfo> fieldInfoCollection) {
        super(fieldInfoCollection);
    }
    
    protected void findReferencedClassInfo(final Map<String, ClassInfo> classNameToClassInfo, final Set<ClassInfo> refdClassInfo) {
        for (final FieldInfo fi : this) {
            fi.findReferencedClassInfo(classNameToClassInfo, refdClassInfo);
        }
    }
    
    public FieldInfoList filter(final FieldInfoFilter filter) {
        final FieldInfoList fieldInfoFiltered = new FieldInfoList();
        for (final FieldInfo resource : this) {
            if (filter.accept(resource)) {
                fieldInfoFiltered.add(resource);
            }
        }
        return fieldInfoFiltered;
    }
    
    static {
        (EMPTY_LIST = new FieldInfoList()).makeUnmodifiable();
    }
    
    @FunctionalInterface
    public interface FieldInfoFilter
    {
        boolean accept(final FieldInfo p0);
    }
}
