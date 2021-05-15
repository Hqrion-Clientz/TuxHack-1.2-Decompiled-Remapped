// 
// Decompiled by Procyon v0.5.36
// 

package nonapi.io.github.classgraph.utils;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;
import java.util.LinkedList;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;
import java.lang.reflect.Field;

public final class ReflectionUtils
{
    private ReflectionUtils() {
    }
    
    private static Object getFieldVal(final Class<?> cls, final Object obj, final String fieldName, final boolean throwException) throws IllegalArgumentException {
        Field field = null;
        Class<?> currClass = cls;
        while (currClass != null) {
            try {
                field = currClass.getDeclaredField(fieldName);
            }
            catch (ReflectiveOperationException | SecurityException ex) {
                currClass = currClass.getSuperclass();
                continue;
            }
            break;
        }
        if (field == null) {
            if (throwException) {
                throw new IllegalArgumentException(((obj == null) ? "Static field " : "Field ") + "\"" + fieldName + "\" not found or not accessible");
            }
        }
        else {
            try {
                field.setAccessible(true);
            }
            catch (RuntimeException ex2) {}
            try {
                return field.get(obj);
            }
            catch (IllegalAccessException e) {
                if (throwException) {
                    throw new IllegalArgumentException("Can't read " + ((obj == null) ? "static " : "") + " field \"" + fieldName + "\": " + e);
                }
            }
        }
        return null;
    }
    
    public static Object getFieldVal(final Object obj, final String fieldName, final boolean throwException) throws IllegalArgumentException {
        if (obj != null && fieldName != null) {
            return getFieldVal(obj.getClass(), obj, fieldName, throwException);
        }
        if (throwException) {
            throw new NullPointerException();
        }
        return null;
    }
    
    public static Object getStaticFieldVal(final Class<?> cls, final String fieldName, final boolean throwException) throws IllegalArgumentException {
        if (cls != null && fieldName != null) {
            return getFieldVal(cls, null, fieldName, throwException);
        }
        if (throwException) {
            throw new NullPointerException();
        }
        return null;
    }
    
    private static List<Class<?>> getReverseMethodAttemptOrder(final Class<?> cls) {
        final List<Class<?>> reverseAttemptOrder = new ArrayList<Class<?>>();
        for (Class<?> c = cls; c != null && c != Object.class; c = c.getSuperclass()) {
            reverseAttemptOrder.add(c);
        }
        final Set<Class<?>> addedIfaces = new HashSet<Class<?>>();
        final LinkedList<Class<?>> ifaceQueue = new LinkedList<Class<?>>();
        for (Class<?> c2 = cls; c2 != null; c2 = c2.getSuperclass()) {
            if (c2.isInterface() && addedIfaces.add(c2)) {
                ifaceQueue.add(c2);
            }
            for (final Class<?> iface : c2.getInterfaces()) {
                if (addedIfaces.add(iface)) {
                    ifaceQueue.add(iface);
                }
            }
        }
        while (!ifaceQueue.isEmpty()) {
            final Class<?> iface2 = ifaceQueue.remove();
            reverseAttemptOrder.add(iface2);
            final Class<?>[] superIfaces = iface2.getInterfaces();
            if (superIfaces.length > 0) {
                for (final Class<?> superIface : superIfaces) {
                    if (addedIfaces.add(superIface)) {
                        ifaceQueue.add(superIface);
                    }
                }
            }
        }
        return reverseAttemptOrder;
    }
    
    private static Object invokeMethod(final Class<?> cls, final Object obj, final String methodName, final boolean oneArg, final Class<?> argType, final Object param, final boolean throwException) throws IllegalArgumentException {
        Method method = null;
        final List<Class<?>> reverseAttemptOrder = getReverseMethodAttemptOrder(cls);
        int i = reverseAttemptOrder.size() - 1;
        while (i >= 0) {
            final Class<?> classOrInterface = reverseAttemptOrder.get(i);
            try {
                method = (oneArg ? classOrInterface.getDeclaredMethod(methodName, argType) : classOrInterface.getDeclaredMethod(methodName, (Class<?>[])new Class[0]));
            }
            catch (ReflectiveOperationException | SecurityException ex2) {
                --i;
                continue;
            }
            break;
        }
        if (method == null) {
            if (throwException) {
                throw new IllegalArgumentException(((obj == null) ? "Static method " : "Method ") + "\"" + methodName + "\" not found or not accesible");
            }
        }
        else {
            try {
                method.setAccessible(true);
            }
            catch (RuntimeException ex3) {}
            try {
                return oneArg ? method.invoke(obj, param) : method.invoke(obj, new Object[0]);
            }
            catch (IllegalAccessException | SecurityException ex4) {
                final Exception ex;
                final Exception e = ex;
                if (throwException) {
                    throw new IllegalArgumentException("Can't call " + ((obj == null) ? "static " : "") + "method \"" + methodName + "\": " + e);
                }
            }
            catch (InvocationTargetException e2) {
                if (throwException) {
                    throw new IllegalArgumentException("Exception while invoking " + ((obj == null) ? "static " : "") + "method \"" + methodName + "\"", e2);
                }
            }
        }
        return null;
    }
    
    public static Object invokeMethod(final Object obj, final String methodName, final boolean throwException) throws IllegalArgumentException {
        if (obj != null && methodName != null) {
            return invokeMethod(obj.getClass(), obj, methodName, false, null, null, throwException);
        }
        if (throwException) {
            throw new NullPointerException();
        }
        return null;
    }
    
    public static Object invokeMethod(final Object obj, final String methodName, final Class<?> argType, final Object param, final boolean throwException) throws IllegalArgumentException {
        if (obj != null && methodName != null) {
            return invokeMethod(obj.getClass(), obj, methodName, true, argType, param, throwException);
        }
        if (throwException) {
            throw new NullPointerException();
        }
        return null;
    }
    
    public static Object invokeStaticMethod(final Class<?> cls, final String methodName, final boolean throwException) throws IllegalArgumentException {
        if (cls != null && methodName != null) {
            return invokeMethod(cls, null, methodName, false, null, null, throwException);
        }
        if (throwException) {
            throw new NullPointerException();
        }
        return null;
    }
    
    public static Object invokeStaticMethod(final Class<?> cls, final String methodName, final Class<?> argType, final Object param, final boolean throwException) throws IllegalArgumentException {
        if (cls != null && methodName != null) {
            return invokeMethod(cls, null, methodName, true, argType, param, throwException);
        }
        if (throwException) {
            throw new NullPointerException();
        }
        return null;
    }
    
    public static Class<?> classForNameOrNull(final String className) {
        try {
            return Class.forName(className);
        }
        catch (ReflectiveOperationException | LinkageError ex) {
            final Throwable t;
            final Throwable e = t;
            return null;
        }
    }
}
