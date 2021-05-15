// 
// Decompiled by Procyon v0.5.36
// 

package io.github.classgraph;

import java.lang.reflect.Field;

public class AnnotationEnumValue extends ScanResultObject implements Comparable<AnnotationEnumValue>
{
    private String className;
    private String valueName;
    
    AnnotationEnumValue() {
    }
    
    AnnotationEnumValue(final String className, final String constValueName) {
        this.className = className;
        this.valueName = constValueName;
    }
    
    public String getClassName() {
        return this.className;
    }
    
    public String getValueName() {
        return this.valueName;
    }
    
    public String getName() {
        return this.className + "." + this.valueName;
    }
    
    public Object loadClassAndReturnEnumValue(final boolean ignoreExceptions) throws IllegalArgumentException {
        final Class<?> classRef = super.loadClass(ignoreExceptions);
        if (classRef == null) {
            if (ignoreExceptions) {
                return null;
            }
            throw new IllegalArgumentException("Enum class " + this.className + " could not be loaded");
        }
        else {
            if (!classRef.isEnum()) {
                throw new IllegalArgumentException("Class " + this.className + " is not an enum");
            }
            Field field;
            try {
                field = classRef.getDeclaredField(this.valueName);
            }
            catch (ReflectiveOperationException | SecurityException ex3) {
                final Exception ex;
                final Exception e = ex;
                throw new IllegalArgumentException("Could not find enum constant " + this.toString(), e);
            }
            if (!field.isEnumConstant()) {
                throw new IllegalArgumentException("Field " + this.toString() + " is not an enum constant");
            }
            try {
                return field.get(null);
            }
            catch (ReflectiveOperationException | SecurityException ex4) {
                final Exception ex2;
                final Exception e = ex2;
                throw new IllegalArgumentException("Field " + this.toString() + " is not accessible", e);
            }
        }
    }
    
    public Object loadClassAndReturnEnumValue() throws IllegalArgumentException {
        return this.loadClassAndReturnEnumValue(false);
    }
    
    @Override
    public int compareTo(final AnnotationEnumValue o) {
        final int diff = this.className.compareTo(o.className);
        return (diff == 0) ? this.valueName.compareTo(o.valueName) : diff;
    }
    
    @Override
    public boolean equals(final Object obj) {
        return obj == this || (obj instanceof AnnotationEnumValue && this.compareTo((AnnotationEnumValue)obj) == 0);
    }
    
    @Override
    public int hashCode() {
        return this.className.hashCode() * 11 + this.valueName.hashCode();
    }
    
    @Override
    public String toString() {
        return this.className + "." + this.valueName;
    }
}
