// 
// Decompiled by Procyon v0.5.36
// 

package io.github.classgraph;

import java.util.Iterator;
import java.util.HashSet;
import java.util.List;
import nonapi.io.github.classgraph.utils.CollectionUtils;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class PackageInfo implements Comparable<PackageInfo>, HasName
{
    private String name;
    private Set<AnnotationInfo> annotationInfoSet;
    private AnnotationInfoList annotationInfo;
    private PackageInfo parent;
    private Set<PackageInfo> children;
    private Map<String, ClassInfo> memberClassNameToClassInfo;
    
    PackageInfo() {
    }
    
    PackageInfo(final String packageName) {
        this.name = packageName;
    }
    
    @Override
    public String getName() {
        return this.name;
    }
    
    void addAnnotations(final AnnotationInfoList packageAnnotations) {
        if (packageAnnotations != null && !packageAnnotations.isEmpty()) {
            if (this.annotationInfoSet == null) {
                this.annotationInfoSet = new LinkedHashSet<AnnotationInfo>();
            }
            this.annotationInfoSet.addAll(packageAnnotations);
        }
    }
    
    void addClassInfo(final ClassInfo classInfo) {
        if (this.memberClassNameToClassInfo == null) {
            this.memberClassNameToClassInfo = new HashMap<String, ClassInfo>();
        }
        this.memberClassNameToClassInfo.put(classInfo.getName(), classInfo);
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
    
    public PackageInfo getParent() {
        return this.parent;
    }
    
    public PackageInfoList getChildren() {
        if (this.children == null) {
            return PackageInfoList.EMPTY_LIST;
        }
        final PackageInfoList childrenSorted = new PackageInfoList(this.children);
        CollectionUtils.sortIfNotEmpty((List<Object>)childrenSorted, (Comparator<? super Object>)new Comparator<PackageInfo>() {
            @Override
            public int compare(final PackageInfo o1, final PackageInfo o2) {
                return o1.name.compareTo(o2.name);
            }
        });
        return childrenSorted;
    }
    
    public ClassInfo getClassInfo(final String className) {
        return (this.memberClassNameToClassInfo == null) ? null : this.memberClassNameToClassInfo.get(className);
    }
    
    public ClassInfoList getClassInfo() {
        return (this.memberClassNameToClassInfo == null) ? ClassInfoList.EMPTY_LIST : new ClassInfoList(new HashSet<ClassInfo>(this.memberClassNameToClassInfo.values()), true);
    }
    
    private void obtainClassInfoRecursive(final Set<ClassInfo> reachableClassInfo) {
        if (this.memberClassNameToClassInfo != null) {
            reachableClassInfo.addAll(this.memberClassNameToClassInfo.values());
        }
        for (final PackageInfo subPackageInfo : this.getChildren()) {
            subPackageInfo.obtainClassInfoRecursive(reachableClassInfo);
        }
    }
    
    public ClassInfoList getClassInfoRecursive() {
        final Set<ClassInfo> reachableClassInfo = new HashSet<ClassInfo>();
        this.obtainClassInfoRecursive(reachableClassInfo);
        return new ClassInfoList(reachableClassInfo, true);
    }
    
    static String getParentPackageName(final String packageOrClassName) {
        if (packageOrClassName.isEmpty()) {
            return null;
        }
        final int lastDotIdx = packageOrClassName.lastIndexOf(46);
        return (lastDotIdx < 0) ? "" : packageOrClassName.substring(0, lastDotIdx);
    }
    
    static PackageInfo getOrCreatePackage(final String packageName, final Map<String, PackageInfo> packageNameToPackageInfo) {
        PackageInfo packageInfo = packageNameToPackageInfo.get(packageName);
        if (packageInfo != null) {
            return packageInfo;
        }
        packageNameToPackageInfo.put(packageName, packageInfo = new PackageInfo(packageName));
        if (!packageName.isEmpty()) {
            final PackageInfo parentPackageInfo = getOrCreatePackage(getParentPackageName(packageInfo.name), packageNameToPackageInfo);
            if (parentPackageInfo != null) {
                if (parentPackageInfo.children == null) {
                    parentPackageInfo.children = new HashSet<PackageInfo>();
                }
                parentPackageInfo.children.add(packageInfo);
                packageInfo.parent = parentPackageInfo;
            }
        }
        return packageInfo;
    }
    
    @Override
    public int compareTo(final PackageInfo o) {
        return this.name.compareTo(o.name);
    }
    
    @Override
    public int hashCode() {
        return this.name.hashCode();
    }
    
    @Override
    public boolean equals(final Object obj) {
        return obj == this || (obj instanceof PackageInfo && this.name.equals(((PackageInfo)obj).name));
    }
    
    @Override
    public String toString() {
        return this.name;
    }
}
