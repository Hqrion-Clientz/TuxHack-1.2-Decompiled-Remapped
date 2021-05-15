// 
// Decompiled by Procyon v0.5.36
// 

package io.github.classgraph;

import nonapi.io.github.classgraph.utils.Join;
import nonapi.io.github.classgraph.types.TypeUtils;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;
import java.util.Collections;
import nonapi.io.github.classgraph.types.ParseException;
import java.util.ArrayList;
import nonapi.io.github.classgraph.types.Parser;
import java.util.List;

public final class ClassTypeSignature extends HierarchicalTypeSignature
{
    private final ClassInfo classInfo;
    final List<TypeParameter> typeParameters;
    private final ClassRefTypeSignature superclassSignature;
    private final List<ClassRefTypeSignature> superinterfaceSignatures;
    
    private ClassTypeSignature(final ClassInfo classInfo, final List<TypeParameter> typeParameters, final ClassRefTypeSignature superclassSignature, final List<ClassRefTypeSignature> superinterfaceSignatures) {
        this.classInfo = classInfo;
        this.typeParameters = typeParameters;
        this.superclassSignature = superclassSignature;
        this.superinterfaceSignatures = superinterfaceSignatures;
    }
    
    public List<TypeParameter> getTypeParameters() {
        return this.typeParameters;
    }
    
    public ClassRefTypeSignature getSuperclassSignature() {
        return this.superclassSignature;
    }
    
    public List<ClassRefTypeSignature> getSuperinterfaceSignatures() {
        return this.superinterfaceSignatures;
    }
    
    static ClassTypeSignature parse(final String typeDescriptor, final ClassInfo classInfo) throws ParseException {
        final Parser parser = new Parser(typeDescriptor);
        final String definingClassNameNull = null;
        final List<TypeParameter> typeParameters = TypeParameter.parseList(parser, definingClassNameNull);
        final ClassRefTypeSignature superclassSignature = ClassRefTypeSignature.parse(parser, definingClassNameNull);
        List<ClassRefTypeSignature> superinterfaceSignatures;
        if (parser.hasMore()) {
            superinterfaceSignatures = new ArrayList<ClassRefTypeSignature>();
            while (parser.hasMore()) {
                final ClassRefTypeSignature superinterfaceSignature = ClassRefTypeSignature.parse(parser, definingClassNameNull);
                if (superinterfaceSignature == null) {
                    throw new ParseException(parser, "Could not parse superinterface signature");
                }
                superinterfaceSignatures.add(superinterfaceSignature);
            }
        }
        else {
            superinterfaceSignatures = Collections.emptyList();
        }
        if (parser.hasMore()) {
            throw new ParseException(parser, "Extra characters at end of type descriptor");
        }
        return new ClassTypeSignature(classInfo, typeParameters, superclassSignature, superinterfaceSignatures);
    }
    
    @Override
    protected String getClassName() {
        return (this.classInfo != null) ? this.classInfo.getName() : null;
    }
    
    protected ClassInfo getClassInfo() {
        return this.classInfo;
    }
    
    @Override
    void setScanResult(final ScanResult scanResult) {
        super.setScanResult(scanResult);
        if (this.typeParameters != null) {
            for (final TypeParameter typeParameter : this.typeParameters) {
                typeParameter.setScanResult(scanResult);
            }
        }
        if (this.superclassSignature != null) {
            this.superclassSignature.setScanResult(scanResult);
        }
        if (this.superinterfaceSignatures != null) {
            for (final ClassRefTypeSignature classRefTypeSignature : this.superinterfaceSignatures) {
                classRefTypeSignature.setScanResult(scanResult);
            }
        }
    }
    
    protected void findReferencedClassNames(final Set<String> refdClassNames) {
        for (final TypeParameter typeParameter : this.typeParameters) {
            typeParameter.findReferencedClassNames(refdClassNames);
        }
        if (this.superclassSignature != null) {
            this.superclassSignature.findReferencedClassNames(refdClassNames);
        }
        for (final ClassRefTypeSignature typeSignature : this.superinterfaceSignatures) {
            typeSignature.findReferencedClassNames(refdClassNames);
        }
    }
    
    @Override
    protected void findReferencedClassInfo(final Map<String, ClassInfo> classNameToClassInfo, final Set<ClassInfo> refdClassInfo) {
        final Set<String> refdClassNames = new HashSet<String>();
        this.findReferencedClassNames(refdClassNames);
        for (final String refdClassName : refdClassNames) {
            final ClassInfo clsInfo = ClassInfo.getOrCreateClassInfo(refdClassName, classNameToClassInfo);
            clsInfo.scanResult = this.scanResult;
            refdClassInfo.add(clsInfo);
        }
    }
    
    @Override
    public int hashCode() {
        return this.typeParameters.hashCode() + this.superclassSignature.hashCode() * 7 + this.superinterfaceSignatures.hashCode() * 15;
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof ClassTypeSignature)) {
            return false;
        }
        final ClassTypeSignature o = (ClassTypeSignature)obj;
        return o.typeParameters.equals(this.typeParameters) && o.superclassSignature.equals(this.superclassSignature) && o.superinterfaceSignatures.equals(this.superinterfaceSignatures);
    }
    
    String toString(final String className, final boolean typeNameOnly, final int modifiers, final boolean isAnnotation, final boolean isInterface) {
        final StringBuilder buf = new StringBuilder();
        if (!typeNameOnly) {
            if (modifiers != 0) {
                TypeUtils.modifiersToString(modifiers, TypeUtils.ModifierType.CLASS, false, buf);
            }
            if (buf.length() > 0) {
                buf.append(' ');
            }
            buf.append(isAnnotation ? "@interface" : (isInterface ? "interface" : (((modifiers & 0x4000) != 0x0) ? "enum" : "class")));
            buf.append(' ');
        }
        if (className != null) {
            buf.append(className);
        }
        if (!this.typeParameters.isEmpty()) {
            Join.join(buf, "<", ", ", ">", this.typeParameters);
        }
        if (!typeNameOnly) {
            if (this.superclassSignature != null) {
                final String superSig = this.superclassSignature.toString();
                if (!superSig.equals("java.lang.Object")) {
                    buf.append(" extends ");
                    buf.append(superSig);
                }
            }
            if (!this.superinterfaceSignatures.isEmpty()) {
                buf.append(isInterface ? " extends " : " implements ");
                Join.join(buf, "", ", ", "", this.superinterfaceSignatures);
            }
        }
        return buf.toString();
    }
    
    @Override
    public String toString() {
        return this.toString(this.classInfo.getName(), false, this.classInfo.getModifiers(), this.classInfo.isAnnotation(), this.classInfo.isInterface());
    }
}
