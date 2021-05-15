// 
// Decompiled by Procyon v0.5.36
// 

package io.github.classgraph;

import java.util.Map;
import java.util.LinkedHashSet;
import java.util.Set;

abstract class ScanResultObject
{
    protected transient ScanResult scanResult;
    private transient ClassInfo classInfo;
    protected transient Class<?> classRef;
    
    void setScanResult(final ScanResult scanResult) {
        this.scanResult = scanResult;
    }
    
    final Set<ClassInfo> findReferencedClassInfo() {
        final Set<ClassInfo> refdClassInfo = new LinkedHashSet<ClassInfo>();
        if (this.scanResult != null) {
            this.findReferencedClassInfo(this.scanResult.classNameToClassInfo, refdClassInfo);
        }
        return refdClassInfo;
    }
    
    protected void findReferencedClassInfo(final Map<String, ClassInfo> classNameToClassInfo, final Set<ClassInfo> refdClassInfo) {
        final ClassInfo ci = this.getClassInfo();
        if (ci != null) {
            refdClassInfo.add(ci);
        }
    }
    
    protected abstract String getClassName();
    
    ClassInfo getClassInfo() {
        if (this.classInfo == null) {
            if (this.scanResult == null) {
                return null;
            }
            final String className = this.getClassName();
            if (className == null) {
                throw new IllegalArgumentException("Class name is not set");
            }
            this.classInfo = this.scanResult.getClassInfo(className);
        }
        return this.classInfo;
    }
    
    private String getClassInfoNameOrClassName() {
        ClassInfo ci = null;
        try {
            ci = this.getClassInfo();
        }
        catch (IllegalArgumentException ex) {}
        if (ci == null) {
            ci = this.classInfo;
        }
        String className;
        if (ci != null) {
            className = ci.getName();
        }
        else {
            className = this.getClassName();
        }
        if (className == null) {
            throw new IllegalArgumentException("Class name is not set");
        }
        return className;
    }
    
     <T> Class<T> loadClass(final Class<T> superclassOrInterfaceType, final boolean ignoreExceptions) {
        if (this.classRef == null) {
            final String className = this.getClassInfoNameOrClassName();
            if (this.scanResult != null) {
                this.classRef = this.scanResult.loadClass(className, (Class<?>)superclassOrInterfaceType, ignoreExceptions);
            }
            else {
                try {
                    this.classRef = Class.forName(className);
                }
                catch (Throwable t) {
                    if (!ignoreExceptions) {
                        throw new IllegalArgumentException("Could not load class " + className, t);
                    }
                }
            }
        }
        final Class<T> classT = (Class<T>)this.classRef;
        return classT;
    }
    
     <T> Class<T> loadClass(final Class<T> superclassOrInterfaceType) {
        return this.loadClass(superclassOrInterfaceType, false);
    }
    
    Class<?> loadClass(final boolean ignoreExceptions) {
        if (this.classRef == null) {
            final String className = this.getClassInfoNameOrClassName();
            if (this.scanResult != null) {
                this.classRef = this.scanResult.loadClass(className, ignoreExceptions);
            }
            else {
                try {
                    this.classRef = Class.forName(className);
                }
                catch (Throwable t) {
                    if (!ignoreExceptions) {
                        throw new IllegalArgumentException("Could not load class " + className, t);
                    }
                }
            }
        }
        return this.classRef;
    }
    
    Class<?> loadClass() {
        return this.loadClass(false);
    }
}
