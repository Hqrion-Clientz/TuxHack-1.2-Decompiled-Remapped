// 
// Decompiled by Procyon v0.5.36
// 

package io.github.classgraph;

import java.util.Objects;
import java.lang.reflect.Array;
import java.util.Set;
import java.util.Map;

public class AnnotationParameterValue extends ScanResultObject implements HasName, Comparable<AnnotationParameterValue>
{
    private String name;
    private ObjectTypedValueWrapper value;
    
    AnnotationParameterValue() {
    }
    
    AnnotationParameterValue(final String name, final Object value) {
        this.name = name;
        this.value = new ObjectTypedValueWrapper(value);
    }
    
    @Override
    public String getName() {
        return this.name;
    }
    
    public Object getValue() {
        return (this.value == null) ? null : this.value.get();
    }
    
    void setValue(final Object newValue) {
        this.value = new ObjectTypedValueWrapper(newValue);
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
        if (this.value != null) {
            this.value.setScanResult(scanResult);
        }
    }
    
    @Override
    protected void findReferencedClassInfo(final Map<String, ClassInfo> classNameToClassInfo, final Set<ClassInfo> refdClassInfo) {
        if (this.value != null) {
            this.value.findReferencedClassInfo(classNameToClassInfo, refdClassInfo);
        }
    }
    
    void convertWrapperArraysToPrimitiveArrays(final ClassInfo annotationClassInfo) {
        if (this.value != null) {
            this.value.convertWrapperArraysToPrimitiveArrays(annotationClassInfo, this.name);
        }
    }
    
    Object instantiate(final ClassInfo annotationClassInfo) {
        return this.value.instantiateOrGet(annotationClassInfo, this.name);
    }
    
    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder();
        this.toString(buf);
        return buf.toString();
    }
    
    void toString(final StringBuilder buf) {
        buf.append(this.name);
        buf.append("=");
        this.toStringParamValueOnly(buf);
    }
    
    void toStringParamValueOnly(final StringBuilder buf) {
        if (this.value == null) {
            buf.append("null");
        }
        else {
            final Object paramVal = this.value.get();
            final Class<?> valClass = paramVal.getClass();
            if (valClass.isArray()) {
                buf.append('{');
                for (int j = 0, n = Array.getLength(paramVal); j < n; ++j) {
                    if (j > 0) {
                        buf.append(", ");
                    }
                    final Object elt = Array.get(paramVal, j);
                    buf.append((elt == null) ? "null" : elt.toString());
                }
                buf.append('}');
            }
            else if (paramVal instanceof String) {
                buf.append('\"');
                buf.append(paramVal.toString().replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r"));
                buf.append('\"');
            }
            else if (paramVal instanceof Character) {
                buf.append('\'');
                buf.append(paramVal.toString().replace("'", "\\'").replace("\n", "\\n").replace("\r", "\\r"));
                buf.append('\'');
            }
            else {
                buf.append(paramVal.toString());
            }
        }
    }
    
    private String toStringParamValueOnly() {
        final StringBuilder buf = new StringBuilder();
        this.toStringParamValueOnly(buf);
        return buf.toString();
    }
    
    @Override
    public int compareTo(final AnnotationParameterValue other) {
        if (other == this) {
            return 0;
        }
        final int diff = this.name.compareTo(other.getName());
        if (diff != 0) {
            return diff;
        }
        if (this.value.equals(other.value)) {
            return 0;
        }
        final Object p0 = this.getValue();
        final Object p2 = other.getValue();
        return (p0 == null || p2 == null) ? ((p0 != null) - (p2 != null)) : this.toStringParamValueOnly().compareTo(other.toStringParamValueOnly());
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof AnnotationParameterValue)) {
            return false;
        }
        final AnnotationParameterValue other = (AnnotationParameterValue)obj;
        return this.name.equals(other.name) && this.value == null == (other.value == null) && (this.value == null || this.value.equals(other.value));
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(this.name, this.value);
    }
}
