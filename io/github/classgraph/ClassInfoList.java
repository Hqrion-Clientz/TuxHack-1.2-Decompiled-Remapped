// 
// Decompiled by Procyon v0.5.36
// 

package io.github.classgraph;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.File;
import nonapi.io.github.classgraph.scanspec.ScanSpec;
import java.util.ArrayDeque;
import java.util.LinkedHashSet;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import nonapi.io.github.classgraph.utils.CollectionUtils;
import java.util.Collection;
import java.util.Set;

public class ClassInfoList extends MappableInfoList<ClassInfo>
{
    private final transient Set<ClassInfo> directlyRelatedClasses;
    private final boolean sortByName;
    private static final long serialVersionUID = 1L;
    static final ClassInfoList EMPTY_LIST;
    
    public static ClassInfoList emptyList() {
        return ClassInfoList.EMPTY_LIST;
    }
    
    ClassInfoList(final Set<ClassInfo> reachableClasses, final Set<ClassInfo> directlyRelatedClasses, final boolean sortByName) {
        super(reachableClasses);
        this.sortByName = sortByName;
        if (sortByName) {
            CollectionUtils.sortIfNotEmpty((List<Comparable>)this);
        }
        this.directlyRelatedClasses = ((directlyRelatedClasses == null) ? reachableClasses : directlyRelatedClasses);
    }
    
    ClassInfoList(final ClassInfo.ReachableAndDirectlyRelatedClasses reachableAndDirectlyRelatedClasses, final boolean sortByName) {
        this(reachableAndDirectlyRelatedClasses.reachableClasses, reachableAndDirectlyRelatedClasses.directlyRelatedClasses, sortByName);
    }
    
    ClassInfoList(final Set<ClassInfo> reachableClasses, final boolean sortByName) {
        this(reachableClasses, null, sortByName);
    }
    
    public ClassInfoList() {
        super(1);
        this.sortByName = false;
        this.directlyRelatedClasses = new HashSet<ClassInfo>(2);
    }
    
    public ClassInfoList(final int sizeHint) {
        super(sizeHint);
        this.sortByName = false;
        this.directlyRelatedClasses = new HashSet<ClassInfo>(2);
    }
    
    public ClassInfoList(final Collection<ClassInfo> classInfoCollection) {
        this((classInfoCollection instanceof Set) ? ((Set)classInfoCollection) : new HashSet<ClassInfo>(classInfoCollection), null, true);
    }
    
    public <T> List<Class<T>> loadClasses(final Class<T> superclassOrInterfaceType, final boolean ignoreExceptions) {
        if (this.isEmpty()) {
            return Collections.emptyList();
        }
        final List<Class<T>> classRefs = new ArrayList<Class<T>>();
        for (final ClassInfo classInfo : this) {
            final Class<T> classRef = classInfo.loadClass(superclassOrInterfaceType, ignoreExceptions);
            if (classRef != null) {
                classRefs.add(classRef);
            }
        }
        return classRefs.isEmpty() ? Collections.emptyList() : classRefs;
    }
    
    public <T> List<Class<T>> loadClasses(final Class<T> superclassOrInterfaceType) {
        return this.loadClasses(superclassOrInterfaceType, false);
    }
    
    public List<Class<?>> loadClasses(final boolean ignoreExceptions) {
        if (this.isEmpty()) {
            return Collections.emptyList();
        }
        final List<Class<?>> classRefs = new ArrayList<Class<?>>();
        for (final ClassInfo classInfo : this) {
            final Class<?> classRef = classInfo.loadClass(ignoreExceptions);
            if (classRef != null) {
                classRefs.add(classRef);
            }
        }
        return classRefs.isEmpty() ? Collections.emptyList() : classRefs;
    }
    
    public List<Class<?>> loadClasses() {
        return this.loadClasses(false);
    }
    
    public ClassInfoList directOnly() {
        return new ClassInfoList(this.directlyRelatedClasses, this.directlyRelatedClasses, this.sortByName);
    }
    
    public ClassInfoList union(final ClassInfoList... others) {
        final Set<ClassInfo> reachableClassesUnion = new LinkedHashSet<ClassInfo>(this);
        final Set<ClassInfo> directlyRelatedClassesUnion = new LinkedHashSet<ClassInfo>(this.directlyRelatedClasses);
        for (final ClassInfoList other : others) {
            reachableClassesUnion.addAll(other);
            directlyRelatedClassesUnion.addAll(other.directlyRelatedClasses);
        }
        return new ClassInfoList(reachableClassesUnion, directlyRelatedClassesUnion, this.sortByName);
    }
    
