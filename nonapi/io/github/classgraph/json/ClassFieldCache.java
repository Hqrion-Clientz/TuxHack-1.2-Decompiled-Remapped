// 
// Decompiled by Procyon v0.5.36
// 

package nonapi.io.github.classgraph.json;

import io.github.classgraph.ClassGraphException;
import java.lang.reflect.AccessibleObject;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TransferQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.AbstractQueue;
import java.util.Queue;
import java.util.TreeSet;
import java.util.SortedSet;
import java.util.HashSet;
import java.util.AbstractSet;
import java.util.Set;
import java.util.LinkedList;
import java.util.AbstractSequentialList;
import java.util.Collection;
import java.util.ArrayList;
import java.util.AbstractList;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.TreeMap;
import java.util.NavigableMap;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.AbstractMap;
import java.util.HashMap;
import java.lang.reflect.Constructor;
import java.util.Map;

class ClassFieldCache
{
    private final Map<Class<?>, ClassFields> classToClassFields;
    private final boolean resolveTypes;
    private final boolean onlySerializePublicFields;
    private final Map<Class<?>, Constructor<?>> defaultConstructorForConcreteType;
    private final Map<Class<?>, Constructor<?>> constructorForConcreteTypeWithSizeHint;
    private static final Constructor<?> NO_CONSTRUCTOR;
    
    ClassFieldCache(final boolean forDeserialization, final boolean onlySerializePublicFields) {
        this.classToClassFields = new HashMap<Class<?>, ClassFields>();
        this.defaultConstructorForConcreteType = new HashMap<Class<?>, Constructor<?>>();
        this.constructorForConcreteTypeWithSizeHint = new HashMap<Class<?>, Constructor<?>>();
        this.resolveTypes = forDeserialization;
        this.onlySerializePublicFields = (!forDeserialization && onlySerializePublicFields);
    }
    
    ClassFields get(final Class<?> cls) {
        ClassFields classFields = this.classToClassFields.get(cls);
        if (classFields == null) {
            this.classToClassFields.put(cls, classFields = new ClassFields(cls, this.resolveTypes, this.onlySerializePublicFields, this));
        }
        return classFields;
    }
    
    private static Class<?> getConcreteType(final Class<?> rawType, final boolean returnNullIfNotMapOrCollection) {
        if (rawType == Map.class || rawType == AbstractMap.class || rawType == HashMap.class) {
            return HashMap.class;
        }
        if (rawType == ConcurrentMap.class || rawType == ConcurrentHashMap.class) {
            return ConcurrentHashMap.class;
        }
        if (rawType == SortedMap.class || rawType == NavigableMap.class || rawType == TreeMap.class) {
            return TreeMap.class;
        }
        if (rawType == ConcurrentNavigableMap.class || rawType == ConcurrentSkipListMap.class) {
            return ConcurrentSkipListMap.class;
        }
        if (rawType == List.class || rawType == AbstractList.class || rawType == ArrayList.class || rawType == Collection.class) {
            return ArrayList.class;
        }
        if (rawType == AbstractSequentialList.class || rawType == LinkedList.class) {
            return LinkedList.class;
        }
        if (rawType == Set.class || rawType == AbstractSet.class || rawType == HashSet.class) {
            return HashSet.class;
        }
        if (rawType == SortedSet.class || rawType == TreeSet.class) {
            return TreeSet.class;
        }
        if (rawType == Queue.class || rawType == AbstractQueue.class || rawType == Deque.class || rawType == ArrayDeque.class) {
            return ArrayDeque.class;
        }
        if (rawType == BlockingQueue.class || rawType == LinkedBlockingQueue.class) {
            return LinkedBlockingQueue.class;
        }
        if (rawType == BlockingDeque.class || rawType == LinkedBlockingDeque.class) {
            return LinkedBlockingDeque.class;
        }
        if (rawType == TransferQueue.class || rawType == LinkedTransferQueue.class) {
            return LinkedTransferQueue.class;
        }
        return returnNullIfNotMapOrCollection ? null : rawType;
    }
    
    Constructor<?> getDefaultConstructorForConcreteTypeOf(final Class<?> cls) {
        if (cls == null) {
            throw new IllegalArgumentException("Class reference cannot be null");
        }
        final Constructor<?> constructor = this.defaultConstructorForConcreteType.get(cls);
        if (constructor != null) {
            return constructor;
        }
        Class<?> c;
        final Class<?> concreteType = c = getConcreteType(cls, false);
        while (c != null) {
            if (c == Object.class) {
                if (cls != Object.class) {
                    break;
                }
            }
            try {
                final Constructor<?> defaultConstructor = c.getDeclaredConstructor((Class<?>[])new Class[0]);
                JSONUtils.isAccessibleOrMakeAccessible(defaultConstructor);
                this.defaultConstructorForConcreteType.put(cls, defaultConstructor);
                return defaultConstructor;
            }
            catch (ReflectiveOperationException | SecurityException ex) {
                c = c.getSuperclass();
                continue;
            }
            break;
        }
        throw new IllegalArgumentException("Class " + cls.getName() + " does not have an accessible default (no-arg) constructor");
    }
    
    Constructor<?> getConstructorWithSizeHintForConcreteTypeOf(final Class<?> cls) {
        final Constructor<?> constructor = this.constructorForConcreteTypeWithSizeHint.get(cls);
        if (constructor == ClassFieldCache.NO_CONSTRUCTOR) {
            return null;
        }
        if (constructor != null) {
            return constructor;
        }
        final Class<?> concreteType = getConcreteType(cls, true);
        if (concreteType != null) {
            Class<?> c = concreteType;
            while (c != null) {
                if (c == Object.class) {
                    if (cls != Object.class) {
                        break;
                    }
                }
                try {
                    final Constructor<?> constructorWithSizeHint = c.getDeclaredConstructor(Integer.TYPE);
                    JSONUtils.isAccessibleOrMakeAccessible(constructorWithSizeHint);
                    this.constructorForConcreteTypeWithSizeHint.put(cls, constructorWithSizeHint);
                    return constructorWithSizeHint;
                }
                catch (ReflectiveOperationException | SecurityException ex) {
                    c = c.getSuperclass();
                    continue;
                }
                break;
            }
        }
        this.constructorForConcreteTypeWithSizeHint.put(cls, ClassFieldCache.NO_CONSTRUCTOR);
        return null;
    }
    
    static {
        try {
            NO_CONSTRUCTOR = NoConstructor.class.getDeclaredConstructor((Class<?>[])new Class[0]);
        }
        catch (NoSuchMethodException | SecurityException ex2) {
            final Exception ex;
            final Exception e = ex;
            throw ClassGraphException.newClassGraphException("Could not find or access constructor for " + NoConstructor.class.getName(), e);
        }
    }
    
    private static class NoConstructor
    {
        public NoConstructor() {
        }
    }
}
