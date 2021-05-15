// 
// Decompiled by Procyon v0.5.36
// 

package io.github.classgraph;

import java.util.Collections;
import java.util.ArrayList;
import nonapi.io.github.classgraph.types.ParseException;
import nonapi.io.github.classgraph.types.TypeUtils;
import nonapi.io.github.classgraph.types.Parser;
import java.util.Set;
import java.util.Iterator;
import java.util.List;

public final class ClassRefTypeSignature extends ClassRefOrTypeVariableSignature
{
    final String className;
    private String fullyQualifiedClassName;
    private final List<TypeArgument> typeArguments;
    private final List<String> suffixes;
    private final List<List<TypeArgument>> suffixTypeArguments;
    
    private ClassRefTypeSignature(final String className, final List<TypeArgument> typeArguments, final List<String> suffixes, final List<List<TypeArgument>> suffixTypeArguments) {
        this.className = className;
        this.typeArguments = typeArguments;
        this.suffixes = suffixes;
        this.suffixTypeArguments = suffixTypeArguments;
    }
    
    public String getBaseClassName() {
        return this.className;
    }
    
    public String getFullyQualifiedClassName() {
        if (this.fullyQualifiedClassName == null) {
            final StringBuilder buf = new StringBuilder();
            buf.append(this.className);
            for (final String suffix : this.suffixes) {
                buf.append('$');
                buf.append(suffix);
            }
            this.fullyQualifiedClassName = buf.toString();
        }
        return this.fullyQualifiedClassName;
    }
    
    public List<TypeArgument> getTypeArguments() {
        return this.typeArguments;
    }
    
    public List<String> getSuffixes() {
        return this.suffixes;
    }
    
    public List<List<TypeArgument>> getSuffixTypeArguments() {
        return this.suffixTypeArguments;
    }
    
    public Class<?> loadClass(final boolean ignoreExceptions) {
        return super.loadClass(ignoreExceptions);
    }
    
    public Class<?> loadClass() {
        return super.loadClass();
    }
    
    @Override
    protected String getClassName() {
        return this.getFullyQualifiedClassName();
    }
    
    public ClassInfo getClassInfo() {
        return super.getClassInfo();
    }
    
    @Override
    void setScanResult(final ScanResult scanResult) {
        super.setScanResult(scanResult);
        if (this.typeArguments != null) {
            for (final TypeArgument typeArgument : this.typeArguments) {
                typeArgument.setScanResult(scanResult);
            }
        }
        if (this.suffixTypeArguments != null) {
            for (final List<TypeArgument> list : this.suffixTypeArguments) {
                for (final TypeArgument typeArgument2 : list) {
                    typeArgument2.setScanResult(scanResult);
                }
            }
        }
    }
    
    @Override
    protected void findReferencedClassNames(final Set<String> refdClassNames) {
        refdClassNames.add(this.getFullyQualifiedClassName());
        for (final TypeArgument typeArgument : this.typeArguments) {
            typeArgument.findReferencedClassNames(refdClassNames);
        }
    }
    
    @Override
    public int hashCode() {
        return this.className.hashCode() + 7 * this.typeArguments.hashCode() + 15 * this.suffixes.hashCode();
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof ClassRefTypeSignature)) {
            return false;
        }
        final ClassRefTypeSignature o = (ClassRefTypeSignature)obj;
        return o.className.equals(this.className) && o.typeArguments.equals(this.typeArguments) && o.suffixes.equals(this.suffixes);
    }
    
    @Override
    public boolean equalsIgnoringTypeParams(final TypeSignature other) {
        if (other instanceof TypeVariableSignature) {
            return other.equalsIgnoringTypeParams(this);
        }
        if (!(other instanceof ClassRefTypeSignature)) {
            return false;
        }
        final ClassRefTypeSignature o = (ClassRefTypeSignature)other;
        if (o.suffixes.equals(this.suffixes)) {
            return o.className.equals(this.className);
        }
        return o.getFullyQualifiedClassName().equals(this.getFullyQualifiedClassName());
    }
    
    @Override
    protected String toStringInternal(final boolean useSimpleNames) {
        final StringBuilder buf = new StringBuilder();
        if (!useSimpleNames || this.suffixes.isEmpty()) {
            buf.append(useSimpleNames ? ClassInfo.getSimpleName(this.className) : this.className);
            if (!this.typeArguments.isEmpty()) {
                buf.append('<');
                for (int i = 0; i < this.typeArguments.size(); ++i) {
                    if (i > 0) {
                        buf.append(", ");
                    }
                    buf.append(useSimpleNames ? this.typeArguments.get(i).toStringWithSimpleNames() : this.typeArguments.get(i).toString());
                }
                buf.append('>');
            }
        }
        for (int i = (useSimpleNames && !this.suffixes.isEmpty()) ? (this.suffixes.size() - 1) : 0; i < this.suffixes.size(); ++i) {
            if (!useSimpleNames) {
                buf.append('.');
            }
            buf.append(this.suffixes.get(i));
            final List<TypeArgument> suffixTypeArgs = this.suffixTypeArguments.get(i);
            if (!suffixTypeArgs.isEmpty()) {
                buf.append('<');
                for (int j = 0; j < suffixTypeArgs.size(); ++j) {
                    if (j > 0) {
                        buf.append(", ");
                    }
                    buf.append(useSimpleNames ? suffixTypeArgs.get(j).toStringWithSimpleNames() : suffixTypeArgs.get(j).toString());
                }
                buf.append('>');
            }
        }
        return buf.toString();
    }
    
    static ClassRefTypeSignature parse(final Parser parser, final String definingClassName) throws ParseException {
        if (parser.peek() != 'L') {
            return null;
        }
        parser.next();
        if (!TypeUtils.getIdentifierToken(parser, '/', '.')) {
            throw new ParseException(parser, "Could not parse identifier token");
        }
        final String className = parser.currToken();
        final List<TypeArgument> typeArguments = TypeArgument.parseList(parser, definingClassName);
        List<String> suffixes;
        List<List<TypeArgument>> suffixTypeArguments;
        if (parser.peek() == '.') {
            suffixes = new ArrayList<String>();
            suffixTypeArguments = new ArrayList<List<TypeArgument>>();
            while (parser.peek() == '.') {
                parser.expect('.');
                if (!TypeUtils.getIdentifierToken(parser, '/', '.')) {
                    throw new ParseException(parser, "Could not parse identifier token");
                }
                suffixes.add(parser.currToken());
                suffixTypeArguments.add(TypeArgument.parseList(parser, definingClassName));
            }
        }
        else {
            suffixes = Collections.emptyList();
            suffixTypeArguments = Collections.emptyList();
        }
        parser.expect(';');
        return new ClassRefTypeSignature(className, typeArguments, suffixes, suffixTypeArguments);
    }
}
