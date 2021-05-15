// 
// Decompiled by Procyon v0.5.36
// 

package io.github.classgraph;

import java.util.Objects;
import java.util.Arrays;
import java.util.Set;
import java.util.Map;
import java.lang.reflect.Array;

class ObjectTypedValueWrapper extends ScanResultObject
{
    private AnnotationEnumValue annotationEnumValue;
    private AnnotationClassRef annotationClassRef;
    private AnnotationInfo annotationInfo;
    private String stringValue;
    private Integer integerValue;
    private Long longValue;
    private Short shortValue;
    private Boolean booleanValue;
    private Character characterValue;
    private Float floatValue;
    private Double doubleValue;
    private Byte byteValue;
    private String[] stringArrayValue;
    private int[] intArrayValue;
    private long[] longArrayValue;
    private short[] shortArrayValue;
    private boolean[] booleanArrayValue;
    private char[] charArrayValue;
    private float[] floatArrayValue;
    private double[] doubleArrayValue;
    private byte[] byteArrayValue;
    private ObjectTypedValueWrapper[] objectArrayValue;
    
    public ObjectTypedValueWrapper() {
    }
    
    public ObjectTypedValueWrapper(final Object annotationParamValue) {
        if (annotationParamValue != null) {
            final Class<?> annotationParameterValueClass = annotationParamValue.getClass();
            if (annotationParameterValueClass.isArray()) {
                if (annotationParameterValueClass == String[].class) {
                    this.stringArrayValue = (String[])annotationParamValue;
                }
                else if (annotationParameterValueClass == int[].class) {
                    this.intArrayValue = (int[])annotationParamValue;
                }
                else if (annotationParameterValueClass == long[].class) {
                    this.longArrayValue = (long[])annotationParamValue;
                }
                else if (annotationParameterValueClass == short[].class) {
                    this.shortArrayValue = (short[])annotationParamValue;
                }
                else if (annotationParameterValueClass == boolean[].class) {
                    this.booleanArrayValue = (boolean[])annotationParamValue;
                }
                else if (annotationParameterValueClass == char[].class) {
                    this.charArrayValue = (char[])annotationParamValue;
                }
                else if (annotationParameterValueClass == float[].class) {
                    this.floatArrayValue = (float[])annotationParamValue;
                }
                else if (annotationParameterValueClass == double[].class) {
                    this.doubleArrayValue = (double[])annotationParamValue;
                }
                else if (annotationParameterValueClass == byte[].class) {
                    this.byteArrayValue = (byte[])annotationParamValue;
                }
                else {
                    final int n = Array.getLength(annotationParamValue);
                    this.objectArrayValue = new ObjectTypedValueWrapper[n];
                    for (int i = 0; i < n; ++i) {
                        this.objectArrayValue[i] = new ObjectTypedValueWrapper(Array.get(annotationParamValue, i));
                    }
                }
            }
            else if (annotationParamValue instanceof AnnotationEnumValue) {
                this.annotationEnumValue = (AnnotationEnumValue)annotationParamValue;
            }
            else if (annotationParamValue instanceof AnnotationClassRef) {
                this.annotationClassRef = (AnnotationClassRef)annotationParamValue;
            }
            else if (annotationParamValue instanceof AnnotationInfo) {
                this.annotationInfo = (AnnotationInfo)annotationParamValue;
            }
            else if (annotationParamValue instanceof String) {
                this.stringValue = (String)annotationParamValue;
            }
            else if (annotationParamValue instanceof Integer) {
                this.integerValue = (Integer)annotationParamValue;
            }
            else if (annotationParamValue instanceof Long) {
                this.longValue = (Long)annotationParamValue;
            }
            else if (annotationParamValue instanceof Short) {
                this.shortValue = (Short)annotationParamValue;
            }
            else if (annotationParamValue instanceof Boolean) {
                this.booleanValue = (Boolean)annotationParamValue;
            }
            else if (annotationParamValue instanceof Character) {
                this.characterValue = (Character)annotationParamValue;
            }
            else if (annotationParamValue instanceof Float) {
                this.floatValue = (Float)annotationParamValue;
            }
            else if (annotationParamValue instanceof Double) {
                this.doubleValue = (Double)annotationParamValue;
            }
            else {
                if (!(annotationParamValue instanceof Byte)) {
                    throw new IllegalArgumentException("Unsupported annotation parameter value type: " + annotationParameterValueClass.getName());
                }
                this.byteValue = (Byte)annotationParamValue;
            }
        }
    }
    
