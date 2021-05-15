// 
// Decompiled by Procyon v0.5.36
// 

package io.github.classgraph;

import java.lang.reflect.Array;
import java.util.Set;
import nonapi.io.github.classgraph.types.ParseException;
import nonapi.io.github.classgraph.types.Parser;

public class ArrayTypeSignature extends ReferenceTypeSignature
{
    private final TypeSignature elementTypeSignature;
    private final int numDims;
    private final String typeSignatureStr;
    private String className;
    private ArrayClassInfo arrayClassInfo;
    private Class<?> elementClassRef;
    
    ArrayTypeSignature(final TypeSignature elementTypeSignature, final int numDims, final String typeSignatureStr) {
        this.elementTypeSignature = elementTypeSignature;
        this.numDims = numDims;
        this.typeSignatureStr = typeSignatureStr;
    }
    
    ArrayTypeSignature(final String eltClassName, final int numDims) {
        final BaseTypeSignature baseTypeSignature = BaseTypeSignature.getTypeSignature(eltClassName);
        String eltTypeSigStr;
        if (baseTypeSignature != null) {
            eltTypeSigStr = baseTypeSignature.getTypeSignatureChar();
            this.elementTypeSignature = baseTypeSignature;
        }
        else {
            eltTypeSigStr = "L" + eltClassName.replace('.', '/') + ";";
            try {
                this.elementTypeSignature = ClassRefTypeSignature.parse(new Parser(eltTypeSigStr), null);
                if (this.elementTypeSignature == null) {
                    throw new IllegalArgumentException("Could not form array base type signature for class " + eltClassName);
                }
            }
            catch (ParseException e) {
                throw new IllegalArgumentException("Could not form array base type signature for class " + eltClassName);
            }
        }
        final StringBuilder buf = new StringBuilder(numDims + eltTypeSigStr.length());
        for (int i = 0; i < numDims; ++i) {
            buf.append('[');
        }
        buf.append(eltTypeSigStr);
        this.typeSignatureStr = buf.toString();
        this.numDims = numDims;
    }
    
    public String getTypeSignatureStr() {
        return this.typeSignatureStr;
    }
    
    public TypeSignature getElementTypeSignature() {
        return this.elementTypeSignature;
    }
    
    public int getNumDimensions() {
        return this.numDims;
    }
    
    @Override
    protected String getClassName() {
        if (this.className == null) {
            this.className = this.toStringInternal(false);
        }
        return this.className;
    }
    
    protected ClassInfo getClassInfo() {
        return this.getArrayClassInfo();
    }
    
    public ArrayClassInfo getArrayClassInfo() {
        if (this.arrayClassInfo == null) {
            if (this.scanResult != null) {
                final String clsName = this.getClassName();
                this.arrayClassInfo = this.scanResult.classNameToClassInfo.get(clsName);
                if (this.arrayClassInfo == null) {
                    this.scanResult.classNameToClassInfo.put(clsName, this.arrayClassInfo = new ArrayClassInfo(this));
                    this.arrayClassInfo.setScanResult(this.scanResult);
                }
            }
            else {
                this.arrayClassInfo = new ArrayClassInfo(this);
            }
        }
        return this.arrayClassInfo;
    }
    
    @Override
    void setScanResult(final ScanResult scanResult) {
        super.setScanResult(scanResult);
        if (this.elementTypeSignature != null) {
            this.elementTypeSignature.setScanResult(scanResult);
        }
        if (this.arrayClassInfo != null) {
            this.arrayClassInfo.setScanResult(scanResult);
        }
    }
    
    @Override
    protected void findReferencedClassNames(final Set<String> refdClassNames) {
        this.elementTypeSignature.findReferencedClassNames(refdClassNames);
    }
    
    public Class<?> loadElementClass(final boolean ignoreExceptions) {
        if (this.elementClassRef == null) {
            if (this.elementTypeSignature instanceof BaseTypeSignature) {
                this.elementClassRef = ((BaseTypeSignature)this.elementTypeSignature).getType();
            }
            else if (this.scanResult != null) {
                this.elementClassRef = this.elementTypeSignature.loadClass(ignoreExceptions);
            }
            else {
                final String elementTypeName = ((ClassRefTypeSignature)this.elementTypeSignature).getFullyQualifiedClassName();
                try {
                    this.elementClassRef = Class.forName(elementTypeName);
                }
                catch (Throwable t) {
                    if (!ignoreExceptions) {
                        throw new IllegalArgumentException("Could not load array element class " + elementTypeName, t);
                    }
                }
            }
        }
        return this.elementClassRef;
    }
    
    public Class<?> loadElementClass() {
        return this.loadElementClass(false);
    }
    
    public Class<?> loadClass(final boolean ignoreExceptions) {
        if (this.classRef == null) {
            Class<?> eltClassRef = null;
            Label_0029: {
                if (ignoreExceptions) {
                    try {
                        eltClassRef = this.loadElementClass();
                        break Label_0029;
                    }
                    catch (IllegalArgumentException e) {
                        return null;
                    }
                }
                eltClassRef = this.loadElementClass();
            }
            if (eltClassRef == null) {
                throw new IllegalArgumentException("Could not load array element class " + this.elementTypeSignature);
            }
            final Object eltArrayInstance = Array.newInstance(eltClassRef, new int[this.numDims]);
            this.classRef = eltArrayInstance.getClass();
        }
        return this.classRef;
    }
    
    public Class<?> loadClass() {
        return this.loadClass(false);
    }
    
    @Override
    public int hashCode() {
        return this.elementTypeSignature.hashCode() + this.numDims * 15;
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof ArrayTypeSignature)) {
            return false;
        }
        final ArrayTypeSignature other = (ArrayTypeSignature)obj;
        return other.elementTypeSignature.equals(this.elementTypeSignature) && other.numDims == this.numDims;
    }
    
    @Override
    public boolean equalsIgnoringTypeParams(final TypeSignature other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ArrayTypeSignature)) {
            return false;
        }
        final ArrayTypeSignature o = (ArrayTypeSignature)other;
        return o.elementTypeSignature.equalsIgnoringTypeParams(this.elementTypeSignature) && o.numDims == this.numDims;
    }
    
    @Override
    protected String toStringInternal(final boolean useSimpleNames) {
        final StringBuilder buf = new StringBuilder();
        buf.append(useSimpleNames ? this.elementTypeSignature.toStringWithSimpleNames() : this.elementTypeSignature.toString());
        for (int i = 0; i < this.numDims; ++i) {
            buf.append("[]");
        }
        return buf.toString();
    }
    
    static ArrayTypeSignature parse(final Parser parser, final String definingClassName) throws ParseException {
        int numArrayDims = 0;
        final int begin = parser.getPosition();
        while (parser.peek() == '[') {
            ++numArrayDims;
            parser.next();
        }
        if (numArrayDims <= 0) {
            return null;
        }
        final TypeSignature elementTypeSignature = TypeSignature.parse(parser, definingClassName);
        if (elementTypeSignature == null) {
            throw new ParseException(parser, "elementTypeSignature == null");
        }
        final String typeSignatureStr = parser.getSubsequence(begin, parser.getPosition()).toString();
        return new ArrayTypeSignature(elementTypeSignature, numArrayDims, typeSignatureStr);
    }
}
