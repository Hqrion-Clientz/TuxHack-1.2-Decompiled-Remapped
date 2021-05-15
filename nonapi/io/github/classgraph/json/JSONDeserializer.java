// 
// Decompiled by Procyon v0.5.36
// 

package nonapi.io.github.classgraph.json;

import java.util.AbstractMap;
import nonapi.io.github.classgraph.types.ParseException;
import java.util.Iterator;
import java.lang.reflect.Constructor;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.lang.reflect.ParameterizedType;
import io.github.classgraph.ClassGraphException;
import java.lang.reflect.Type;

public class JSONDeserializer
{
    private JSONDeserializer() {
    }
    
    private static Object jsonBasicValueToObject(final Object jsonVal, final Type expectedType, final boolean convertStringToNumber) {
        if (jsonVal == null) {
            return null;
        }
        if (jsonVal instanceof JSONArray || jsonVal instanceof JSONObject) {
            throw ClassGraphException.newClassGraphException("Expected a basic value type");
        }
        if (expectedType instanceof ParameterizedType) {
            if (((ParameterizedType)expectedType).getRawType().getClass() == Class.class) {
                final String str = jsonVal.toString();
                final int idx = str.indexOf(60);
                final String className = str.substring(0, (idx < 0) ? str.length() : idx);
                try {
                    return Class.forName(className);
                }
                catch (ClassNotFoundException e) {
                    throw new IllegalArgumentException("Could not deserialize class reference " + jsonVal, e);
                }
            }
            throw new IllegalArgumentException("Got illegal ParameterizedType: " + expectedType);
        }
        if (!(expectedType instanceof Class)) {
            throw new IllegalArgumentException("Got illegal basic value type: " + expectedType);
        }
        final Class<?> rawType = (Class<?>)expectedType;
        if (rawType == String.class) {
            if (!(jsonVal instanceof CharSequence)) {
                throw new IllegalArgumentException("Expected string; got " + jsonVal.getClass().getName());
            }
            return jsonVal.toString();
        }
        else if (rawType == CharSequence.class) {
            if (!(jsonVal instanceof CharSequence)) {
                throw new IllegalArgumentException("Expected CharSequence; got " + jsonVal.getClass().getName());
            }
            return jsonVal;
        }
        else if (rawType == Integer.class || rawType == Integer.TYPE) {
            if (convertStringToNumber && jsonVal instanceof CharSequence) {
                return Integer.parseInt(jsonVal.toString());
            }
            if (!(jsonVal instanceof Integer)) {
                throw new IllegalArgumentException("Expected integer; got " + jsonVal.getClass().getName());
            }
            return jsonVal;
        }
        else if (rawType == Long.class || rawType == Long.TYPE) {
            final boolean isLong = jsonVal instanceof Long;
            final boolean isInteger = jsonVal instanceof Integer;
            if (convertStringToNumber && jsonVal instanceof CharSequence) {
                return isLong ? Long.parseLong(jsonVal.toString()) : Integer.parseInt(jsonVal.toString());
            }
            if (!isLong && !isInteger) {
                throw new IllegalArgumentException("Expected long; got " + jsonVal.getClass().getName());
            }
            if (isLong) {
                return jsonVal;
            }
            return jsonVal;
        }
        else if (rawType == Short.class || rawType == Short.TYPE) {
            if (convertStringToNumber && jsonVal instanceof CharSequence) {
                return Short.parseShort(jsonVal.toString());
            }
            if (!(jsonVal instanceof Integer)) {
                throw new IllegalArgumentException("Expected short; got " + jsonVal.getClass().getName());
            }
            final int intValue = (int)jsonVal;
            if (intValue < -32768 || intValue > 32767) {
                throw new IllegalArgumentException("Expected short; got out-of-range value " + intValue);
            }
            return (short)intValue;
        }
        else if (rawType == Float.class || rawType == Float.TYPE) {
            if (convertStringToNumber && jsonVal instanceof CharSequence) {
                return Float.parseFloat(jsonVal.toString());
            }
            if (!(jsonVal instanceof Double)) {
                throw new IllegalArgumentException("Expected float; got " + jsonVal.getClass().getName());
            }
            final double doubleValue = (double)jsonVal;
            if (doubleValue < -3.4028234663852886E38 || doubleValue > 3.4028234663852886E38) {
                throw new IllegalArgumentException("Expected float; got out-of-range value " + doubleValue);
            }
            return (float)doubleValue;
        }
        else if (rawType == Double.class || rawType == Double.TYPE) {
            if (convertStringToNumber && jsonVal instanceof CharSequence) {
                return Double.parseDouble(jsonVal.toString());
            }
            if (!(jsonVal instanceof Double)) {
                throw new IllegalArgumentException("Expected double; got " + jsonVal.getClass().getName());
            }
            return jsonVal;
        }
        else if (rawType == Byte.class || rawType == Byte.TYPE) {
            if (convertStringToNumber && jsonVal instanceof CharSequence) {
                return Byte.parseByte(jsonVal.toString());
            }
            if (!(jsonVal instanceof Integer)) {
                throw new IllegalArgumentException("Expected byte; got " + jsonVal.getClass().getName());
            }
            final int intValue = (int)jsonVal;
            if (intValue < -128 || intValue > 127) {
                throw new IllegalArgumentException("Expected byte; got out-of-range value " + intValue);
            }
            return (byte)intValue;
        }
        else if (rawType == Character.class || rawType == Character.TYPE) {
            if (!(jsonVal instanceof CharSequence)) {
                throw new IllegalArgumentException("Expected character; got " + jsonVal.getClass().getName());
            }
            final CharSequence charSequence = (CharSequence)jsonVal;
            if (charSequence.length() != 1) {
                throw new IllegalArgumentException("Expected single character; got string");
            }
            return charSequence.charAt(0);
        }
        else if (rawType == Boolean.class || rawType == Boolean.TYPE) {
            if (convertStringToNumber && jsonVal instanceof CharSequence) {
                return Boolean.parseBoolean(jsonVal.toString());
            }
            if (!(jsonVal instanceof Boolean)) {
                throw new IllegalArgumentException("Expected boolean; got " + jsonVal.getClass().getName());
            }
            return jsonVal;
        }
        else if (Enum.class.isAssignableFrom(rawType)) {
            if (!(jsonVal instanceof CharSequence)) {
                throw new IllegalArgumentException("Expected string for enum value; got " + jsonVal.getClass().getName());
            }
            final Enum enumValue = (Enum)Enum.valueOf(rawType, jsonVal.toString());
            return enumValue;
        }
        else {
            if (JSONUtils.getRawType(expectedType).isAssignableFrom(jsonVal.getClass())) {
                return jsonVal;
            }
            throw new IllegalArgumentException("Got type " + jsonVal.getClass() + "; expected " + expectedType);
        }
    }
    
