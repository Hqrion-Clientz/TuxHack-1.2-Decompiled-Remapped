// 
// Decompiled by Procyon v0.5.36
// 

package nonapi.io.github.classgraph.json;

import java.util.HashSet;
import java.util.HashMap;
import java.lang.reflect.Array;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.List;
import nonapi.io.github.classgraph.utils.CollectionUtils;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Set;
import java.lang.reflect.Field;
import java.util.Iterator;
import io.github.classgraph.ClassGraphException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Map;
import java.util.Comparator;

public final class JSONSerializer
{
    private static final Comparator<Object> SET_COMPARATOR;
    
    private JSONSerializer() {
    }
    
    private static void assignObjectIds(final Object jsonVal, final Map<ReferenceEqualityKey<Object>, JSONObject> objToJSONVal, final ClassFieldCache classFieldCache, final Map<ReferenceEqualityKey<JSONReference>, CharSequence> jsonReferenceToId, final AtomicInteger objId, final boolean onlySerializePublicFields) {
        if (jsonVal instanceof JSONObject) {
            for (final Map.Entry<String, Object> item : ((JSONObject)jsonVal).items) {
                assignObjectIds(item.getValue(), objToJSONVal, classFieldCache, jsonReferenceToId, objId, onlySerializePublicFields);
            }
        }
        else if (jsonVal instanceof JSONArray) {
            for (final Object item2 : ((JSONArray)jsonVal).items) {
                assignObjectIds(item2, objToJSONVal, classFieldCache, jsonReferenceToId, objId, onlySerializePublicFields);
            }
        }
        else if (jsonVal instanceof JSONReference) {
            final Object refdObj = ((JSONReference)jsonVal).idObject;
            if (refdObj == null) {
                throw ClassGraphException.newClassGraphException("Internal inconsistency");
            }
            final ReferenceEqualityKey<Object> refdObjKey = new ReferenceEqualityKey<Object>(refdObj);
            final JSONObject refdJsonVal = objToJSONVal.get(refdObjKey);
            if (refdJsonVal == null) {
                throw ClassGraphException.newClassGraphException("Internal inconsistency");
            }
            final Field annotatedField = classFieldCache.get(refdObj.getClass()).idField;
            CharSequence idStr = null;
            if (annotatedField != null) {
                try {
                    final Object idObject = annotatedField.get(refdObj);
                    if (idObject != null) {
                        idStr = idObject.toString();
                        refdJsonVal.objectId = idStr;
                    }
                }
                catch (IllegalArgumentException | IllegalAccessException ex2) {
                    final Exception ex;
                    final Exception e = ex;
                    throw new IllegalArgumentException("Could not access @Id-annotated field " + annotatedField, e);
                }
            }
            if (idStr == null) {
                if (refdJsonVal.objectId == null) {
                    idStr = "[#" + objId.getAndIncrement() + "]";
                    refdJsonVal.objectId = idStr;
                }
                else {
                    idStr = refdJsonVal.objectId;
                }
            }
            jsonReferenceToId.put(new ReferenceEqualityKey<JSONReference>((JSONReference)jsonVal), idStr);
        }
    }
    