    Object instantiateOrGet(final ClassInfo annotationClassInfo, final String paramName) {
        final boolean instantiate = annotationClassInfo != null;
        if (this.annotationEnumValue != null) {
            return instantiate ? this.annotationEnumValue.loadClassAndReturnEnumValue() : this.annotationEnumValue;
        }
        if (this.annotationClassRef != null) {
            return instantiate ? this.annotationClassRef.loadClass() : this.annotationClassRef;
        }
        if (this.annotationInfo != null) {
            return instantiate ? this.annotationInfo.loadClassAndInstantiate() : this.annotationInfo;
        }
        if (this.stringValue != null) {
            return this.stringValue;
        }
        if (this.integerValue != null) {
            return this.integerValue;
        }
        if (this.longValue != null) {
            return this.longValue;
        }
        if (this.shortValue != null) {
            return this.shortValue;
        }
        if (this.booleanValue != null) {
            return this.booleanValue;
        }
        if (this.characterValue != null) {
            return this.characterValue;
        }
        if (this.floatValue != null) {
            return this.floatValue;
        }
        if (this.doubleValue != null) {
            return this.doubleValue;
        }
        if (this.byteValue != null) {
            return this.byteValue;
        }
        if (this.stringArrayValue != null) {
            return this.stringArrayValue;
        }
        if (this.intArrayValue != null) {
            return this.intArrayValue;
        }
        if (this.longArrayValue != null) {
            return this.longArrayValue;
        }
        if (this.shortArrayValue != null) {
            return this.shortArrayValue;
        }
        if (this.booleanArrayValue != null) {
            return this.booleanArrayValue;
        }
        if (this.charArrayValue != null) {
            return this.charArrayValue;
        }
        if (this.floatArrayValue != null) {
            return this.floatArrayValue;
        }
        if (this.doubleArrayValue != null) {
            return this.doubleArrayValue;
        }
        if (this.byteArrayValue != null) {
            return this.byteArrayValue;
        }
        if (this.objectArrayValue != null) {
            final Class<?> eltClass = (Class<?>)(instantiate ? ((Class)this.getArrayValueClassOrName(annotationClassInfo, paramName, true)) : null);
            final Object annotationValueObjectArray = (eltClass == null) ? new Object[this.objectArrayValue.length] : Array.newInstance(eltClass, this.objectArrayValue.length);
            for (int i = 0; i < this.objectArrayValue.length; ++i) {
                if (this.objectArrayValue[i] != null) {
                    final Object eltValue = this.objectArrayValue[i].instantiateOrGet(annotationClassInfo, paramName);
                    Array.set(annotationValueObjectArray, i, eltValue);
                }
            }
            return annotationValueObjectArray;
        }
        return null;
    }
    
    public Object get() {
        return this.instantiateOrGet(null, null);
    }
    
