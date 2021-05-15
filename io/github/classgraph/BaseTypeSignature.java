// 
// Decompiled by Procyon v0.5.36
// 

package io.github.classgraph;

import java.util.Set;
import nonapi.io.github.classgraph.types.Parser;

public class BaseTypeSignature extends TypeSignature
{
    private final String baseType;
    private final String typeSignatureChar;
    private static final BaseTypeSignature BYTE;
    private static final BaseTypeSignature CHAR;
    private static final BaseTypeSignature DOUBLE;
    private static final BaseTypeSignature FLOAT;
    private static final BaseTypeSignature INT;
    private static final BaseTypeSignature LONG;
    private static final BaseTypeSignature SHORT;
    private static final BaseTypeSignature BOOLEAN;
    static final BaseTypeSignature VOID;
    
    private BaseTypeSignature(final String baseType, final char typeSignatureChar) {
        this.baseType = baseType;
        this.typeSignatureChar = Character.toString(typeSignatureChar);
    }
    
    public String getTypeStr() {
        return this.baseType;
    }
    
    public String getTypeSignatureChar() {
        return this.typeSignatureChar;
    }
    
    public Class<?> getType() {
        final String baseType = this.baseType;
        switch (baseType) {
            case "byte": {
                return Byte.TYPE;
            }
            case "char": {
                return Character.TYPE;
            }
            case "double": {
                return Double.TYPE;
            }
            case "float": {
                return Float.TYPE;
            }
            case "int": {
                return Integer.TYPE;
            }
            case "long": {
                return Long.TYPE;
            }
            case "short": {
                return Short.TYPE;
            }
            case "boolean": {
                return Boolean.TYPE;
            }
            case "void": {
                return Void.TYPE;
            }
            default: {
                throw new IllegalArgumentException("Unknown base type " + this.baseType);
            }
        }
    }
    
    public static BaseTypeSignature getTypeSignature(final String typeName) {
        switch (typeName) {
            case "byte": {
                return BaseTypeSignature.BYTE;
            }
            case "char": {
                return BaseTypeSignature.CHAR;
            }
            case "double": {
                return BaseTypeSignature.DOUBLE;
            }
            case "float": {
                return BaseTypeSignature.FLOAT;
            }
            case "int": {
                return BaseTypeSignature.INT;
            }
            case "long": {
                return BaseTypeSignature.LONG;
            }
            case "short": {
                return BaseTypeSignature.SHORT;
            }
            case "boolean": {
                return BaseTypeSignature.BOOLEAN;
            }
            case "void": {
                return BaseTypeSignature.VOID;
            }
            default: {
                return null;
            }
        }
    }
    
    @Override
    Class<?> loadClass() {
        return this.getType();
    }
    
    @Override
     <T> Class<T> loadClass(final Class<T> superclassOrInterfaceType) {
        final Class<?> type = this.getType();
        if (!superclassOrInterfaceType.isAssignableFrom(type)) {
            throw new IllegalArgumentException("Primitive class " + this.baseType + " cannot be cast to " + superclassOrInterfaceType.getName());
        }
        final Class<T> classT = (Class<T>)type;
        return classT;
    }
    
    static BaseTypeSignature parse(final Parser parser) {
        switch (parser.peek()) {
            case 'B': {
                parser.next();
                return BaseTypeSignature.BYTE;
            }
            case 'C': {
                parser.next();
                return BaseTypeSignature.CHAR;
            }
            case 'D': {
                parser.next();
                return BaseTypeSignature.DOUBLE;
            }
            case 'F': {
                parser.next();
                return BaseTypeSignature.FLOAT;
            }
            case 'I': {
                parser.next();
                return BaseTypeSignature.INT;
            }
            case 'J': {
                parser.next();
                return BaseTypeSignature.LONG;
            }
            case 'S': {
                parser.next();
                return BaseTypeSignature.SHORT;
            }
            case 'Z': {
                parser.next();
                return BaseTypeSignature.BOOLEAN;
            }
            case 'V': {
                parser.next();
                return BaseTypeSignature.VOID;
            }
            default: {
                return null;
            }
        }
    }
    
    @Override
    protected String getClassName() {
        return this.baseType;
    }
    
    protected ClassInfo getClassInfo() {
        return null;
    }
    
    @Override
    protected void findReferencedClassNames(final Set<String> refdClassNames) {
    }
    
    @Override
    void setScanResult(final ScanResult scanResult) {
    }
    
    @Override
    public int hashCode() {
        return this.baseType.hashCode();
    }
    
    @Override
    public boolean equals(final Object obj) {
        return obj == this || (obj instanceof BaseTypeSignature && ((BaseTypeSignature)obj).baseType.equals(this.baseType));
    }
    
    @Override
    public boolean equalsIgnoringTypeParams(final TypeSignature other) {
        return other instanceof BaseTypeSignature && this.baseType.equals(((BaseTypeSignature)other).baseType);
    }
    
    @Override
    protected String toStringInternal(final boolean useSimpleNames) {
        return this.baseType;
    }
    
    static {
        BYTE = new BaseTypeSignature("byte", 'B');
        CHAR = new BaseTypeSignature("char", 'C');
        DOUBLE = new BaseTypeSignature("double", 'D');
        FLOAT = new BaseTypeSignature("float", 'F');
        INT = new BaseTypeSignature("int", 'I');
        LONG = new BaseTypeSignature("long", 'J');
        SHORT = new BaseTypeSignature("short", 'S');
        BOOLEAN = new BaseTypeSignature("boolean", 'Z');
        VOID = new BaseTypeSignature("void", 'V');
    }
}