    private static void convertVals(final Object[] convertedVals, final Set<ReferenceEqualityKey<Object>> visitedOnPath, final Set<ReferenceEqualityKey<Object>> standardObjectVisited, final ClassFieldCache classFieldCache, final Map<ReferenceEqualityKey<Object>, JSONObject> objToJSONVal, final boolean onlySerializePublicFields) {
        final ReferenceEqualityKey<?>[] valKeys = (ReferenceEqualityKey<?>[])new ReferenceEqualityKey[convertedVals.length];
        final boolean[] needToConvert = new boolean[convertedVals.length];
        for (int i = 0; i < convertedVals.length; ++i) {
            final Object val = convertedVals[i];
            needToConvert[i] = !JSONUtils.isBasicValueType(val);
            if (needToConvert[i] && !JSONUtils.isCollectionOrArray(val)) {
                final ReferenceEqualityKey<Object> valKey = new ReferenceEqualityKey<Object>(val);
                valKeys[i] = valKey;
                final boolean alreadyVisited = !standardObjectVisited.add(valKey);
                if (alreadyVisited) {
                    convertedVals[i] = new JSONReference(val);
                    needToConvert[i] = false;
                }
            }
            if (val instanceof Class) {
                convertedVals[i] = ((Class)val).getName();
            }
        }
        for (int i = 0; i < convertedVals.length; ++i) {
            if (needToConvert[i]) {
                final Object val = convertedVals[i];
                convertedVals[i] = toJSONGraph(val, visitedOnPath, standardObjectVisited, classFieldCache, objToJSONVal, onlySerializePublicFields);
                if (!JSONUtils.isCollectionOrArray(val)) {
                    final ReferenceEqualityKey<Object> valKey = (ReferenceEqualityKey<Object>)valKeys[i];
                    objToJSONVal.put(valKey, (JSONObject)convertedVals[i]);
                }
            }
        }
    }
    