    private static void populateObjectFromJsonObject(final Object objectInstance, final Type objectResolvedType, final Object jsonVal, final ClassFieldCache classFieldCache, final Map<CharSequence, Object> idToObjectInstance, final List<Runnable> collectionElementAdders) {
        if (jsonVal == null) {
            return;
        }
        final boolean isJsonObject = jsonVal instanceof JSONObject;
        final boolean isJsonArray = jsonVal instanceof JSONArray;
        if (!isJsonArray && !isJsonObject) {
            throw new IllegalArgumentException("Expected JSONObject or JSONArray, got " + jsonVal.getClass().getSimpleName());
        }
        final JSONObject jsonObject = isJsonObject ? ((JSONObject)jsonVal) : null;
        final JSONArray jsonArray = isJsonArray ? ((JSONArray)jsonVal) : null;
        final Class<?> rawType = objectInstance.getClass();
        final boolean isMap = Map.class.isAssignableFrom(rawType);
        final Map<Object, Object> mapInstance = (Map<Object, Object>)(isMap ? ((Map)objectInstance) : null);
        final boolean isCollection = Collection.class.isAssignableFrom(rawType);
        final Collection<Object> collectionInstance = (Collection<Object>)(isCollection ? ((Collection)objectInstance) : null);
        final boolean isArray = rawType.isArray();
        final boolean isObj = !isMap && !isCollection && !isArray;
        if ((isMap || isObj) != isJsonObject || (isCollection || isArray) != isJsonArray) {
            throw new IllegalArgumentException("Wrong JSON type for class " + objectInstance.getClass().getName());
        }
        Type objectResolvedTypeGeneric = objectResolvedType;
        if (objectResolvedType instanceof Class) {
            final Class<?> objectResolvedCls = (Class<?>)objectResolvedType;
            if (Map.class.isAssignableFrom(objectResolvedCls)) {
                if (!isMap) {
                    throw new IllegalArgumentException("Got an unexpected map type");
                }
                objectResolvedTypeGeneric = objectResolvedCls.getGenericSuperclass();
            }
            else if (Collection.class.isAssignableFrom(objectResolvedCls)) {
                if (!isCollection) {
                    throw new IllegalArgumentException("Got an unexpected map type");
                }
                objectResolvedTypeGeneric = objectResolvedCls.getGenericSuperclass();
            }
        }
        TypeResolutions typeResolutions;
        Type mapKeyType;
        Class<?> arrayComponentType;
        boolean is1DArray;
        Type commonResolvedValueType;
        if (objectResolvedTypeGeneric instanceof Class) {
            typeResolutions = null;
            mapKeyType = null;
            final Class<?> objectResolvedCls2 = (Class<?>)objectResolvedTypeGeneric;
            if (isArray) {
                arrayComponentType = objectResolvedCls2.getComponentType();
                is1DArray = !arrayComponentType.isArray();
            }
            else {
                arrayComponentType = null;
                is1DArray = false;
            }
            commonResolvedValueType = null;
        }
        else {
            if (!(objectResolvedTypeGeneric instanceof ParameterizedType)) {
                throw new IllegalArgumentException("Got illegal type: " + objectResolvedTypeGeneric);
            }
            final ParameterizedType parameterizedResolvedType = (ParameterizedType)objectResolvedTypeGeneric;
            typeResolutions = new TypeResolutions(parameterizedResolvedType);
            final int numTypeArgs = typeResolutions.resolvedTypeArguments.length;
            if (isMap && numTypeArgs != 2) {
                throw new IllegalArgumentException("Wrong number of type arguments for Map: got " + numTypeArgs + "; expected 2");
            }
            if (isCollection && numTypeArgs != 1) {
                throw new IllegalArgumentException("Wrong number of type arguments for Collection: got " + numTypeArgs + "; expected 1");
            }
            mapKeyType = (isMap ? typeResolutions.resolvedTypeArguments[0] : null);
            commonResolvedValueType = (isMap ? typeResolutions.resolvedTypeArguments[1] : (isCollection ? typeResolutions.resolvedTypeArguments[0] : null));
            is1DArray = false;
            arrayComponentType = null;
        }
        final Class<?> commonValueRawType = (commonResolvedValueType == null) ? null : JSONUtils.getRawType(commonResolvedValueType);
        Constructor<?> commonValueConstructorWithSizeHint;
        if (isMap || isCollection || (is1DArray && !JSONUtils.isBasicValueType(arrayComponentType))) {
            commonValueConstructorWithSizeHint = classFieldCache.getConstructorWithSizeHintForConcreteTypeOf(is1DArray ? arrayComponentType : commonValueRawType);
            if (commonValueConstructorWithSizeHint != null) {
                final Constructor<?> commonValueDefaultConstructor = null;
            }
            else {
                final Constructor<?> commonValueDefaultConstructor = classFieldCache.getDefaultConstructorForConcreteTypeOf(is1DArray ? arrayComponentType : commonValueRawType);
            }
        }
        else {
            commonValueConstructorWithSizeHint = null;
            final Constructor<?> commonValueDefaultConstructor = null;
        }
        final ClassFields classFields = isObj ? classFieldCache.get(rawType) : null;
        ArrayList<ObjectInstantiation> itemsToRecurseToInPass2 = null;
        for (int numItems = (jsonObject != null) ? jsonObject.items.size() : ((jsonArray != null) ? jsonArray.items.size() : 0), i = 0; i < numItems; ++i) {
            String itemJsonKey;
            Object itemJsonValue;
            if (jsonObject != null) {
                final Map.Entry<String, Object> jsonObjectItem = jsonObject.items.get(i);
                itemJsonKey = jsonObjectItem.getKey();
                itemJsonValue = jsonObjectItem.getValue();
            }
            else {
                if (jsonArray == null) {
                    throw ClassGraphException.newClassGraphException("This exception should not be thrown");
                }
                itemJsonKey = null;
                itemJsonValue = jsonArray.items.get(i);
            }
            final boolean itemJsonValueIsJsonObject = itemJsonValue instanceof JSONObject;
            final boolean itemJsonValueIsJsonArray = itemJsonValue instanceof JSONArray;
            final JSONObject itemJsonValueJsonObject = itemJsonValueIsJsonObject ? ((JSONObject)itemJsonValue) : null;
            final JSONArray itemJsonValueJsonArray = itemJsonValueIsJsonArray ? ((JSONArray)itemJsonValue) : null;
            FieldTypeInfo fieldTypeInfo;
            if (classFields != null) {
                fieldTypeInfo = classFields.fieldNameToFieldTypeInfo.get(itemJsonKey);
                if (fieldTypeInfo == null) {
                    throw new IllegalArgumentException("Field " + rawType.getName() + "." + itemJsonKey + " does not exist or is not accessible, non-final, and non-transient");
                }
            }
            else {
                fieldTypeInfo = null;
            }
            final Type resolvedItemValueType = (fieldTypeInfo != null) ? fieldTypeInfo.getFullyResolvedFieldType(typeResolutions) : (isArray ? arrayComponentType : commonResolvedValueType);
            Object instantiatedItemObject;
            if (itemJsonValue == null) {
                instantiatedItemObject = null;
            }
            else if (resolvedItemValueType == Object.class) {
                if (itemJsonValueIsJsonObject) {
                    instantiatedItemObject = new HashMap();
                    if (itemsToRecurseToInPass2 == null) {
                        itemsToRecurseToInPass2 = new ArrayList<ObjectInstantiation>();
                    }
                    itemsToRecurseToInPass2.add(new ObjectInstantiation(instantiatedItemObject, ParameterizedTypeImpl.MAP_OF_UNKNOWN_TYPE, itemJsonValue));
                }
                else if (itemJsonValueIsJsonArray) {
                    instantiatedItemObject = new ArrayList();
                    if (itemsToRecurseToInPass2 == null) {
                        itemsToRecurseToInPass2 = new ArrayList<ObjectInstantiation>();
                    }
                    itemsToRecurseToInPass2.add(new ObjectInstantiation(instantiatedItemObject, ParameterizedTypeImpl.LIST_OF_UNKNOWN_TYPE, itemJsonValue));
                }
                else {
                    instantiatedItemObject = jsonBasicValueToObject(itemJsonValue, resolvedItemValueType, false);
                }
            }
            else if (JSONUtils.isBasicValueType(resolvedItemValueType)) {
                if (itemJsonValueIsJsonObject || itemJsonValueIsJsonArray) {
                    throw new IllegalArgumentException("Got JSONObject or JSONArray type when expecting a simple value type");
                }
                instantiatedItemObject = jsonBasicValueToObject(itemJsonValue, resolvedItemValueType, false);
            }
            else if (CharSequence.class.isAssignableFrom(itemJsonValue.getClass())) {
                final Object linkedObject = idToObjectInstance.get(itemJsonValue);
                if (linkedObject == null) {
                    throw new IllegalArgumentException("Object id not found: " + itemJsonValue);
                }
                instantiatedItemObject = linkedObject;
            }
            else {
                if (!itemJsonValueIsJsonObject && !itemJsonValueIsJsonArray) {
                    throw new IllegalArgumentException("Got simple value type when expecting a JSON object or JSON array");
                }
                try {
                    final int numSubItems = (itemJsonValueJsonObject != null) ? itemJsonValueJsonObject.items.size() : ((itemJsonValueJsonArray != null) ? itemJsonValueJsonArray.items.size() : 0);
                    if (resolvedItemValueType instanceof Class && ((Class)resolvedItemValueType).isArray()) {
                        if (!itemJsonValueIsJsonArray) {
                            throw new IllegalArgumentException("Expected JSONArray, got " + itemJsonValue.getClass().getName());
                        }
                        instantiatedItemObject = Array.newInstance(((Class)resolvedItemValueType).getComponentType(), numSubItems);
                    }
                    else if (isCollection || isMap || is1DArray) {
                        final Constructor<?> commonValueDefaultConstructor;
                        instantiatedItemObject = ((commonValueConstructorWithSizeHint != null) ? commonValueConstructorWithSizeHint.newInstance(numSubItems) : ((commonValueDefaultConstructor != null) ? commonValueDefaultConstructor.newInstance(new Object[0]) : null));
                    }
                    else if (fieldTypeInfo != null) {
                        final Constructor<?> valueConstructorWithSizeHint = fieldTypeInfo.getConstructorForFieldTypeWithSizeHint(resolvedItemValueType, classFieldCache);
                        if (valueConstructorWithSizeHint != null) {
                            instantiatedItemObject = valueConstructorWithSizeHint.newInstance(numSubItems);
                        }
                        else {
                            instantiatedItemObject = fieldTypeInfo.getDefaultConstructorForFieldType(resolvedItemValueType, classFieldCache).newInstance(new Object[0]);
                        }
                    }
                    else {
                        if (!isArray || is1DArray) {
                            throw new IllegalArgumentException("Got illegal type");
                        }
                        instantiatedItemObject = Array.newInstance(rawType.getComponentType(), numSubItems);
                    }
                }
                catch (ReflectiveOperationException | SecurityException ex2) {
                    final Exception ex;
                    final Exception e = ex;
                    throw new IllegalArgumentException("Could not instantiate type " + resolvedItemValueType, e);
                }
                if (itemJsonValue instanceof JSONObject) {
                    final JSONObject itemJsonObject = (JSONObject)itemJsonValue;
                    if (itemJsonObject.objectId != null) {
                        idToObjectInstance.put(itemJsonObject.objectId, instantiatedItemObject);
                    }
                }
                if (itemsToRecurseToInPass2 == null) {
                    itemsToRecurseToInPass2 = new ArrayList<ObjectInstantiation>();
                }
                itemsToRecurseToInPass2.add(new ObjectInstantiation(instantiatedItemObject, resolvedItemValueType, itemJsonValue));
            }
            if (fieldTypeInfo != null) {
                fieldTypeInfo.setFieldValue(objectInstance, instantiatedItemObject);
            }
            else if (mapInstance != null) {
                final Object mapKey = jsonBasicValueToObject(itemJsonKey, mapKeyType, true);
                mapInstance.put(mapKey, instantiatedItemObject);
            }
            else if (isArray) {
                Array.set(objectInstance, i, instantiatedItemObject);
            }
            else if (collectionInstance != null) {
                collectionElementAdders.add(new Runnable() {
                    @Override
                    public void run() {
                        collectionInstance.add(instantiatedItemObject);
                    }
                });
            }
        }
        if (itemsToRecurseToInPass2 != null) {
            for (final ObjectInstantiation j : itemsToRecurseToInPass2) {
                populateObjectFromJsonObject(j.objectInstance, j.type, j.jsonVal, classFieldCache, idToObjectInstance, collectionElementAdders);
            }
        }
    }
    