    private Object getArrayValueClassOrName(final ClassInfo annotationClassInfo, final String paramName, final boolean getClass) {
        final MethodInfoList annotationMethodList = (annotationClassInfo == null || annotationClassInfo.methodInfo == null) ? null : annotationClassInfo.methodInfo.get(paramName);
        if (annotationClassInfo != null && annotationMethodList != null && !annotationMethodList.isEmpty()) {
            if (annotationMethodList.size() > 1) {
                throw new IllegalArgumentException("Duplicated annotation parameter method " + paramName + "() in annotation class " + annotationClassInfo.getName());
            }
            final TypeSignature annotationMethodResultTypeSig = annotationMethodList.get(0).getTypeSignatureOrTypeDescriptor().getResultType();
            if (!(annotationMethodResultTypeSig instanceof ArrayTypeSignature)) {
                throw new IllegalArgumentException("Annotation parameter " + paramName + " in annotation class " + annotationClassInfo.getName() + " holds an array, but does not have an array type signature");
            }
            final ArrayTypeSignature arrayTypeSig = (ArrayTypeSignature)annotationMethodResultTypeSig;
            if (arrayTypeSig.getNumDimensions() != 1) {
                throw new IllegalArgumentException("Annotations only support 1-dimensional arrays");
            }
            final TypeSignature elementTypeSig = arrayTypeSig.getElementTypeSignature();
            if (elementTypeSig instanceof ClassRefTypeSignature) {
                final ClassRefTypeSignature classRefTypeSignature = (ClassRefTypeSignature)elementTypeSig;
                return getClass ? classRefTypeSignature.loadClass() : classRefTypeSignature.getFullyQualifiedClassName();
            }
            if (elementTypeSig instanceof BaseTypeSignature) {
                final BaseTypeSignature baseTypeSignature = (BaseTypeSignature)elementTypeSig;
                return getClass ? baseTypeSignature.getType() : baseTypeSignature.getTypeStr();
            }
        }
        else {
            for (final ObjectTypedValueWrapper elt : this.objectArrayValue) {
                if (elt != null) {
                    return (elt.integerValue != null) ? (getClass ? Integer.class : "int") : ((elt.longValue != null) ? (getClass ? Long.class : "long") : ((elt.shortValue != null) ? (getClass ? Short.class : "short") : ((elt.characterValue != null) ? (getClass ? Character.class : "char") : ((elt.byteValue != null) ? (getClass ? Byte.class : "byte") : ((elt.booleanValue != null) ? (getClass ? Boolean.class : "boolean") : ((elt.doubleValue != null) ? (getClass ? Double.class : "double") : ((elt.floatValue != null) ? (getClass ? Float.class : "float") : (getClass ? elt.getClass() : elt.getClass().getName()))))))));
                }
            }
        }
        return getClass ? Object.class : "java.lang.Object";
    }
    
