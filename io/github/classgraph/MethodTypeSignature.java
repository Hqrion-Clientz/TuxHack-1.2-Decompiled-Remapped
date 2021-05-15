// 
// Decompiled by Procyon v0.5.36
// 

package io.github.classgraph;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;
import nonapi.io.github.classgraph.types.ParseException;
import java.util.ArrayList;
import nonapi.io.github.classgraph.types.Parser;
import java.util.Collections;
import java.util.List;

public final class MethodTypeSignature extends HierarchicalTypeSignature
{
    final List<TypeParameter> typeParameters;
    private final List<TypeSignature> parameterTypeSignatures;
    private final TypeSignature resultType;
    private final List<ClassRefOrTypeVariableSignature> throwsSignatures;
    
    private MethodTypeSignature(final List<TypeParameter> typeParameters, final List<TypeSignature> paramTypes, final TypeSignature resultType, final List<ClassRefOrTypeVariableSignature> throwsSignatures) {
        this.typeParameters = typeParameters;
        this.parameterTypeSignatures = paramTypes;
        this.resultType = resultType;
        this.throwsSignatures = throwsSignatures;
    }
    
    List<TypeParameter> getTypeParameters() {
        return this.typeParameters;
    }
    
    List<TypeSignature> getParameterTypeSignatures() {
        return this.parameterTypeSignatures;
    }
    
    public TypeSignature getResultType() {
        return this.resultType;
    }
    
    public List<ClassRefOrTypeVariableSignature> getThrowsSignatures() {
        return this.throwsSignatures;
    }
    
    static MethodTypeSignature parse(final String typeDescriptor, final String definingClassName) throws ParseException {
        if (typeDescriptor.equals("<init>")) {
            return new MethodTypeSignature(Collections.emptyList(), Collections.emptyList(), BaseTypeSignature.VOID, Collections.emptyList());
        }
        final Parser parser = new Parser(typeDescriptor);
        final List<TypeParameter> typeParameters = TypeParameter.parseList(parser, definingClassName);
        parser.expect('(');
        final List<TypeSignature> paramTypes = new ArrayList<TypeSignature>();
        while (parser.peek() != ')') {
            if (!parser.hasMore()) {
                throw new ParseException(parser, "Ran out of input while parsing method signature");
            }
            final TypeSignature paramType = TypeSignature.parse(parser, definingClassName);
            if (paramType == null) {
                throw new ParseException(parser, "Missing method parameter type signature");
            }
            paramTypes.add(paramType);
        }
        parser.expect(')');
        final TypeSignature resultType = TypeSignature.parse(parser, definingClassName);
        if (resultType == null) {
            throw new ParseException(parser, "Missing method result type signature");
        }
        List<ClassRefOrTypeVariableSignature> throwsSignatures;
        if (parser.peek() == '^') {
            throwsSignatures = new ArrayList<ClassRefOrTypeVariableSignature>();
            while (parser.peek() == '^') {
                parser.expect('^');
                final ClassRefTypeSignature classTypeSignature = ClassRefTypeSignature.parse(parser, definingClassName);
                if (classTypeSignature != null) {
                    throwsSignatures.add(classTypeSignature);
                }
                else {
                    final TypeVariableSignature typeVariableSignature = TypeVariableSignature.parse(parser, definingClassName);
                    if (typeVariableSignature == null) {
                        throw new ParseException(parser, "Missing type variable signature");
                    }
                    throwsSignatures.add(typeVariableSignature);
                }
            }
        }
        else {
            throwsSignatures = Collections.emptyList();
        }
        if (parser.hasMore()) {
            throw new ParseException(parser, "Extra characters at end of type descriptor");
        }
        final MethodTypeSignature methodSignature = new MethodTypeSignature(typeParameters, paramTypes, resultType, throwsSignatures);
        final List<TypeVariableSignature> typeVariableSignatures = (List<TypeVariableSignature>)parser.getState();
        if (typeVariableSignatures != null) {
            for (final TypeVariableSignature typeVariableSignature2 : typeVariableSignatures) {
                typeVariableSignature2.containingMethodSignature = methodSignature;
            }
        }
        return methodSignature;
    }
    
