// 
// Decompiled by Procyon v0.5.36
// 

package nonapi.io.github.classgraph.json;

import java.util.Map;
import java.util.Collection;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.lang.reflect.Field;

class FieldTypeInfo
{
    final Field field;
    private final Type fieldTypePartiallyResolved;
    private final boolean hasUnresolvedTypeVariables;
    private final boolean isTypeVariable;
    private final PrimitiveType primitiveType;
    private Constructor<?> constructorForFieldTypeWithSizeHint;
    private Constructor<?> defaultConstructorForFieldType;
    
    private static boolean hasTypeVariables(final Type type) {
        if (type instanceof TypeVariable || type instanceof GenericArrayType) {
            return true;
        }
        if (type instanceof ParameterizedType) {
            for (final Type arg : ((ParameterizedType)type).getActualTypeArguments()) {
                if (hasTypeVariables(arg)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public FieldTypeInfo(final Field field, final Type fieldTypePartiallyResolved, final ClassFieldCache classFieldCache) {
        this.field = field;
        this.fieldTypePartiallyResolved = fieldTypePartiallyResolved;
        this.isTypeVariable = (fieldTypePartiallyResolved instanceof TypeVariable);
        this.hasUnresolvedTypeVariables = (this.isTypeVariable || hasTypeVariables(fieldTypePartiallyResolved));
        final boolean isArray = fieldTypePartiallyResolved instanceof GenericArrayType || (fieldTypePartiallyResolved instanceof Class && ((Class)fieldTypePartiallyResolved).isArray());
        if (isArray || this.isTypeVariable) {
            this.primitiveType = PrimitiveType.NON_PRIMITIVE;
        }
        else {
            final Class<?> fieldRawType = JSONUtils.getRawType(fieldTypePartiallyResolved);
            if (fieldRawType == Integer.TYPE) {
                this.primitiveType = PrimitiveType.INTEGER;
            }
            else if (fieldRawType == Long.TYPE) {
                this.primitiveType = PrimitiveType.LONG;
            }
            else if (fieldRawType == Short.TYPE) {
                this.primitiveType = PrimitiveType.SHORT;
            }
            else if (fieldRawType == Double.TYPE) {
                this.primitiveType = PrimitiveType.DOUBLE;
            }
            else if (fieldRawType == Float.TYPE) {
                this.primitiveType = PrimitiveType.FLOAT;
            }
            else if (fieldRawType == Boolean.TYPE) {
                this.primitiveType = PrimitiveType.BOOLEAN;
            }
            else if (fieldRawType == Byte.TYPE) {
                this.primitiveType = PrimitiveType.BYTE;
            }
            else if (fieldRawType == Character.TYPE) {
                this.primitiveType = PrimitiveType.CHARACTER;
            }
            else if (fieldRawType == Class.class) {
                this.primitiveType = PrimitiveType.CLASS_REF;
            }
            else {
                this.primitiveType = PrimitiveType.NON_PRIMITIVE;
            }
            if (!JSONUtils.isBasicValueType(fieldRawType)) {
                if (Collection.class.isAssignableFrom(fieldRawType) || Map.class.isAssignableFrom(fieldRawType)) {
                    this.constructorForFieldTypeWithSizeHint = classFieldCache.getConstructorWithSizeHintForConcreteTypeOf(fieldRawType);
                }
                if (this.constructorForFieldTypeWithSizeHint == null) {
                    this.defaultConstructorForFieldType = classFieldCache.getDefaultConstructorForConcreteTypeOf(fieldRawType);
                }
            }
        }
    }
    
    public Constructor<?> getConstructorForFieldTypeWithSizeHint(final Type fieldTypeFullyResolved, final ClassFieldCache classFieldCache) {
        if (!this.isTypeVariable) {
            return this.constructorForFieldTypeWithSizeHint;
        }
        final Class<?> fieldRawTypeFullyResolved = JSONUtils.getRawType(fieldTypeFullyResolved);
        if (!Collection.class.isAssignableFrom(fieldRawTypeFullyResolved) && !Map.class.isAssignableFrom(fieldRawTypeFullyResolved)) {
            return null;
        }
        return classFieldCache.getConstructorWithSizeHintForConcreteTypeOf(fieldRawTypeFullyResolved);
    }
    
    public Constructor<?> getDefaultConstructorForFieldType(final Type fieldTypeFullyResolved, final ClassFieldCache classFieldCache) {
        if (!this.isTypeVariable) {
            return this.defaultConstructorForFieldType;
        }
        final Class<?> fieldRawTypeFullyResolved = JSONUtils.getRawType(fieldTypeFullyResolved);
        return classFieldCache.getDefaultConstructorForConcreteTypeOf(fieldRawTypeFullyResolved);
    }
    
    public Type getFullyResolvedFieldType(final TypeResolutions typeResolutions) {
        if (!this.hasUnresolvedTypeVariables) {
            return this.fieldTypePartiallyResolved;
        }
        return typeResolutions.resolveTypeVariables(this.fieldTypePartiallyResolved);
    }
    
    void setFieldValue(final Object containingObj, final Object value) {
        try {
            if (value == null) {
                if (this.primitiveType != PrimitiveType.NON_PRIMITIVE) {
                    throw new IllegalArgumentException("Tried to set primitive-typed field " + this.field.getDeclaringClass().getName() + "." + this.field.getName() + " to null value");
                }
                this.field.set(containingObj, null);
            }
            else {
                switch (this.primitiveType) {
                    case NON_PRIMITIVE: {
                        this.field.set(containingObj, value);
                        break;
                    }
                    case CLASS_REF: {
                        if (!(value instanceof Class)) {
                            throw new IllegalArgumentException("Expected value of type Class<?>; got " + value.getClass().getName());
                        }
                        this.field.set(containingObj, value);
                        break;
                    }
                    case INTEGER: {
                        if (!(value instanceof Integer)) {
                            throw new IllegalArgumentException("Expected value of type Integer; got " + value.getClass().getName());
                        }
                        this.field.setInt(containingObj, (int)value);
                        break;
                    }
                    case LONG: {
                        if (!(value instanceof Long)) {
                            throw new IllegalArgumentException("Expected value of type Long; got " + value.getClass().getName());
                        }
                        this.field.setLong(containingObj, (long)value);
                        break;
                    }
                    case SHORT: {
                        if (!(value instanceof Short)) {
                            throw new IllegalArgumentException("Expected value of type Short; got " + value.getClass().getName());
                        }
                        this.field.setShort(containingObj, (short)value);
                        break;
                    }
                    case DOUBLE: {
                        if (!(value instanceof Double)) {
                            throw new IllegalArgumentException("Expected value of type Double; got " + value.getClass().getName());
                        }
                        this.field.setDouble(containingObj, (double)value);
                        break;
                    }
                    case FLOAT: {
                        if (!(value instanceof Float)) {
                            throw new IllegalArgumentException("Expected value of type Float; got " + value.getClass().getName());
                        }
                        this.field.setFloat(containingObj, (float)value);
                        break;
                    }
                    case BOOLEAN: {
                        if (!(value instanceof Boolean)) {
                            throw new IllegalArgumentException("Expected value of type Boolean; got " + value.getClass().getName());
                        }
                        this.field.setBoolean(containingObj, (boolean)value);
                        break;
                    }
                    case BYTE: {
                        if (!(value instanceof Byte)) {
                            throw new IllegalArgumentException("Expected value of type Byte; got " + value.getClass().getName());
                        }
                        this.field.setByte(containingObj, (byte)value);
                        break;
                    }
                    case CHARACTER: {
                        if (!(value instanceof Character)) {
                            throw new IllegalArgumentException("Expected value of type Character; got " + value.getClass().getName());
                        }
                        this.field.setChar(containingObj, (char)value);
                        break;
                    }
                    default: {
                        throw new IllegalArgumentException();
                    }
                }
            }
        }
        catch (IllegalArgumentException | IllegalAccessException ex2) {
            final Exception ex;
            final Exception e = ex;
            throw new IllegalArgumentException("Could not set field " + this.field.getDeclaringClass().getName() + "." + this.field.getName(), e);
        }
    }
    
    @Override
    public String toString() {
        return this.fieldTypePartiallyResolved + " " + this.field.getDeclaringClass().getName() + "." + this.field.getDeclaringClass().getName();
    }
    
    private enum PrimitiveType
    {
        NON_PRIMITIVE, 
        INTEGER, 
        LONG, 
        SHORT, 
        DOUBLE, 
        FLOAT, 
        BOOLEAN, 
        BYTE, 
        CHARACTER, 
        CLASS_REF;
    }
}
