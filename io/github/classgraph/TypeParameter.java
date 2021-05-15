// 
// Decompiled by Procyon v0.5.36
// 

package io.github.classgraph;

import java.util.Set;
import java.util.Iterator;
import nonapi.io.github.classgraph.types.TypeUtils;
import nonapi.io.github.classgraph.types.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import nonapi.io.github.classgraph.types.Parser;
import java.util.List;

public final class TypeParameter extends HierarchicalTypeSignature
{
    final String name;
    final ReferenceTypeSignature classBound;
    final List<ReferenceTypeSignature> interfaceBounds;
    
    private TypeParameter(final String identifier, final ReferenceTypeSignature classBound, final List<ReferenceTypeSignature> interfaceBounds) {
        this.name = identifier;
        this.classBound = classBound;
        this.interfaceBounds = interfaceBounds;
    }
    
    public String getName() {
        return this.name;
    }
    
    public ReferenceTypeSignature getClassBound() {
        return this.classBound;
    }
    
    public List<ReferenceTypeSignature> getInterfaceBounds() {
        return this.interfaceBounds;
    }
    
    static List<TypeParameter> parseList(final Parser parser, final String definingClassName) throws ParseException {
        if (parser.peek() != '<') {
            return Collections.emptyList();
        }
        parser.expect('<');
        final List<TypeParameter> typeParams = new ArrayList<TypeParameter>(1);
        while (parser.peek() != '>') {
            if (!parser.hasMore()) {
                throw new ParseException(parser, "Missing '>'");
            }
            if (!TypeUtils.getIdentifierToken(parser)) {
                throw new ParseException(parser, "Could not parse identifier token");
            }
            final String identifier = parser.currToken();
            final ReferenceTypeSignature classBound = ReferenceTypeSignature.parseClassBound(parser, definingClassName);
            List<ReferenceTypeSignature> interfaceBounds;
            if (parser.peek() == ':') {
                interfaceBounds = new ArrayList<ReferenceTypeSignature>();
                while (parser.peek() == ':') {
                    parser.expect(':');
                    final ReferenceTypeSignature interfaceTypeSignature = ReferenceTypeSignature.parseReferenceTypeSignature(parser, definingClassName);
                    if (interfaceTypeSignature == null) {
                        throw new ParseException(parser, "Missing interface type signature");
                    }
                    interfaceBounds.add(interfaceTypeSignature);
                }
            }
            else {
                interfaceBounds = Collections.emptyList();
            }
            typeParams.add(new TypeParameter(identifier, classBound, interfaceBounds));
        }
        parser.expect('>');
        return typeParams;
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
        if (this.classBound != null) {
            this.classBound.setScanResult(scanResult);
        }
        if (this.interfaceBounds != null) {
            for (final ReferenceTypeSignature referenceTypeSignature : this.interfaceBounds) {
                referenceTypeSignature.setScanResult(scanResult);
            }
        }
    }
    
    protected void findReferencedClassNames(final Set<String> refdClassNames) {
        if (this.classBound != null) {
            this.classBound.findReferencedClassNames(refdClassNames);
        }
        for (final ReferenceTypeSignature typeSignature : this.interfaceBounds) {
            typeSignature.findReferencedClassNames(refdClassNames);
        }
    }
    
    @Override
    public int hashCode() {
        return this.name.hashCode() + ((this.classBound == null) ? 0 : (this.classBound.hashCode() * 7)) + this.interfaceBounds.hashCode() * 15;
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof TypeParameter)) {
            return false;
        }
        final TypeParameter o = (TypeParameter)obj;
        return o.name.equals(this.name) && ((o.classBound == null && this.classBound == null) || (o.classBound != null && o.classBound.equals(this.classBound))) && o.interfaceBounds.equals(this.interfaceBounds);
    }
    
    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder();
        buf.append(this.name);
        String classBoundStr;
        if (this.classBound == null) {
            classBoundStr = null;
        }
        else {
            classBoundStr = this.classBound.toString();
            if (classBoundStr.equals("java.lang.Object")) {
                classBoundStr = null;
            }
        }
        if (classBoundStr != null || !this.interfaceBounds.isEmpty()) {
            buf.append(" extends");
        }
        if (classBoundStr != null) {
            buf.append(' ');
            buf.append(classBoundStr);
        }
        for (int i = 0; i < this.interfaceBounds.size(); ++i) {
            if (i > 0 || classBoundStr != null) {
                buf.append(" &");
            }
            buf.append(' ');
            buf.append(this.interfaceBounds.get(i).toString());
        }
        return buf.toString();
    }
}