    private static Object toJSONGraph(final Object obj, final Set<ReferenceEqualityKey<Object>> visitedOnPath, final Set<ReferenceEqualityKey<Object>> standardObjectVisited, final ClassFieldCache classFieldCache, final Map<ReferenceEqualityKey<Object>, JSONObject> objToJSONVal, final boolean onlySerializePublicFields) {
        if (obj instanceof Class) {
            return ((Class)obj).getName();
        }
        if (JSONUtils.isBasicValueType(obj)) {
            return obj;
        }
        final ReferenceEqualityKey<Object> objKey = new ReferenceEqualityKey<Object>(obj);
        if (visitedOnPath.add(objKey)) {
            final Class<?> cls = obj.getClass();
            final boolean isArray = cls.isArray();
            Object jsonVal;
            if (Map.class.isAssignableFrom(cls)) {
                final Map<Object, Object> map = (Map<Object, Object>)obj;
                final ArrayList<?> keys = new ArrayList<Object>(map.keySet());
                final int n = keys.size();
                boolean keysComparable = false;
                Object firstNonNullKey = null;
                for (int i = 0; i < n && firstNonNullKey == null; firstNonNullKey = keys.get(i), ++i) {}
                if (firstNonNullKey != null && Comparable.class.isAssignableFrom(firstNonNullKey.getClass())) {
                    CollectionUtils.sortIfNotEmpty(keys);
                    keysComparable = true;
                }
                final String[] convertedKeys = new String[n];
                for (int j = 0; j < n; ++j) {
                    final Object key = keys.get(j);
                    if (key != null && !JSONUtils.isBasicValueType(key)) {
                        throw new IllegalArgumentException("Map key of type " + key.getClass().getName() + " is not a basic type (String, Integer, etc.), so can't be easily serialized as a JSON associative array key");
                    }
                    convertedKeys[j] = JSONUtils.escapeJSONString((key == null) ? "null" : key.toString());
                }
                if (!keysComparable) {
                    Arrays.sort(convertedKeys);
                }
                final Object[] convertedVals = new Object[n];
                for (int k = 0; k < n; ++k) {
                    convertedVals[k] = map.get(keys.get(k));
                }
                convertVals(convertedVals, visitedOnPath, standardObjectVisited, classFieldCache, objToJSONVal, onlySerializePublicFields);
                final List<Map.Entry<String, Object>> convertedKeyValPairs = new ArrayList<Map.Entry<String, Object>>(n);
                for (int l = 0; l < n; ++l) {
                    convertedKeyValPairs.add(new AbstractMap.SimpleEntry<String, Object>(convertedKeys[l], convertedVals[l]));
                }
                jsonVal = new JSONObject(convertedKeyValPairs);
            }
            else if (isArray || List.class.isAssignableFrom(cls)) {
                final boolean isList = List.class.isAssignableFrom(cls);
                final List<?> list = (List<?>)(isList ? ((List)obj) : null);
                final int n = (list != null) ? list.size() : (isArray ? Array.getLength(obj) : 0);
                final Object[] convertedVals2 = new Object[n];
                for (int m = 0; m < n; ++m) {
                    convertedVals2[m] = ((list != null) ? list.get(m) : (isArray ? Array.get(obj, m) : Integer.valueOf(0)));
                }
                convertVals(convertedVals2, visitedOnPath, standardObjectVisited, classFieldCache, objToJSONVal, onlySerializePublicFields);
                jsonVal = new JSONArray(Arrays.asList(convertedVals2));
            }
            else if (Collection.class.isAssignableFrom(cls)) {
                final Collection<?> collection = (Collection<?>)obj;
                final List<Object> convertedValsList = new ArrayList<Object>(collection);
                if (Set.class.isAssignableFrom(cls)) {
                    CollectionUtils.sortIfNotEmpty(convertedValsList, JSONSerializer.SET_COMPARATOR);
                }
                final Object[] convertedVals3 = convertedValsList.toArray();
                convertVals(convertedVals3, visitedOnPath, standardObjectVisited, classFieldCache, objToJSONVal, onlySerializePublicFields);
                jsonVal = new JSONArray(Arrays.asList(convertedVals3));
            }
            else {
                try {
                    final ClassFields resolvedFields = classFieldCache.get(cls);
                    final List<FieldTypeInfo> fieldOrder = resolvedFields.fieldOrder;
                    final int n = fieldOrder.size();
                    final String[] fieldNames = new String[n];
                    final Object[] convertedVals4 = new Object[n];
                    for (int i = 0; i < n; ++i) {
                        final FieldTypeInfo fieldInfo = fieldOrder.get(i);
                        final Field field = fieldInfo.field;
                        fieldNames[i] = field.getName();
                        convertedVals4[i] = JSONUtils.getFieldValue(obj, field);
                    }
                    convertVals(convertedVals4, visitedOnPath, standardObjectVisited, classFieldCache, objToJSONVal, onlySerializePublicFields);
                    final List<Map.Entry<String, Object>> convertedKeyValPairs2 = new ArrayList<Map.Entry<String, Object>>(n);
                    for (int j = 0; j < n; ++j) {
                        convertedKeyValPairs2.add(new AbstractMap.SimpleEntry<String, Object>(fieldNames[j], convertedVals4[j]));
                    }
                    jsonVal = new JSONObject(convertedKeyValPairs2);
                }
                catch (IllegalArgumentException | IllegalAccessException ex2) {
                    final Exception ex;
                    final Exception e = ex;
                    throw ClassGraphException.newClassGraphException("Could not get value of field in object: " + obj, e);
                }
            }
            visitedOnPath.remove(objKey);
            return jsonVal;
        }
        if (JSONUtils.isCollectionOrArray(obj)) {
            throw new IllegalArgumentException("Cycles involving collections cannot be serialized, since collections are not assigned object ids. Reached cycle at: " + obj);
        }
        return new JSONReference(obj);
    }
    
    static void jsonValToJSONString(final Object jsonVal, final Map<ReferenceEqualityKey<JSONReference>, CharSequence> jsonReferenceToId, final boolean includeNullValuedFields, final int depth, final int indentWidth, final StringBuilder buf) {
        if (jsonVal == null) {
            buf.append("null");
        }
        else if (jsonVal instanceof JSONObject) {
            ((JSONObject)jsonVal).toJSONString(jsonReferenceToId, includeNullValuedFields, depth, indentWidth, buf);
        }
        else if (jsonVal instanceof JSONArray) {
            ((JSONArray)jsonVal).toJSONString(jsonReferenceToId, includeNullValuedFields, depth, indentWidth, buf);
        }
        else if (jsonVal instanceof JSONReference) {
            final Object referencedObjectId = jsonReferenceToId.get(new ReferenceEqualityKey(jsonVal));
            jsonValToJSONString(referencedObjectId, jsonReferenceToId, includeNullValuedFields, depth, indentWidth, buf);
        }
        else if (jsonVal instanceof CharSequence || jsonVal instanceof Character || jsonVal.getClass().isEnum()) {
            buf.append('\"');
            JSONUtils.escapeJSONString(jsonVal.toString(), buf);
            buf.append('\"');
        }
        else {
            buf.append(jsonVal.toString());
        }
    }
    
