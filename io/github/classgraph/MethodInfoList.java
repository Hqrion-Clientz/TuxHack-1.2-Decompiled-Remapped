// 
// Decompiled by Procyon v0.5.36
// 

package io.github.classgraph;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Map;
import java.util.Collection;

public class MethodInfoList extends InfoList<MethodInfo>
{
    private static final long serialVersionUID = 1L;
    static final MethodInfoList EMPTY_LIST;
    
    public static MethodInfoList emptyList() {
        return MethodInfoList.EMPTY_LIST;
    }
    
    public MethodInfoList() {
    }
    
    public MethodInfoList(final int sizeHint) {
        super(sizeHint);
    }
    
    public MethodInfoList(final Collection<MethodInfo> methodInfoCollection) {
        super(methodInfoCollection);
    }
    
    protected void findReferencedClassInfo(final Map<String, ClassInfo> classNameToClassInfo, final Set<ClassInfo> refdClassInfo) {
        for (final MethodInfo mi : this) {
            mi.findReferencedClassInfo(classNameToClassInfo, refdClassInfo);
        }
    }
    
    public Map<String, MethodInfoList> asMap() {
        final Map<String, MethodInfoList> methodNameToMethodInfoList = new HashMap<String, MethodInfoList>();
        for (final MethodInfo methodInfo : this) {
            final String name = methodInfo.getName();
            MethodInfoList methodInfoList = methodNameToMethodInfoList.get(name);
            if (methodInfoList == null) {
                methodInfoList = new MethodInfoList(1);
                methodNameToMethodInfoList.put(name, methodInfoList);
            }
            methodInfoList.add(methodInfo);
        }
        return methodNameToMethodInfoList;
    }
    
    public boolean containsName(final String methodName) {
        for (final MethodInfo mi : this) {
            if (mi.getName().equals(methodName)) {
                return true;
            }
        }
        return false;
    }
    
    public MethodInfoList get(final String methodName) {
        boolean hasMethodWithName = false;
        for (final MethodInfo mi : this) {
            if (mi.getName().equals(methodName)) {
                hasMethodWithName = true;
                break;
            }
        }
        if (!hasMethodWithName) {
            return MethodInfoList.EMPTY_LIST;
        }
        final MethodInfoList matchingMethods = new MethodInfoList(2);
        for (final MethodInfo mi2 : this) {
            if (mi2.getName().equals(methodName)) {
                matchingMethods.add(mi2);
            }
        }
        return matchingMethods;
    }
    
    public MethodInfo getSingleMethod(final String methodName) {
        int numMethodsWithName = 0;
        MethodInfo lastFoundMethod = null;
        for (final MethodInfo mi : this) {
            if (mi.getName().equals(methodName)) {
                ++numMethodsWithName;
                lastFoundMethod = mi;
            }
        }
        if (numMethodsWithName == 0) {
            return null;
        }
        if (numMethodsWithName == 1) {
            return lastFoundMethod;
        }
        throw new IllegalArgumentException("There are multiple methods named \"" + methodName + "\" in class " + this.iterator().next().getName());
    }
    
    public MethodInfoList filter(final MethodInfoFilter filter) {
        final MethodInfoList methodInfoFiltered = new MethodInfoList();
        for (final MethodInfo resource : this) {
            if (filter.accept(resource)) {
                methodInfoFiltered.add(resource);
            }
        }
        return methodInfoFiltered;
    }
    
    static {
        (EMPTY_LIST = new MethodInfoList()).makeUnmodifiable();
    }
    
    @FunctionalInterface
    public interface MethodInfoFilter
    {
        boolean accept(final MethodInfo p0);
    }
}
