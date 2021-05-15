// 
// Decompiled by Procyon v0.5.36
// 

package nonapi.io.github.classgraph.json;

import java.util.List;
import java.util.Map;
import java.util.Arrays;
import java.util.Objects;
import java.lang.reflect.Type;
import java.lang.reflect.ParameterizedType;

class ParameterizedTypeImpl implements ParameterizedType
{
    private final Type[] actualTypeArguments;
    private final Class<?> rawType;
    private final Type ownerType;
    public static final Type MAP_OF_UNKNOWN_TYPE;
    public static final Type LIST_OF_UNKNOWN_TYPE;
    
    ParameterizedTypeImpl(final Class<?> rawType, final Type[] actualTypeArguments, final Type ownerType) {
        this.actualTypeArguments = actualTypeArguments;
        this.rawType = rawType;
        this.ownerType = ((ownerType != null) ? ownerType : rawType.getDeclaringClass());
        if (rawType.getTypeParameters().length != actualTypeArguments.length) {
            throw new IllegalArgumentException("Argument length mismatch");
        }
    }
    
    @Override
    public Type[] getActualTypeArguments() {
        return this.actualTypeArguments.clone();
    }
    
    @Override
    public Class<?> getRawType() {
        return this.rawType;
    }
    
    @Override
    public Type getOwnerType() {
        return this.ownerType;
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof ParameterizedType)) {
            return false;
        }
        final ParameterizedType other = (ParameterizedType)obj;
        return Objects.equals(this.ownerType, other.getOwnerType()) && Objects.equals(this.rawType, other.getRawType()) && Arrays.equals(this.actualTypeArguments, other.getActualTypeArguments());
    }
    
    @Override
    public int hashCode() {
        return Arrays.hashCode(this.actualTypeArguments) ^ Objects.hashCode(this.ownerType) ^ Objects.hashCode(this.rawType);
    }
    
    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder();
        if (this.ownerType == null) {
            buf.append(this.rawType.getName());
        }
        else {
            if (this.ownerType instanceof Class) {
                buf.append(((Class)this.ownerType).getName());
            }
            else {
                buf.append(this.ownerType.toString());
            }
            buf.append('$');
            if (this.ownerType instanceof ParameterizedTypeImpl) {
                final String simpleName = this.rawType.getName().replace(((ParameterizedTypeImpl)this.ownerType).rawType.getName() + "$", "");
                buf.append(simpleName);
            }
            else {
                buf.append(this.rawType.getSimpleName());
            }
        }
        if (this.actualTypeArguments != null && this.actualTypeArguments.length > 0) {
            buf.append('<');
            boolean first = true;
            for (final Type t : this.actualTypeArguments) {
                if (first) {
                    first = false;
                }
                else {
                    buf.append(", ");
                }
                buf.append(t.toString());
            }
            buf.append('>');
        }
        return buf.toString();
    }
    
    static {
        MAP_OF_UNKNOWN_TYPE = new ParameterizedTypeImpl(Map.class, new Type[] { Object.class, Object.class }, null);
        LIST_OF_UNKNOWN_TYPE = new ParameterizedTypeImpl(List.class, new Type[] { Object.class }, null);
    }
}
