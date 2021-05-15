// 
// Decompiled by Procyon v0.5.36
// 

package io.github.classgraph;

import java.util.Set;
import java.util.Map;

public class ArrayClassInfo extends ClassInfo
{
    private ArrayTypeSignature arrayTypeSignature;
    private ClassInfo elementClassInfo;
    
    ArrayClassInfo() {
    }
    
    ArrayClassInfo(final ArrayTypeSignature arrayTypeSignature) {
        super(arrayTypeSignature.getClassName(), 0, null);
        this.arrayTypeSignature = arrayTypeSignature;
        this.getElementClassInfo();
    }
    
    @Override
    void setScanResult(final ScanResult scanResult) {
        super.setScanResult(scanResult);
    }
    
    @Override
    public String getTypeSignatureStr() {
        return this.arrayTypeSignature.getTypeSignatureStr();
    }
    
    @Override
    public ClassTypeSignature getTypeSignature() {
        return null;
    }
    
    public ArrayTypeSignature getArrayTypeSignature() {
        return this.arrayTypeSignature;
    }
    
    public TypeSignature getElementTypeSignature() {
        return this.arrayTypeSignature.getElementTypeSignature();
    }
    
    public int getNumDimensions() {
        return this.arrayTypeSignature.getNumDimensions();
    }
    
    public ClassInfo getElementClassInfo() {
        if (this.elementClassInfo == null) {
            final TypeSignature elementTypeSignature = this.arrayTypeSignature.getElementTypeSignature();
            if (!(elementTypeSignature instanceof BaseTypeSignature)) {
                this.elementClassInfo = this.arrayTypeSignature.getElementTypeSignature().getClassInfo();
                if (this.elementClassInfo != null) {
                    this.classpathElement = this.elementClassInfo.classpathElement;
                    this.classfileResource = this.elementClassInfo.classfileResource;
                    this.classLoader = this.elementClassInfo.classLoader;
                    this.isScannedClass = this.elementClassInfo.isScannedClass;
                    this.isExternalClass = this.elementClassInfo.isExternalClass;
                    this.moduleInfo = this.elementClassInfo.moduleInfo;
                    this.packageInfo = this.elementClassInfo.packageInfo;
                }
            }
        }
        return this.elementClassInfo;
    }
    
    public Class<?> loadElementClass(final boolean ignoreExceptions) {
        return this.arrayTypeSignature.loadElementClass(ignoreExceptions);
    }
    
    public Class<?> loadElementClass() {
        return this.arrayTypeSignature.loadElementClass();
    }
    
    @Override
    public Class<?> loadClass(final boolean ignoreExceptions) {
        if (this.classRef == null) {
            this.classRef = this.arrayTypeSignature.loadClass(ignoreExceptions);
        }
        return this.classRef;
    }
    
    @Override
    public Class<?> loadClass() {
        if (this.classRef == null) {
            this.classRef = this.arrayTypeSignature.loadClass();
        }
        return this.classRef;
    }
    
    @Override
    protected void findReferencedClassInfo(final Map<String, ClassInfo> classNameToClassInfo, final Set<ClassInfo> refdClassInfo) {
        super.findReferencedClassInfo(classNameToClassInfo, refdClassInfo);
    }
    
    @Override
    public boolean equals(final Object obj) {
        return super.equals(obj);
    }
    
    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
