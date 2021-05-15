// 
// Decompiled by Procyon v0.5.36
// 

package io.github.classgraph;

import java.util.Arrays;
import java.util.Objects;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Collections;

public class MethodParameterInfo
{
    private final MethodInfo methodInfo;
    final AnnotationInfo[] annotationInfo;
    private final int modifiers;
    private final TypeSignature typeDescriptor;
    private final TypeSignature typeSignature;
    private final String name;
    private ScanResult scanResult;
    
    MethodParameterInfo(final MethodInfo methodInfo, final AnnotationInfo[] annotationInfo, final int modifiers, final TypeSignature typeDescriptor, final TypeSignature typeSignature, final String name) {
        this.methodInfo = methodInfo;
        this.name = name;
        this.modifiers = modifiers;
        this.typeDescriptor = typeDescriptor;
        this.typeSignature = typeSignature;
        this.annotationInfo = annotationInfo;
    }
    
    public MethodInfo getMethodInfo() {
        return this.methodInfo;
    }
    
    public String getName() {
        return this.name;
    }
    
    public int getModifiers() {
        return this.modifiers;
    }
    
    public String getModifiersStr() {
        final StringBuilder buf = new StringBuilder();
        modifiersToString(this.modifiers, buf);
        return buf.toString();
    }
    
    public TypeSignature getTypeSignature() {
        return this.typeSignature;
    }
    
    public TypeSignature getTypeDescriptor() {
        return this.typeDescriptor;
    }
    
    public TypeSignature getTypeSignatureOrTypeDescriptor() {
        return (this.typeSignature != null) ? this.typeSignature : this.typeDescriptor;
    }
    
    public AnnotationInfoList getAnnotationInfo() {
        if (!this.scanResult.scanSpec.enableAnnotationInfo) {
            throw new IllegalArgumentException("Please call ClassGraph#enableAnnotationInfo() before #scan()");
        }
        if (this.annotationInfo == null || this.annotationInfo.length == 0) {
            return AnnotationInfoList.EMPTY_LIST;
        }
        final AnnotationInfoList annotationInfoList = new AnnotationInfoList(this.annotationInfo.length);
        Collections.addAll(annotationInfoList, this.annotationInfo);
        return AnnotationInfoList.getIndirectAnnotations(annotationInfoList, null);
    }
    
    public AnnotationInfo getAnnotationInfo(final String annotationName) {
        return this.getAnnotationInfo().get(annotationName);
    }
    
    public AnnotationInfoList getAnnotationInfoRepeatable(final String annotationName) {
        return this.getAnnotationInfo().getRepeatable(annotationName);
    }
    
    public boolean hasAnnotation(final String annotationName) {
        return this.getAnnotationInfo().containsName(annotationName);
    }
    
    protected void setScanResult(final ScanResult scanResult) {
        this.scanResult = scanResult;
        if (this.annotationInfo != null) {
            for (final AnnotationInfo ai : this.annotationInfo) {
                ai.setScanResult(scanResult);
            }
        }
        if (this.typeDescriptor != null) {
            this.typeDescriptor.setScanResult(scanResult);
        }
        if (this.typeSignature != null) {
            this.typeSignature.setScanResult(scanResult);
        }
    }
    
    public boolean isFinal() {
        return Modifier.isFinal(this.modifiers);
    }
    
    public boolean isSynthetic() {
        return (this.modifiers & 0x1000) != 0x0;
    }
    
    public boolean isMandated() {
        return (this.modifiers & 0x8000) != 0x0;
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof MethodParameterInfo)) {
            return false;
        }
        final MethodParameterInfo other = (MethodParameterInfo)obj;
        return Objects.equals(this.methodInfo, other.methodInfo) && Objects.deepEquals(this.annotationInfo, other.annotationInfo) && this.modifiers == other.modifiers && Objects.equals(this.typeDescriptor, other.typeDescriptor) && Objects.equals(this.typeSignature, other.typeSignature) && Objects.equals(this.name, other.name);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(this.methodInfo, Arrays.hashCode(this.annotationInfo), this.typeDescriptor, this.typeSignature, this.name) + this.modifiers;
    }
    
    static void modifiersToString(final int modifiers, final StringBuilder buf) {
        if ((modifiers & 0x10) != 0x0) {
            buf.append("final ");
        }
        if ((modifiers & 0x1000) != 0x0) {
            buf.append("synthetic ");
        }
        if ((modifiers & 0x8000) != 0x0) {
            buf.append("mandated ");
        }
    }
    
    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder();
        if (this.annotationInfo != null) {
            for (final AnnotationInfo anAnnotationInfo : this.annotationInfo) {
                anAnnotationInfo.toString(buf);
                buf.append(' ');
            }
        }
        modifiersToString(this.modifiers, buf);
        buf.append(this.getTypeSignatureOrTypeDescriptor().toString());
        buf.append(' ');
        buf.append((this.name == null) ? "_unnamed_param" : this.name);
        return buf.toString();
    }
}