    public ClassInfoList intersect(final ClassInfoList... others) {
        final ArrayDeque<ClassInfoList> intersectionOrder = new ArrayDeque<ClassInfoList>();
        intersectionOrder.add(this);
        boolean foundFirst = false;
        for (final ClassInfoList other : others) {
            if (other.sortByName) {
                intersectionOrder.add(other);
            }
            else if (!foundFirst) {
                foundFirst = true;
                intersectionOrder.push(other);
            }
            else {
                intersectionOrder.add(other);
            }
        }
        final ClassInfoList first = intersectionOrder.remove();
        final Set<ClassInfo> reachableClassesIntersection = new LinkedHashSet<ClassInfo>(first);
        while (!intersectionOrder.isEmpty()) {
            reachableClassesIntersection.retainAll(intersectionOrder.remove());
        }
        final Set<ClassInfo> directlyRelatedClassesIntersection = new LinkedHashSet<ClassInfo>(this.directlyRelatedClasses);
        for (final ClassInfoList other2 : others) {
            directlyRelatedClassesIntersection.retainAll(other2.directlyRelatedClasses);
        }
        return new ClassInfoList(reachableClassesIntersection, directlyRelatedClassesIntersection, first.sortByName);
    }
    
    public ClassInfoList exclude(final ClassInfoList other) {
        final Set<ClassInfo> reachableClassesDifference = new LinkedHashSet<ClassInfo>(this);
        final Set<ClassInfo> directlyRelatedClassesDifference = new LinkedHashSet<ClassInfo>(this.directlyRelatedClasses);
        reachableClassesDifference.removeAll(other);
        directlyRelatedClassesDifference.removeAll(other.directlyRelatedClasses);
        return new ClassInfoList(reachableClassesDifference, directlyRelatedClassesDifference, this.sortByName);
    }
    
    public ClassInfoList filter(final ClassInfoFilter filter) {
        final Set<ClassInfo> reachableClassesFiltered = new LinkedHashSet<ClassInfo>(this.size());
        final Set<ClassInfo> directlyRelatedClassesFiltered = new LinkedHashSet<ClassInfo>(this.directlyRelatedClasses.size());
        for (final ClassInfo ci : this) {
            if (filter.accept(ci)) {
                reachableClassesFiltered.add(ci);
                if (!this.directlyRelatedClasses.contains(ci)) {
                    continue;
                }
                directlyRelatedClassesFiltered.add(ci);
            }
        }
        return new ClassInfoList(reachableClassesFiltered, directlyRelatedClassesFiltered, this.sortByName);
    }
    
    public ClassInfoList getStandardClasses() {
        return this.filter(new ClassInfoFilter() {
            @Override
            public boolean accept(final ClassInfo ci) {
                return ci.isStandardClass();
            }
        });
    }
    
    public ClassInfoList getInterfaces() {
        return this.filter(new ClassInfoFilter() {
            @Override
            public boolean accept(final ClassInfo ci) {
                return ci.isInterface();
            }
        });
    }
    
    public ClassInfoList getInterfacesAndAnnotations() {
        return this.filter(new ClassInfoFilter() {
            @Override
            public boolean accept(final ClassInfo ci) {
                return ci.isInterfaceOrAnnotation();
            }
        });
    }
    
    public ClassInfoList getImplementedInterfaces() {
        return this.filter(new ClassInfoFilter() {
            @Override
            public boolean accept(final ClassInfo ci) {
                return ci.isImplementedInterface();
            }
        });
    }
    
    public ClassInfoList getAnnotations() {
        return this.filter(new ClassInfoFilter() {
            @Override
            public boolean accept(final ClassInfo ci) {
                return ci.isAnnotation();
            }
        });
    }
    
    public ClassInfoList getEnums() {
        return this.filter(new ClassInfoFilter() {
            @Override
            public boolean accept(final ClassInfo ci) {
                return ci.isEnum();
            }
        });
    }
    
    public ClassInfoList getRecords() {
        return this.filter(new ClassInfoFilter() {
            @Override
            public boolean accept(final ClassInfo ci) {
                return ci.isRecord();
            }
        });
    }
    