    void convertWrapperArraysToPrimitiveArrays(final ClassInfo annotationClassInfo, final String paramName) {
        if (this.annotationInfo != null) {
            this.annotationInfo.convertWrapperArraysToPrimitiveArrays();
        }
        else if (this.objectArrayValue != null) {
            for (final ObjectTypedValueWrapper elt : this.objectArrayValue) {
                if (elt.annotationInfo != null) {
                    elt.annotationInfo.convertWrapperArraysToPrimitiveArrays();
                }
            }
            if (this.objectArrayValue.getClass().getComponentType().isArray()) {
                return;
            }
            final String s;
            final String targetElementTypeName = s = (String)this.getArrayValueClassOrName(annotationClassInfo, paramName, false);
            switch (s) {
                case "java.lang.String": {
                    this.stringArrayValue = new String[this.objectArrayValue.length];
                    for (int j = 0; j < this.objectArrayValue.length; ++j) {
                        this.stringArrayValue[j] = this.objectArrayValue[j].stringValue;
                    }
                    this.objectArrayValue = null;
                    break;
                }
                case "int": {
                    this.intArrayValue = new int[this.objectArrayValue.length];
                    for (int j = 0; j < this.objectArrayValue.length; ++j) {
                        final ObjectTypedValueWrapper elt2 = this.objectArrayValue[j];
                        if (elt2 == null) {
                            throw new IllegalArgumentException("Illegal null value for array of element type " + targetElementTypeName + " in parameter " + paramName + " of annotation class " + ((annotationClassInfo == null) ? "<class outside accept>" : annotationClassInfo.getName()));
                        }
                        this.intArrayValue[j] = this.objectArrayValue[j].integerValue;
                    }
                    this.objectArrayValue = null;
                    break;
                }
                case "long": {
                    this.longArrayValue = new long[this.objectArrayValue.length];
                    for (int j = 0; j < this.objectArrayValue.length; ++j) {
                        final ObjectTypedValueWrapper elt2 = this.objectArrayValue[j];
                        if (elt2 == null) {
                            throw new IllegalArgumentException("Illegal null value for array of element type " + targetElementTypeName + " in parameter " + paramName + " of annotation class " + ((annotationClassInfo == null) ? "<class outside accept>" : annotationClassInfo.getName()));
                        }
                        this.longArrayValue[j] = this.objectArrayValue[j].longValue;
                    }
                    this.objectArrayValue = null;
                    break;
                }
                case "short": {
                    this.shortArrayValue = new short[this.objectArrayValue.length];
                    for (int j = 0; j < this.objectArrayValue.length; ++j) {
                        final ObjectTypedValueWrapper elt2 = this.objectArrayValue[j];
                        if (elt2 == null) {
                            throw new IllegalArgumentException("Illegal null value for array of element type " + targetElementTypeName + " in parameter " + paramName + " of annotation class " + ((annotationClassInfo == null) ? "<class outside accept>" : annotationClassInfo.getName()));
                        }
                        this.shortArrayValue[j] = this.objectArrayValue[j].shortValue;
                    }
                    this.objectArrayValue = null;
                    break;
                }
                case "char": {
                    this.charArrayValue = new char[this.objectArrayValue.length];
                    for (int j = 0; j < this.objectArrayValue.length; ++j) {
                        final ObjectTypedValueWrapper elt2 = this.objectArrayValue[j];
                        if (elt2 == null) {
                            throw new IllegalArgumentException("Illegal null value for array of element type " + targetElementTypeName + " in parameter " + paramName + " of annotation class " + ((annotationClassInfo == null) ? "<class outside accept>" : annotationClassInfo.getName()));
                        }
                        this.charArrayValue[j] = this.objectArrayValue[j].characterValue;
                    }
                    this.objectArrayValue = null;
                    break;
                }
                case "float": {
                    this.floatArrayValue = new float[this.objectArrayValue.length];
                    for (int j = 0; j < this.objectArrayValue.length; ++j) {
                        final ObjectTypedValueWrapper elt2 = this.objectArrayValue[j];
                        if (elt2 == null) {
                            throw new IllegalArgumentException("Illegal null value for array of element type " + targetElementTypeName + " in parameter " + paramName + " of annotation class " + ((annotationClassInfo == null) ? "<class outside accept>" : annotationClassInfo.getName()));
                        }
                        this.floatArrayValue[j] = this.objectArrayValue[j].floatValue;
                    }
                    this.objectArrayValue = null;
                    break;
                }
                case "double": {
                    this.doubleArrayValue = new double[this.objectArrayValue.length];
                    for (int j = 0; j < this.objectArrayValue.length; ++j) {
                        final ObjectTypedValueWrapper elt2 = this.objectArrayValue[j];
                        if (elt2 == null) {
                            throw new IllegalArgumentException("Illegal null value for array of element type " + targetElementTypeName + " in parameter " + paramName + " of annotation class " + ((annotationClassInfo == null) ? "<class outside accept>" : annotationClassInfo.getName()));
                        }
                        this.doubleArrayValue[j] = this.objectArrayValue[j].doubleValue;
                    }
                    this.objectArrayValue = null;
                    break;
                }
                case "boolean": {
                    this.booleanArrayValue = new boolean[this.objectArrayValue.length];
                    for (int j = 0; j < this.objectArrayValue.length; ++j) {
                        final ObjectTypedValueWrapper elt2 = this.objectArrayValue[j];
                        if (elt2 == null) {
                            throw new IllegalArgumentException("Illegal null value for array of element type " + targetElementTypeName + " in parameter " + paramName + " of annotation class " + ((annotationClassInfo == null) ? "<class outside accept>" : annotationClassInfo.getName()));
                        }
                        this.booleanArrayValue[j] = this.objectArrayValue[j].booleanValue;
                    }
                    this.objectArrayValue = null;
                    break;
                }
                case "byte": {
                    this.byteArrayValue = new byte[this.objectArrayValue.length];
                    for (int j = 0; j < this.objectArrayValue.length; ++j) {
                        final ObjectTypedValueWrapper elt2 = this.objectArrayValue[j];
                        if (elt2 == null) {
                            throw new IllegalArgumentException("Illegal null value for array of element type " + targetElementTypeName + " in parameter " + paramName + " of annotation class " + ((annotationClassInfo == null) ? "<class outside accept>" : annotationClassInfo.getName()));
                        }
                        this.byteArrayValue[j] = this.objectArrayValue[j].byteValue;
                    }
                    this.objectArrayValue = null;
                    break;
                }
            }
        }
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
        if (this.annotationEnumValue != null) {
            this.annotationEnumValue.setScanResult(scanResult);
        }
        else if (this.annotationClassRef != null) {
            this.annotationClassRef.setScanResult(scanResult);
        }
        else if (this.annotationInfo != null) {
            this.annotationInfo.setScanResult(scanResult);
        }
        else if (this.objectArrayValue != null) {
            for (final ObjectTypedValueWrapper anObjectArrayValue : this.objectArrayValue) {
                if (anObjectArrayValue != null) {
                    anObjectArrayValue.setScanResult(scanResult);
                }
            }
        }
    }
    
