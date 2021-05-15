// 
// Decompiled by Procyon v0.5.36
// 

package nonapi.io.github.classgraph.json;

import io.github.classgraph.ScanResult;
import java.lang.reflect.Type;
import java.util.Set;
import java.util.Collection;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.lang.reflect.ParameterizedType;
import java.util.HashSet;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.List;

class ClassFields
{
    final List<FieldTypeInfo> fieldOrder;
    final Map<String, FieldTypeInfo> fieldNameToFieldTypeInfo;
    Field idField;
    private static final Comparator<Field> FIELD_NAME_ORDER_COMPARATOR;
    private static final Comparator<Field> SERIALIZATION_FORMAT_FIELD_NAME_ORDER_COMPARATOR;
    private static final String SERIALIZATION_FORMAT_CLASS_NAME;
    
    public ClassFields(final Class<?> cls, final boolean resolveTypes, final boolean onlySerializePublicFields, final ClassFieldCache classFieldCache) {
        this.fieldOrder = new ArrayList<FieldTypeInfo>();
        this.fieldNameToFieldTypeInfo = new HashMap<String, FieldTypeInfo>();
        final Set<String> visibleFieldNames = new HashSet<String>();
        final List<List<FieldTypeInfo>> fieldSuperclassReversedOrder = new ArrayList<List<FieldTypeInfo>>();
        TypeResolutions currTypeResolutions = null;
        Type currType = cls;
        while (currType != Object.class && currType != null) {
            Class<?> currRawType;
            if (currType instanceof ParameterizedType) {
                final ParameterizedType currParameterizedType = (ParameterizedType)currType;
                currRawType = (Class<?>)currParameterizedType.getRawType();
            }
            else {
                if (!(currType instanceof Class)) {
                    throw new IllegalArgumentException("Illegal class type: " + currType);
                }
                currRawType = (Class<?>)currType;
            }
            final Field[] fields = currRawType.getDeclaredFields();
            Arrays.sort(fields, cls.getName().equals(ClassFields.SERIALIZATION_FORMAT_CLASS_NAME) ? ClassFields.SERIALIZATION_FORMAT_FIELD_NAME_ORDER_COMPARATOR : ClassFields.FIELD_NAME_ORDER_COMPARATOR);
            final List<FieldTypeInfo> fieldOrderWithinClass = new ArrayList<FieldTypeInfo>();
            for (final Field field : fields) {
                if (visibleFieldNames.add(field.getName())) {
                    final boolean isIdField = field.isAnnotationPresent(Id.class);
                    if (isIdField) {
                        if (this.idField != null) {
                            throw new IllegalArgumentException("More than one @Id annotation: " + this.idField.getDeclaringClass() + "." + this.idField + " ; " + currRawType.getName() + "." + field.getName());
                        }
                        this.idField = field;
                    }
                    if (JSONUtils.fieldIsSerializable(field, onlySerializePublicFields)) {
                        final Type fieldGenericType = field.getGenericType();
                        final Type fieldTypePartiallyResolved = (currTypeResolutions != null && resolveTypes) ? currTypeResolutions.resolveTypeVariables(fieldGenericType) : fieldGenericType;
                        final FieldTypeInfo fieldTypeInfo = new FieldTypeInfo(field, fieldTypePartiallyResolved, classFieldCache);
                        this.fieldNameToFieldTypeInfo.put(field.getName(), fieldTypeInfo);
                        fieldOrderWithinClass.add(fieldTypeInfo);
                    }
                    else if (isIdField) {
                        throw new IllegalArgumentException("@Id annotation field must be accessible, final, and non-transient: " + currRawType.getName() + "." + field.getName());
                    }
                }
            }
            fieldSuperclassReversedOrder.add(fieldOrderWithinClass);
            final Type genericSuperType = currRawType.getGenericSuperclass();
            if (resolveTypes) {
                if (genericSuperType instanceof ParameterizedType) {
                    final Type resolvedSupertype = (currTypeResolutions == null) ? genericSuperType : currTypeResolutions.resolveTypeVariables(genericSuperType);
                    currTypeResolutions = ((resolvedSupertype instanceof ParameterizedType) ? new TypeResolutions((ParameterizedType)resolvedSupertype) : null);
                    currType = resolvedSupertype;
                }
                else {
                    if (!(genericSuperType instanceof Class)) {
                        throw new IllegalArgumentException("Got unexpected supertype " + genericSuperType);
                    }
                    currType = genericSuperType;
                    currTypeResolutions = null;
                }
            }
            else {
                currType = genericSuperType;
            }
        }
        for (int i = fieldSuperclassReversedOrder.size() - 1; i >= 0; --i) {
            final List<FieldTypeInfo> fieldGroupingForClass = fieldSuperclassReversedOrder.get(i);
            this.fieldOrder.addAll(fieldGroupingForClass);
        }
    }
    
    static {
        FIELD_NAME_ORDER_COMPARATOR = new Comparator<Field>() {
            @Override
            public int compare(final Field a, final Field b) {
                return a.getName().compareTo(b.getName());
            }
        };
        SERIALIZATION_FORMAT_FIELD_NAME_ORDER_COMPARATOR = new Comparator<Field>() {
            @Override
            public int compare(final Field a, final Field b) {
                return a.getName().equals("format") ? -1 : (b.getName().equals("format") ? 1 : a.getName().compareTo(b.getName()));
            }
        };
        SERIALIZATION_FORMAT_CLASS_NAME = ScanResult.class.getName() + "$SerializationFormat";
    }
}