    private static Map<CharSequence, Object> getInitialIdToObjectMap(final Object objectInstance, final Object parsedJSON) {
        final Map<CharSequence, Object> idToObjectInstance = new HashMap<CharSequence, Object>();
        if (parsedJSON instanceof JSONObject) {
            final JSONObject itemJsonObject = (JSONObject)parsedJSON;
            if (!itemJsonObject.items.isEmpty()) {
                final Map.Entry<String, Object> firstItem = itemJsonObject.items.get(0);
                if (firstItem.getKey().equals("__ID")) {
                    final Object firstItemValue = firstItem.getValue();
                    if (firstItemValue == null || !CharSequence.class.isAssignableFrom(firstItemValue.getClass())) {
                        idToObjectInstance.put((CharSequence)firstItemValue, objectInstance);
                    }
                }
            }
        }
        return idToObjectInstance;
    }
    
    private static <T> T deserializeObject(final Class<T> expectedType, final String json, final ClassFieldCache classFieldCache) throws IllegalArgumentException {
        Object parsedJSON;
        try {
            parsedJSON = JSONParser.parseJSON(json);
        }
        catch (ParseException e) {
            throw new IllegalArgumentException("Could not parse JSON", e);
        }
        T objectInstance;
        try {
            final Constructor<?> constructor = classFieldCache.getDefaultConstructorForConcreteTypeOf(expectedType);
            final T newInstance = objectInstance = (T)constructor.newInstance(new Object[0]);
        }
        catch (ReflectiveOperationException | SecurityException ex2) {
            final Exception ex;
            final Exception e2 = ex;
            throw new IllegalArgumentException("Could not construct object of type " + expectedType.getName(), e2);
        }
        final List<Runnable> collectionElementAdders = new ArrayList<Runnable>();
        populateObjectFromJsonObject(objectInstance, expectedType, parsedJSON, classFieldCache, getInitialIdToObjectMap(objectInstance, parsedJSON), collectionElementAdders);
        for (final Runnable runnable : collectionElementAdders) {
            runnable.run();
        }
        return objectInstance;
    }
    