    public static String serializeObject(final Object obj, final int indentWidth, final boolean onlySerializePublicFields, final ClassFieldCache classFieldCache) {
        final HashMap<ReferenceEqualityKey<Object>, JSONObject> objToJSONVal = new HashMap<ReferenceEqualityKey<Object>, JSONObject>();
        final Object rootJsonVal = toJSONGraph(obj, new HashSet<ReferenceEqualityKey<Object>>(), new HashSet<ReferenceEqualityKey<Object>>(), classFieldCache, objToJSONVal, onlySerializePublicFields);
        final Map<ReferenceEqualityKey<JSONReference>, CharSequence> jsonReferenceToId = new HashMap<ReferenceEqualityKey<JSONReference>, CharSequence>();
        final AtomicInteger objId = new AtomicInteger(0);
        assignObjectIds(rootJsonVal, objToJSONVal, classFieldCache, jsonReferenceToId, objId, onlySerializePublicFields);
        final StringBuilder buf = new StringBuilder(32768);
        jsonValToJSONString(rootJsonVal, jsonReferenceToId, false, 0, indentWidth, buf);
        return buf.toString();
    }
    
    public static String serializeObject(final Object obj, final int indentWidth, final boolean onlySerializePublicFields) {
        return serializeObject(obj, indentWidth, onlySerializePublicFields, new ClassFieldCache(false, false));
    }
    
    public static String serializeObject(final Object obj) {
        return serializeObject(obj, 0, false);
    }
    
    public static String serializeFromField(final Object containingObject, final String fieldName, final int indentWidth, final boolean onlySerializePublicFields, final ClassFieldCache classFieldCache) {
        final FieldTypeInfo fieldResolvedTypeInfo = classFieldCache.get(containingObject.getClass()).fieldNameToFieldTypeInfo.get(fieldName);
        if (fieldResolvedTypeInfo == null) {
            throw new IllegalArgumentException("Class " + containingObject.getClass().getName() + " does not have a field named \"" + fieldName + "\"");
        }
        final Field field = fieldResolvedTypeInfo.field;
        if (!JSONUtils.fieldIsSerializable(field, false)) {
            throw new IllegalArgumentException("Field " + containingObject.getClass().getName() + "." + fieldName + " needs to be accessible, non-transient, and non-final");
        }
        Object fieldValue;
        try {
            fieldValue = JSONUtils.getFieldValue(containingObject, field);
        }
        catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Could get value of field " + fieldName, e);
        }
        return serializeObject(fieldValue, indentWidth, onlySerializePublicFields, classFieldCache);
    }
    
    public static String serializeFromField(final Object containingObject, final String fieldName, final int indentWidth, final boolean onlySerializePublicFields) {
        final ClassFieldCache classFieldCache = new ClassFieldCache(false, onlySerializePublicFields);
        return serializeFromField(containingObject, fieldName, indentWidth, onlySerializePublicFields, classFieldCache);
    }
    
    static {
        SET_COMPARATOR = new Comparator<Object>() {
            @Override
            public int compare(final Object o1, final Object o2) {
                if (o1 == null || o2 == null) {
                    return ((o1 != null) - (o2 != null)) ? 1 : 0;
                }
                if (Comparable.class.isAssignableFrom(o1.getClass()) && Comparable.class.isAssignableFrom(o2.getClass())) {
                    final Comparable<Object> comparableO1 = (Comparable<Object>)o1;
                    return comparableO1.compareTo(o2);
                }
                return o1.toString().compareTo(o2.toString());
            }
        };
    }
}