    @Override
    protected String getClassName() {
        throw new IllegalArgumentException("getClassName() cannot be called here");
    }
    
    protected ClassInfo getClassInfo() {
        throw new IllegalArgumentException("getClassInfo() cannot be called here");
    }
    
    @Override
    void setScanResult(final ScanResult scanResult) {
        super.setScanResult(scanResult);
        if (this.typeParameters != null) {
            for (final TypeParameter typeParameter : this.typeParameters) {
                typeParameter.setScanResult(scanResult);
            }
        }
        if (this.parameterTypeSignatures != null) {
            for (final TypeSignature typeParameter2 : this.parameterTypeSignatures) {
                typeParameter2.setScanResult(scanResult);
            }
        }
        if (this.resultType != null) {
            this.resultType.setScanResult(scanResult);
        }
        if (this.throwsSignatures != null) {
            for (final ClassRefOrTypeVariableSignature throwsSignature : this.throwsSignatures) {
                throwsSignature.setScanResult(scanResult);
            }
        }
    }
    
    protected void findReferencedClassNames(final Set<String> refdClassNames) {
        for (final TypeParameter typeParameter : this.typeParameters) {
            if (typeParameter != null) {
                typeParameter.findReferencedClassNames(refdClassNames);
            }
        }
        for (final TypeSignature typeSignature : this.parameterTypeSignatures) {
            if (typeSignature != null) {
                typeSignature.findReferencedClassNames(refdClassNames);
            }
        }
        this.resultType.findReferencedClassNames(refdClassNames);
        for (final ClassRefOrTypeVariableSignature typeSignature2 : this.throwsSignatures) {
            if (typeSignature2 != null) {
                typeSignature2.findReferencedClassNames(refdClassNames);
            }
        }
    }
    
    @Override
    protected void findReferencedClassInfo(final Map<String, ClassInfo> classNameToClassInfo, final Set<ClassInfo> refdClassInfo) {
        final Set<String> refdClassNames = new HashSet<String>();
        this.findReferencedClassNames(refdClassNames);
        for (final String refdClassName : refdClassNames) {
            final ClassInfo classInfo = ClassInfo.getOrCreateClassInfo(refdClassName, classNameToClassInfo);
            classInfo.scanResult = this.scanResult;
            refdClassInfo.add(classInfo);
        }
    }
    
    @Override
    public int hashCode() {
        return this.typeParameters.hashCode() + this.parameterTypeSignatures.hashCode() * 7 + this.resultType.hashCode() * 15 + this.throwsSignatures.hashCode() * 31;
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof MethodTypeSignature)) {
            return false;
        }
        final MethodTypeSignature o = (MethodTypeSignature)obj;
        return o.typeParameters.equals(this.typeParameters) && o.parameterTypeSignatures.equals(this.parameterTypeSignatures) && o.resultType.equals(this.resultType) && o.throwsSignatures.equals(this.throwsSignatures);
    }
    
    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder();
        if (!this.typeParameters.isEmpty()) {
            buf.append('<');
            for (int i = 0; i < this.typeParameters.size(); ++i) {
                if (i > 0) {
                    buf.append(", ");
                }
                final String typeParamStr = this.typeParameters.get(i).toString();
                buf.append(typeParamStr);
            }
            buf.append('>');
        }
        if (buf.length() > 0) {
            buf.append(' ');
        }
        buf.append(this.resultType.toString());
        buf.append(" (");
        for (int i = 0; i < this.parameterTypeSignatures.size(); ++i) {
            if (i > 0) {
                buf.append(", ");
            }
            buf.append(this.parameterTypeSignatures.get(i).toString());
        }
        buf.append(')');
        if (!this.throwsSignatures.isEmpty()) {
            buf.append(" throws ");
            for (int i = 0; i < this.throwsSignatures.size(); ++i) {
                if (i > 0) {
                    buf.append(", ");
                }
                buf.append(this.throwsSignatures.get(i).toString());
            }
        }
        return buf.toString();
    }
}