    public ClassInfoList getAssignableTo(final ClassInfo superclassOrInterface) {
        if (superclassOrInterface == null) {
            throw new IllegalArgumentException("assignableToClass parameter cannot be null");
        }
        final Set<ClassInfo> allAssignableFromClasses = new HashSet<ClassInfo>();
        if (superclassOrInterface.isStandardClass()) {
            allAssignableFromClasses.addAll(superclassOrInterface.getSubclasses());
        }
        else if (superclassOrInterface.isInterfaceOrAnnotation()) {
            allAssignableFromClasses.addAll(superclassOrInterface.getClassesImplementing());
        }
        allAssignableFromClasses.add(superclassOrInterface);
        return this.filter(new ClassInfoFilter() {
            @Override
            public boolean accept(final ClassInfo ci) {
                return allAssignableFromClasses.contains(ci);
            }
        });
    }
    
    public String generateGraphVizDotFileFromInterClassDependencies(final float sizeX, final float sizeY, final boolean includeExternalClasses) {
        if (this.isEmpty()) {
            throw new IllegalArgumentException("List is empty");
        }
        final ScanSpec scanSpec = this.get(0).scanResult.scanSpec;
        if (!scanSpec.enableInterClassDependencies) {
            throw new IllegalArgumentException("Please call ClassGraph#enableInterClassDependencies() before #scan()");
        }
        return GraphvizDotfileGenerator.generateGraphVizDotFileFromInterClassDependencies(this, sizeX, sizeY, includeExternalClasses);
    }
    
    public String generateGraphVizDotFileFromClassDependencies() {
        if (this.isEmpty()) {
            throw new IllegalArgumentException("List is empty");
        }
        final ScanSpec scanSpec = this.get(0).scanResult.scanSpec;
        if (!scanSpec.enableInterClassDependencies) {
            throw new IllegalArgumentException("Please call ClassGraph#enableInterClassDependencies() before #scan()");
        }
        return GraphvizDotfileGenerator.generateGraphVizDotFileFromInterClassDependencies(this, 10.5f, 8.0f, scanSpec.enableExternalClasses);
    }
    
    public String generateGraphVizDotFile(final float sizeX, final float sizeY, final boolean showFields, final boolean showFieldTypeDependencyEdges, final boolean showMethods, final boolean showMethodTypeDependencyEdges, final boolean showAnnotations, final boolean useSimpleNames) {
        if (this.isEmpty()) {
            throw new IllegalArgumentException("List is empty");
        }
        final ScanSpec scanSpec = this.get(0).scanResult.scanSpec;
        if (!scanSpec.enableClassInfo) {
            throw new IllegalArgumentException("Please call ClassGraph#enableClassInfo() before #scan()");
        }
        return GraphvizDotfileGenerator.generateGraphVizDotFile(this, sizeX, sizeY, showFields, showFieldTypeDependencyEdges, showMethods, showMethodTypeDependencyEdges, showAnnotations, useSimpleNames, scanSpec);
    }
    
    public String generateGraphVizDotFile(final float sizeX, final float sizeY, final boolean showFields, final boolean showFieldTypeDependencyEdges, final boolean showMethods, final boolean showMethodTypeDependencyEdges, final boolean showAnnotations) {
        return this.generateGraphVizDotFile(sizeX, sizeY, showFields, showFieldTypeDependencyEdges, showMethods, showMethodTypeDependencyEdges, showAnnotations, true);
    }
    
    public String generateGraphVizDotFile(final float sizeX, final float sizeY) {
        return this.generateGraphVizDotFile(sizeX, sizeY, true, true, true, true, true);
    }
    
    public String generateGraphVizDotFile() {
        return this.generateGraphVizDotFile(10.5f, 8.0f, true, true, true, true, true);
    }
    
    public void generateGraphVizDotFile(final File file) throws IOException {
        try (final PrintWriter writer = new PrintWriter(file)) {
            writer.print(this.generateGraphVizDotFile());
        }
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ClassInfoList)) {
            return false;
        }
        final ClassInfoList other = (ClassInfoList)obj;
        if (this.directlyRelatedClasses == null != (other.directlyRelatedClasses == null)) {
            return false;
        }
        if (this.directlyRelatedClasses == null) {
            return super.equals(other);
        }
        return super.equals(other) && this.directlyRelatedClasses.equals(other.directlyRelatedClasses);
    }
    
    @Override
    public int hashCode() {
        return super.hashCode() ^ ((this.directlyRelatedClasses == null) ? 0 : this.directlyRelatedClasses.hashCode());
    }
    
    static {
        (EMPTY_LIST = new ClassInfoList()).makeUnmodifiable();
    }
    
    @FunctionalInterface
    public interface ClassInfoFilter
    {
        boolean accept(final ClassInfo p0);
    }
}