    public static <T> T deserializeObject(final Class<T> expectedType, final String json) throws IllegalArgumentException {
        final ClassFieldCache classFieldCache = new ClassFieldCache(true, false);
        return deserializeObject(expectedType, json, classFieldCache);
    }
    
    public static void deserializeToField(final Object containingObject, final String fieldName, final String json, final ClassFieldCache classFieldCache) throws IllegalArgumentException {
        if (containingObject == null) {
            throw new IllegalArgumentException("Cannot deserialize to a field of a null object");
        }
        Object parsedJSON;
        try {
            parsedJSON = JSONParser.parseJSON(json);
        }
        catch (ParseException e) {
            throw new IllegalArgumentException("Could not parse JSON", e);
        }
        final JSONObject wrapperJsonObj = new JSONObject(1);
        wrapperJsonObj.items.add(new AbstractMap.SimpleEntry<String, Object>(fieldName, parsedJSON));
        final List<Runnable> collectionElementAdders = new ArrayList<Runnable>();
        populateObjectFromJsonObject(containingObject, containingObject.getClass(), wrapperJsonObj, classFieldCache, new HashMap<CharSequence, Object>(), collectionElementAdders);
        for (final Runnable runnable : collectionElementAdders) {
            runnable.run();
        }
    }
    
    public static void deserializeToField(final Object containingObject, final String fieldName, final String json) throws IllegalArgumentException {
        final ClassFieldCache typeCache = new ClassFieldCache(true, false);
        deserializeToField(containingObject, fieldName, json, typeCache);
    }
    
    private static class ObjectInstantiation
    {
        Object jsonVal;
        Object objectInstance;
        Type type;
        
        public ObjectInstantiation(final Object objectInstance, final Type type, final Object jsonVal) {
            this.jsonVal = jsonVal;
            this.objectInstance = objectInstance;
            this.type = type;
        }
    }
}
