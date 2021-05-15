// 
// Decompiled by Procyon v0.5.36
// 

package io.github.classgraph;

import java.util.Set;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import nonapi.io.github.classgraph.types.ParseException;
import nonapi.io.github.classgraph.types.Parser;

public final class TypeArgument extends HierarchicalTypeSignature
{
    private final Wildcard wildcard;
    private final ReferenceTypeSignature typeSignature;
    
    private TypeArgument(final Wildcard wildcard, final ReferenceTypeSignature typeSignature) {
        this.wildcard = wildcard;
        this.typeSignature = typeSignature;
    }
    
    public Wildcard getWildcard() {
        return this.wildcard;
    }
    
    public ReferenceTypeSignature getTypeSignature() {
        return this.typeSignature;
    }
    
    private static TypeArgument parse(final Parser parser, final String definingClassName) throws ParseException {
        final char peek = parser.peek();
        if (peek == '*') {
            parser.expect('*');
            return new TypeArgument(Wildcard.ANY, null);
        }
        if (peek == '+') {
            parser.expect('+');
            final ReferenceTypeSignature typeSignature = ReferenceTypeSignature.parseReferenceTypeSignature(parser, definingClassName);
            if (typeSignature == null) {
                throw new ParseException(parser, "Missing '+' type bound");
            }
            return new TypeArgument(Wildcard.EXTENDS, typeSignature);
        }
        else if (peek == '-') {
            parser.expect('-');
            final ReferenceTypeSignature typeSignature = ReferenceTypeSignature.parseReferenceTypeSignature(parser, definingClassName);
            if (typeSignature == null) {
                throw new ParseException(parser, "Missing '-' type bound");
            }
            return new TypeArgument(Wildcard.SUPER, typeSignature);
        }
        else {
            final ReferenceTypeSignature typeSignature = ReferenceTypeSignature.parseReferenceTypeSignature(parser, definingClassName);
            if (typeSignature == null) {
                throw new ParseException(parser, "Missing type bound");
            }
            return new TypeArgument(Wildcard.NONE, typeSignature);
        }
    }
    
    static List<TypeArgument> parseList(final Parser parser, final String definingClassName) throws ParseException {
        if (parser.peek() == '<') {
            parser.expect('<');
            final List<TypeArgument> typeArguments = new ArrayList<TypeArgument>(2);
            while (parser.peek() != '>') {
                if (!parser.hasMore()) {
                    throw new ParseException(parser, "Missing '>'");
                }
                typeArguments.add(parse(parser, definingClassName));
            }
            parser.expect('>');
            return typeArguments;
        }
        return Collections.emptyList();
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
        if (this.typeSignature != null) {
            this.typeSignature.setScanResult(scanResult);
        }
    }
    
    public void findReferencedClassNames(final Set<String> refdClassNames) {
        if (this.typeSignature != null) {
            this.typeSignature.findReferencedClassNames(refdClassNames);
        }
    }
    
    @Override
    public int hashCode() {
        return this.typeSignature.hashCode() + 7 * this.wildcard.hashCode();
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof TypeArgument)) {
            return false;
        }
        final TypeArgument o = (TypeArgument)obj;
        return o.typeSignature.equals(this.typeSignature) && o.wildcard.equals(this.wildcard);
    }
    
    private String toStringInternal(final boolean useSimpleNames) {
        switch (this.wildcard) {
            case ANY: {
                return "?";
            }
            case EXTENDS: {
                final String typeSigStr = this.typeSignature.toString();
                return typeSigStr.equals("java.lang.Object") ? "?" : ("? extends " + typeSigStr);
            }
            case SUPER: {
                return "? super " + this.typeSignature.toString();
            }
            case NONE: {
                return useSimpleNames ? this.typeSignature.toStringWithSimpleNames() : this.typeSignature.toString();
            }
            default: {
                throw ClassGraphException.newClassGraphException("Unknown wildcard type " + this.wildcard);
            }
        }
    }
    
    public String toStringWithSimpleNames() {
        return this.toStringInternal(true);
    }
    
    @Override
    public String toString() {
        return this.toStringInternal(false);
    }
    
    public enum Wildcard
    {
        NONE, 
        ANY, 
        EXTENDS, 
        SUPER;
    }
}