    @Override
    protected void findReferencedClassInfo(final Map<String, ClassInfo> classNameToClassInfo, final Set<ClassInfo> refdClassInfo) {
        if (this.annotationEnumValue != null) {
            this.annotationEnumValue.findReferencedClassInfo(classNameToClassInfo, refdClassInfo);
        }
        else if (this.annotationClassRef != null) {
            final ClassInfo classInfo = this.annotationClassRef.getClassInfo();
            if (classInfo != null) {
                refdClassInfo.add(classInfo);
            }
        }
        else if (this.annotationInfo != null) {
            this.annotationInfo.findReferencedClassInfo(classNameToClassInfo, refdClassInfo);
        }
        else if (this.objectArrayValue != null) {
            for (final ObjectTypedValueWrapper item : this.objectArrayValue) {
                item.findReferencedClassInfo(classNameToClassInfo, refdClassInfo);
            }
        }
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(this.annotationEnumValue, this.annotationClassRef, this.annotationInfo, this.stringValue, this.integerValue, this.longValue, this.shortValue, this.booleanValue, this.characterValue, this.floatValue, this.doubleValue, this.byteValue, Arrays.hashCode(this.stringArrayValue), Arrays.hashCode(this.intArrayValue), Arrays.hashCode(this.longArrayValue), Arrays.hashCode(this.shortArrayValue), Arrays.hashCode(this.booleanArrayValue), Arrays.hashCode(this.charArrayValue), Arrays.hashCode(this.floatArrayValue), Arrays.hashCode(this.doubleArrayValue), Arrays.hashCode(this.byteArrayValue), Arrays.hashCode(this.objectArrayValue));
    }
    
    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof ObjectTypedValueWrapper)) {
            return false;
        }
        final ObjectTypedValueWrapper o = (ObjectTypedValueWrapper)other;
        return Objects.equals(this.annotationEnumValue, o.annotationEnumValue) && Objects.equals(this.annotationClassRef, o.annotationClassRef) && Objects.equals(this.annotationInfo, o.annotationInfo) && Objects.equals(this.stringValue, o.stringValue) && Objects.equals(this.integerValue, o.integerValue) && Objects.equals(this.longValue, o.longValue) && Objects.equals(this.shortValue, o.shortValue) && Objects.equals(this.booleanValue, o.booleanValue) && Objects.equals(this.characterValue, o.characterValue) && Objects.equals(this.floatValue, o.floatValue) && Objects.equals(this.doubleValue, o.doubleValue) && Objects.equals(this.byteValue, o.byteValue) && Arrays.equals(this.stringArrayValue, o.stringArrayValue) && Arrays.equals(this.intArrayValue, o.intArrayValue) && Arrays.equals(this.longArrayValue, o.longArrayValue) && Arrays.equals(this.shortArrayValue, o.shortArrayValue) && Arrays.equals(this.floatArrayValue, o.floatArrayValue) && Arrays.equals(this.byteArrayValue, o.byteArrayValue) && Arrays.deepEquals(this.objectArrayValue, o.objectArrayValue);
    }
}
