// 
// Decompiled by Procyon v0.5.36
// 

package nonapi.io.github.classgraph.json;

import io.github.classgraph.ClassGraphException;
import java.lang.reflect.WildcardType;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

class TypeResolutions
{
    private final TypeVariable<?>[] typeVariables;
    Type[] resolvedTypeArguments;
    
    TypeResolutions(final ParameterizedType resolvedType) {
        this.typeVariables = (TypeVariable<?>[])((Class)resolvedType.getRawType()).getTypeParameters();
        this.resolvedTypeArguments = resolvedType.getActualTypeArguments();
        if (this.resolvedTypeArguments.length != this.typeVariables.length) {
            throw new IllegalArgumentException("Type parameter count mismatch");
        }
    }
    
    Type resolveTypeVariables(final Type type) {
        if (type instanceof Class) {
            return type;
        }
        if (type instanceof ParameterizedType) {
            final ParameterizedType parameterizedType = (ParameterizedType)type;
            final Type[] typeArgs = parameterizedType.getActualTypeArguments();
            Type[] typeArgsResolved = null;
            for (int i = 0; i < typeArgs.length; ++i) {
                final Type typeArgResolved = this.resolveTypeVariables(typeArgs[i]);
                if (typeArgsResolved == null) {
                    if (!typeArgResolved.equals(typeArgs[i])) {
                        typeArgsResolved = new Type[typeArgs.length];
                        System.arraycopy(typeArgs, 0, typeArgsResolved, 0, i);
                        typeArgsResolved[i] = typeArgResolved;
                    }
                }
                else {
                    typeArgsResolved[i] = typeArgResolved;
                }
            }
            if (typeArgsResolved == null) {
                return type;
            }
            return new ParameterizedTypeImpl((Class<?>)parameterizedType.getRawType(), typeArgsResolved, parameterizedType.getOwnerType());
        }
        else {
            if (type instanceof TypeVariable) {
                final TypeVariable<?> typeVariable = (TypeVariable<?>)type;
                for (int j = 0; j < this.typeVariables.length; ++j) {
                    if (this.typeVariables[j].getName().equals(typeVariable.getName())) {
                        return this.resolvedTypeArguments[j];
                    }
                }
                return type;
            }
            if (type instanceof GenericArrayType) {
                int numArrayDims = 0;
                Type t;
                for (t = type; t instanceof GenericArrayType; t = ((GenericArrayType)t).getGenericComponentType()) {
                    ++numArrayDims;
                }
                final Type innermostType = t;
                final Type innermostTypeResolved = this.resolveTypeVariables(innermostType);
                if (!(innermostTypeResolved instanceof Class)) {
                    throw new IllegalArgumentException("Could not resolve generic array type " + type);
                }
                final Class<?> innermostTypeResolvedClass = (Class<?>)innermostTypeResolved;
                final int[] dims = (int[])Array.newInstance(Integer.TYPE, numArrayDims);
                final Object arrayInstance = Array.newInstance(innermostTypeResolvedClass, dims);
                return arrayInstance.getClass();
            }
            else {
                if (type instanceof WildcardType) {
                    throw ClassGraphException.newClassGraphException("WildcardType not yet supported: " + type);
                }
                throw ClassGraphException.newClassGraphException("Got unexpected type: " + type);
            }
        }
    }
    
    @Override
    public String toString() {
        if (this.typeVariables.length == 0) {
            return "{ }";
        }
        final StringBuilder buf = new StringBuilder();
        buf.append("{ ");
        for (int i = 0; i < this.typeVariables.length; ++i) {
            if (i > 0) {
                buf.append(", ");
            }
            buf.append(this.typeVariables[i]).append(" => ").append(this.resolvedTypeArguments[i]);
        }
        buf.append(" }");
        return buf.toString();
    }
}
