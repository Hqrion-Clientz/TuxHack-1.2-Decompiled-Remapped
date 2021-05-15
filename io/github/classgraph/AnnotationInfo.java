// 
// Decompiled by Procyon v0.5.36
// 

package io.github.classgraph;

import java.lang.annotation.IncompleteAnnotationException;
import java.util.Arrays;
import nonapi.io.github.classgraph.utils.ReflectionUtils;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.lang.annotation.Annotation;
import java.util.Set;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

public class AnnotationInfo extends ScanResultObject implements Comparable<AnnotationInfo>, HasName
{
    private String name;
    private AnnotationParameterValueList annotationParamValues;
    private transient boolean annotationParamValuesHasBeenConvertedToPrimitive;
    private transient AnnotationParameterValueList annotationParamValuesWithDefaults;
    
    AnnotationInfo() {
    }
    
    AnnotationInfo(final String name, final AnnotationParameterValueList annotationParamValues) {
        this.name = name;
        this.annotationParamValues = annotationParamValues;
    }
    
    @Override
    public String getName() {
        return this.name;
    }
    
    public boolean isInherited() {
        return this.getClassInfo().isInherited;
    }
    
    public AnnotationParameterValueList getDefaultParameterValues() {
        return this.getClassInfo().getAnnotationDefaultParameterValues();
    }
    
    public AnnotationParameterValueList getParameterValues() {
        if (this.annotationParamValuesWithDefaults == null) {
            final ClassInfo classInfo = this.getClassInfo();
            if (classInfo == null) {
                return (this.annotationParamValues == null) ? AnnotationParameterValueList.EMPTY_LIST : this.annotationParamValues;
            }
            if (this.annotationParamValues != null && !this.annotationParamValuesHasBeenConvertedToPrimitive) {
                this.annotationParamValues.convertWrapperArraysToPrimitiveArrays(classInfo);
                this.annotationParamValuesHasBeenConvertedToPrimitive = true;
            }
            if (classInfo.annotationDefaultParamValues != null && !classInfo.annotationDefaultParamValuesHasBeenConvertedToPrimitive) {
                classInfo.annotationDefaultParamValues.convertWrapperArraysToPrimitiveArrays(classInfo);
                classInfo.annotationDefaultParamValuesHasBeenConvertedToPrimitive = true;
            }
            final AnnotationParameterValueList defaultParamValues = classInfo.annotationDefaultParamValues;
            if (defaultParamValues == null && this.annotationParamValues == null) {
                return AnnotationParameterValueList.EMPTY_LIST;
            }
            if (defaultParamValues == null) {
                return this.annotationParamValues;
            }
            if (this.annotationParamValues == null) {
                return defaultParamValues;
            }
            final Map<String, Object> allParamValues = new HashMap<String, Object>();
            for (final AnnotationParameterValue defaultParamValue : defaultParamValues) {
                allParamValues.put(defaultParamValue.getName(), defaultParamValue.getValue());
            }
            for (final AnnotationParameterValue annotationParamValue : this.annotationParamValues) {
                allParamValues.put(annotationParamValue.getName(), annotationParamValue.getValue());
            }
            if (classInfo.methodInfo == null) {
                throw new IllegalArgumentException("Could not find methods for annotation " + classInfo.getName());
            }
            this.annotationParamValuesWithDefaults = new AnnotationParameterValueList();
            for (final MethodInfo mi : classInfo.methodInfo) {
                final String name;
                final String paramName = name = mi.getName();
                switch (name) {
                    case "<init>":
                    case "<clinit>":
                    case "hashCode":
                    case "equals":
                    case "toString":
                    case "annotationType": {
                        continue;
                    }
                    default: {
                        final Object paramValue = allParamValues.get(paramName);
                        if (paramValue != null) {
                            this.annotationParamValuesWithDefaults.add(new AnnotationParameterValue(paramName, paramValue));
                            continue;
                        }
                        continue;
                    }
                }
            }
        }
        return this.annotationParamValuesWithDefaults;
    }
    
    @Override
    protected String getClassName() {
        return this.name;
    }
    
    @Override
    void setScanResult(final ScanResult scanResult) {
        super.setScanResult(scanResult);
        if (this.annotationParamValues != null) {
            for (final AnnotationParameterValue a : this.annotationParamValues) {
                a.setScanResult(scanResult);
            }
        }
    }
    
