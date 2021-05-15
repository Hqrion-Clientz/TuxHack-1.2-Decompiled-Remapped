// 
// Decompiled by Procyon v0.5.36
// 

package io.github.classgraph;

import nonapi.io.github.classgraph.types.ParseException;

public class AnnotationClassRef extends ScanResultObject
{
    private String typeDescriptorStr;
    private transient TypeSignature typeSignature;
    private transient String className;
    
    AnnotationClassRef() {
    }
    
    AnnotationClassRef(final String typeDescriptorStr) {
        this.typeDescriptorStr = typeDescriptorStr;
    }
    
    public String getName() {
        return this.getClassName();
    }
    
    private TypeSignature getTypeSignature() {
        if (this.typeSignature == null) {
            try {
                (this.typeSignature = TypeSignature.parse(this.typeDescriptorStr, null)).setScanResult(this.scanResult);
            }
            catch (ParseException e) {
                throw new IllegalArgumentException(e);
            }
        }
        return this.typeSignature;
    }
    
    public Class<?> loadClass(final boolean ignoreExceptions) {
        this.getTypeSignature();
        if (this.typeSignature instanceof BaseTypeSignature) {
            return ((BaseTypeSignature)this.typeSignature).getType();
        }
        if (this.typeSignature instanceof ClassRefTypeSignature) {
            return ((ClassRefTypeSignature)this.typeSignature).loadClass(ignoreExceptions);
        }
        if (this.typeSignature instanceof ArrayTypeSignature) {
            return ((ArrayTypeSignature)this.typeSignature).loadClass(ignoreExceptions);
        }
        throw new IllegalArgumentException("Got unexpected type " + this.typeSignature.getClass().getName() + " for ref type signature: " + this.typeDescriptorStr);
    }
    
    public Class<?> loadClass() {
        return this.loadClass(false);
    }
    
    @Override
    protected String getClassName() {
        if (this.className == null) {
            this.getTypeSignature();
            if (this.typeSignature instanceof BaseTypeSignature) {
                this.className = ((BaseTypeSignature)this.typeSignature).getTypeStr();
            }
            else if (this.typeSignature instanceof ClassRefTypeSignature) {
                this.className = ((ClassRefTypeSignature)this.typeSignature).getFullyQualifiedClassName();
            }
            else {
                if (!(this.typeSignature instanceof ArrayTypeSignature)) {
                    throw new IllegalArgumentException("Got unexpected type " + this.typeSignature.getClass().getName() + " for ref type signature: " + this.typeDescriptorStr);
                }
                this.className = ((ArrayTypeSignature)this.typeSignature).getClassName();
            }
        }
        return this.className;
    }
    
    public ClassInfo getClassInfo() {
        this.getTypeSignature();
        return this.typeSignature.getClassInfo();
    }
    
    @Override
    void setScanResult(final ScanResult scanResult) {
        super.setScanResult(scanResult);
        if (this.typeSignature != null) {
            this.typeSignature.setScanResult(scanResult);
        }
    }
    
    @Override
    public int hashCode() {
        return this.getTypeSignature().hashCode();
    }
    
    @Override
    public boolean equals(final Object obj) {
        return obj == this || (obj instanceof AnnotationClassRef && this.getTypeSignature().equals(((AnnotationClassRef)obj).getTypeSignature()));
    }
    
    @Override
    public String toString() {
        if (this.scanResult != null) {
            final ClassInfo ci = this.getClassInfo();
            if (ci == null || ci.isInterfaceOrAnnotation()) {}
        }
        return this.getTypeSignature().toString() + ".class";
    }
}
