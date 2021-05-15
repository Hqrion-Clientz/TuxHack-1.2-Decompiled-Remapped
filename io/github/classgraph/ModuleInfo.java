// 
// Decompiled by Procyon v0.5.36
// 

package io.github.classgraph;

import java.util.LinkedHashSet;
import java.util.List;
import nonapi.io.github.classgraph.utils.CollectionUtils;
import java.util.Collection;
import java.util.Iterator;
import java.util.HashSet;
import java.util.Set;
import java.net.URI;

public class ModuleInfo implements Comparable<ModuleInfo>, HasName
{
    private String name;
    private transient ClasspathElement classpathElement;
    private transient ModuleRef moduleRef;
    private transient URI locationURI;
    private Set<AnnotationInfo> annotationInfoSet;
    private AnnotationInfoList annotationInfo;
    private Set<PackageInfo> packageInfoSet;
    private Set<ClassInfo> classInfoSet;
    
    ModuleInfo() {
    }
    
    ModuleInfo(final ModuleRef moduleRef, final ClasspathElement classpathElement) {
        this.moduleRef = moduleRef;
        this.classpathElement = classpathElement;
        this.name = classpathElement.getModuleName();
    }
    
    @Override
    public String getName() {
        return this.name;
    }
    
    public URI getLocation() {
        if (this.locationURI == null) {
            this.locationURI = ((this.moduleRef != null) ? this.moduleRef.getLocation() : null);
            if (this.locationURI == null) {
                this.locationURI = this.classpathElement.getURI();
            }
        }
        return this.locationURI;
    }
    
    public ModuleRef getModuleRef() {
        return this.moduleRef;
    }
    
    void addClassInfo(final ClassInfo classInfo) {
        if (this.classInfoSet == null) {
            this.classInfoSet = new HashSet<ClassInfo>();
        }
        this.classInfoSet.add(classInfo);
    }
    
    public ClassInfo getClassInfo(final String className) {
        for (final ClassInfo ci : this.classInfoSet) {
            if (ci.getName().equals(className)) {
                return ci;
            }
        }
        return null;
    }
    
    public ClassInfoList getClassInfo() {
        return new ClassInfoList(this.classInfoSet, true);
    }
    
    void addPackageInfo(final PackageInfo packageInfo) {
        if (this.packageInfoSet == null) {
            this.packageInfoSet = new HashSet<PackageInfo>();
        }
        this.packageInfoSet.add(packageInfo);
    }
    
    public PackageInfo getPackageInfo(final String packageName) {
        if (this.packageInfoSet == null) {
            return null;
        }
        for (final PackageInfo pi : this.packageInfoSet) {
            if (pi.getName().equals(packageName)) {
                return pi;
            }
        }
        return null;
    }
    
    public PackageInfoList getPackageInfo() {
        if (this.packageInfoSet == null) {
            return new PackageInfoList(1);
        }
        final PackageInfoList packageInfoList = new PackageInfoList(this.packageInfoSet);
        CollectionUtils.sortIfNotEmpty((List<Comparable>)packageInfoList);
        return packageInfoList;
    }
    
    void addAnnotations(final AnnotationInfoList moduleAnnotations) {
        if (moduleAnnotations != null && !moduleAnnotations.isEmpty()) {
            if (this.annotationInfoSet == null) {
                this.annotationInfoSet = new LinkedHashSet<AnnotationInfo>();
            }
            this.annotationInfoSet.addAll(moduleAnnotations);
        }
    }
    
    public AnnotationInfo getAnnotationInfo(final String annotationName) {
        return this.getAnnotationInfo().get(annotationName);
    }
    
    public AnnotationInfoList getAnnotationInfo() {
        if (this.annotationInfo == null) {
            if (this.annotationInfoSet == null) {
                this.annotationInfo = AnnotationInfoList.EMPTY_LIST;
            }
            else {
                (this.annotationInfo = new AnnotationInfoList()).addAll(this.annotationInfoSet);
            }
        }
        return this.annotationInfo;
    }
    
    public boolean hasAnnotation(final String annotationName) {
        return this.getAnnotationInfo().containsName(annotationName);
    }
    
    @Override
    public int compareTo(final ModuleInfo other) {
        final int diff = this.name.compareTo(other.name);
        if (diff != 0) {
            return diff;
        }
        final URI thisLoc = this.getLocation();
        final URI otherLoc = other.getLocation();
        if (thisLoc != null && otherLoc != null) {
            return thisLoc.compareTo(otherLoc);
        }
        return 0;
    }
    
    @Override
    public int hashCode() {
        return this.name.hashCode();
    }
    
    @Override
    public boolean equals(final Object obj) {
        return obj == this || (obj instanceof ModuleInfo && this.compareTo((ModuleInfo)obj) == 0);
    }
    
    @Override
    public String toString() {
        return this.name;
    }
}