    @Override
    protected void findReferencedClassInfo(final Map<String, ClassInfo> classNameToClassInfo, final Set<ClassInfo> refdClassInfo) {
        super.findReferencedClassInfo(classNameToClassInfo, refdClassInfo);
        if (this.annotationParamValues != null) {
            for (final AnnotationParameterValue annotationParamValue : this.annotationParamValues) {
                annotationParamValue.findReferencedClassInfo(classNameToClassInfo, refdClassInfo);
            }
        }
    }
    
    public ClassInfo getClassInfo() {
        return super.getClassInfo();
    }
    
    public Annotation loadClassAndInstantiate() {
        final Class<? extends Annotation> annotationClass = this.getClassInfo().loadClass((Class<? extends Annotation>)Annotation.class);
        return (Annotation)Proxy.newProxyInstance(annotationClass.getClassLoader(), new Class[] { annotationClass }, new AnnotationInvocationHandler(annotationClass, this));
    }
    
    void convertWrapperArraysToPrimitiveArrays() {
        if (this.annotationParamValues != null) {
            this.annotationParamValues.convertWrapperArraysToPrimitiveArrays(this.getClassInfo());
        }
    }
    
    @Override
    public int compareTo(final AnnotationInfo o) {
        final int diff = this.getName().compareTo(o.getName());
        if (diff != 0) {
            return diff;
        }
        if (this.annotationParamValues == null && o.annotationParamValues == null) {
            return 0;
        }
        if (this.annotationParamValues == null) {
            return -1;
        }
        if (o.annotationParamValues == null) {
            return 1;
        }
        for (int i = 0, max = Math.max(this.annotationParamValues.size(), o.annotationParamValues.size()); i < max; ++i) {
            if (i >= this.annotationParamValues.size()) {
                return -1;
            }
            if (i >= o.annotationParamValues.size()) {
                return 1;
            }
            final int diff2 = this.annotationParamValues.get(i).compareTo(o.annotationParamValues.get(i));
            if (diff2 != 0) {
                return diff2;
            }
        }
        return 0;
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof AnnotationInfo)) {
            return false;
        }
        final AnnotationInfo other = (AnnotationInfo)obj;
        return this.compareTo(other) == 0;
    }
    
    @Override
    public int hashCode() {
        int h = this.getName().hashCode();
        if (this.annotationParamValues != null) {
            for (final AnnotationParameterValue e : this.annotationParamValues) {
                h = h * 7 + e.getName().hashCode() * 3 + e.getValue().hashCode();
            }
        }
        return h;
    }
    
    void toString(final StringBuilder buf) {
        buf.append('@').append(this.getName());
        final AnnotationParameterValueList paramVals = this.getParameterValues();
        if (!paramVals.isEmpty()) {
            buf.append('(');
            for (int i = 0; i < paramVals.size(); ++i) {
                if (i > 0) {
                    buf.append(", ");
                }
                final AnnotationParameterValue paramVal = paramVals.get(i);
                if (paramVals.size() > 1 || !"value".equals(paramVal.getName())) {
                    paramVal.toString(buf);
                }
                else {
                    paramVal.toStringParamValueOnly(buf);
                }
            }
            buf.append(')');
        }
    }
    
    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder();
        this.toString(buf);
        return buf.toString();
    }
    
    private static class AnnotationInvocationHandler implements InvocationHandler
    {
        private final Class<? extends Annotation> annotationClass;
        private final AnnotationInfo annotationInfo;
        private final Map<String, Object> annotationParameterValuesInstantiated;
        
        AnnotationInvocationHandler(final Class<? extends Annotation> annotationClass, final AnnotationInfo annotationInfo) {
            this.annotationParameterValuesInstantiated = new HashMap<String, Object>();
            this.annotationClass = annotationClass;
            this.annotationInfo = annotationInfo;
            for (final AnnotationParameterValue apv : annotationInfo.getParameterValues()) {
                final Object instantiatedValue = apv.instantiate(annotationInfo.getClassInfo());
                if (instantiatedValue == null) {
                    throw new IllegalArgumentException("Got null value for annotation parameter " + apv.getName() + " of annotation " + annotationInfo.getName());
                }
                this.annotationParameterValuesInstantiated.put(apv.getName(), instantiatedValue);
            }
        }
        
        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args) {
            final String methodName = method.getName();
            final Class<?>[] paramTypes = method.getParameterTypes();
            if (((args == null) ? 0 : args.length) != paramTypes.length) {
                throw new IllegalArgumentException("Wrong number of arguments for " + this.annotationClass.getName() + "." + methodName + ": got " + ((args == null) ? 0 : args.length) + ", expected " + paramTypes.length);
            }
            if (args != null && paramTypes.length == 1) {
                if (!"equals".equals(methodName) || paramTypes[0] != Object.class) {
                    throw new IllegalArgumentException();
                }
                if (this == args[0]) {
                    return true;
                }
                if (!this.annotationClass.isInstance(args[0])) {
                    return false;
                }
                for (final Map.Entry<String, Object> ent : this.annotationParameterValuesInstantiated.entrySet()) {
                    final String paramName = ent.getKey();
                    final Object paramVal = ent.getValue();
                    final Object otherParamVal = ReflectionUtils.invokeMethod(args[0], paramName, false);
                    if (paramVal == null != (otherParamVal == null)) {
                        return false;
                    }
                    if (paramVal == null && otherParamVal == null) {
                        return true;
                    }
                    if (paramVal == null || !paramVal.equals(otherParamVal)) {
                        return false;
                    }
                }
                return true;
            }
            else {
                if (paramTypes.length != 0) {
                    throw new IllegalArgumentException();
                }
                final String s = methodName;
                switch (s) {
                    case "toString": {
                        return this.annotationInfo.toString();
                    }
                    case "hashCode": {
                        int result = 0;
                        for (final Map.Entry<String, Object> ent2 : this.annotationParameterValuesInstantiated.entrySet()) {
                            final String paramName2 = ent2.getKey();
                            final Object paramVal2 = ent2.getValue();
                            int paramValHashCode;
                            if (paramVal2 == null) {
                                paramValHashCode = 0;
                            }
                            else {
                                final Class<?> type = paramVal2.getClass();
                                if (!type.isArray()) {
                                    paramValHashCode = paramVal2.hashCode();
                                }
                                else if (type == byte[].class) {
                                    paramValHashCode = Arrays.hashCode((byte[])paramVal2);
                                }
                                else if (type == char[].class) {
                                    paramValHashCode = Arrays.hashCode((char[])paramVal2);
                                }
                                else if (type == double[].class) {
                                    paramValHashCode = Arrays.hashCode((double[])paramVal2);
                                }
                                else if (type == float[].class) {
                                    paramValHashCode = Arrays.hashCode((float[])paramVal2);
                                }
                                else if (type == int[].class) {
                                    paramValHashCode = Arrays.hashCode((int[])paramVal2);
                                }
                                else if (type == long[].class) {
                                    paramValHashCode = Arrays.hashCode((long[])paramVal2);
                                }
                                else if (type == short[].class) {
                                    paramValHashCode = Arrays.hashCode((short[])paramVal2);
                                }
                                else if (type == boolean[].class) {
                                    paramValHashCode = Arrays.hashCode((boolean[])paramVal2);
                                }
                                else {
                                    paramValHashCode = Arrays.hashCode((Object[])paramVal2);
                                }
                            }
                            result += (127 * paramName2.hashCode() ^ paramValHashCode);
                        }
                        return result;
                    }
                    case "annotationType": {
                        return this.annotationClass;
                    }
                    default: {
                        final Object annotationParameterValue = this.annotationParameterValuesInstantiated.get(methodName);
                        if (annotationParameterValue == null) {
                            throw new IncompleteAnnotationException(this.annotationClass, methodName);
                        }
                        final Class<?> annotationParameterValueClass = annotationParameterValue.getClass();
                        if (!annotationParameterValueClass.isArray()) {
                            return annotationParameterValue;
                        }
                        if (annotationParameterValueClass == String[].class) {
                            return ((String[])annotationParameterValue).clone();
                        }
                        if (annotationParameterValueClass == byte[].class) {
                            return ((byte[])annotationParameterValue).clone();
                        }
                        if (annotationParameterValueClass == char[].class) {
                            return ((char[])annotationParameterValue).clone();
                        }
                        if (annotationParameterValueClass == double[].class) {
                            return ((double[])annotationParameterValue).clone();
                        }
                        if (annotationParameterValueClass == float[].class) {
                            return ((float[])annotationParameterValue).clone();
                        }
                        if (annotationParameterValueClass == int[].class) {
                            return ((int[])annotationParameterValue).clone();
                        }
                        if (annotationParameterValueClass == long[].class) {
                            return ((long[])annotationParameterValue).clone();
                        }
                        if (annotationParameterValueClass == short[].class) {
                            return ((short[])annotationParameterValue).clone();
                        }
                        if (annotationParameterValueClass == boolean[].class) {
                            return ((boolean[])annotationParameterValue).clone();
                        }
                        final Object[] arr = (Object[])annotationParameterValue;
                        return arr.clone();
                    }
                }
            }
        }
    }
}
